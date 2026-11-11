package com.spotifyclient.media;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/** Reads YouTube Music's Media Session and HTML media clock through local Chrome DevTools. */
final class BrowserMediaReader {
    private static final URI TARGETS = URI.create("http://127.0.0.1:9223/json");
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(500)).build();
    private static final String EXPRESSION = "(() => { const v=document.querySelector('video'); const m=navigator.mediaSession&&navigator.mediaSession.metadata; return JSON.stringify({title:(m&&m.title)||document.title||'',artist:(m&&m.artist)||'',album:(m&&m.album)||'',positionMs:v?Math.floor(v.currentTime*1000):0,durationMs:v&&Number.isFinite(v.duration)?Math.floor(v.duration*1000):0,playing:!!v&&!v.paused&&!v.ended}); })()";

    private BrowserMediaReader() {}

    static WindowsMediaReader.Snapshot readSnapshot() {
        try {
            HttpRequest request = HttpRequest.newBuilder(TARGETS).timeout(Duration.ofMillis(900)).GET().build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;
            var targets = JsonParser.parseString(response.body()).getAsJsonArray();
            for (var item : targets) {
                JsonObject target = item.getAsJsonObject();
                if (!"page".equals(target.has("type") ? target.get("type").getAsString() : "")) continue;
                String url = target.has("url") ? target.get("url").getAsString() : "";
                if (!url.startsWith("https://music.youtube.com")) continue;
                String socket = target.has("webSocketDebuggerUrl") ? target.get("webSocketDebuggerUrl").getAsString() : null;
                if (socket == null) continue;
                return evaluate(URI.create(socket));
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static WindowsMediaReader.Snapshot evaluate(URI socketUri) {
        WebSocket socket = null;
        try {
            ResponseListener listener = new ResponseListener();
            socket = HTTP.newWebSocketBuilder().connectTimeout(Duration.ofMillis(750))
                .buildAsync(socketUri, listener).get(900, TimeUnit.MILLISECONDS);
            socket.sendText("{\"id\":1,\"method\":\"Runtime.evaluate\",\"params\":{\"expression\":" +
                JsonParser.parseString(quote(EXPRESSION)) + ",\"returnByValue\":true}}", true).get(500, TimeUnit.MILLISECONDS);
            JsonObject response = listener.response.get(900, TimeUnit.MILLISECONDS);
            JsonObject remote = response.getAsJsonObject("result").getAsJsonObject("result");
            if (!remote.has("value") || !remote.get("value").isJsonPrimitive()) return null;
            JsonObject media = JsonParser.parseString(remote.get("value").getAsString()).getAsJsonObject();
            String title = text(media, "title");
            if (title.endsWith(" - YouTube Music")) title = title.substring(0, title.length() - " - YouTube Music".length());
            if (title.isBlank()) return null;
            return new WindowsMediaReader.Snapshot(title, text(media, "artist"), text(media, "album"),
                number(media, "positionMs"), number(media, "durationMs"), bool(media, "playing"), System.currentTimeMillis());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } catch (Throwable ignored) {
        } finally {
            if (socket != null) {
                try { socket.sendClose(WebSocket.NORMAL_CLOSURE, "done"); }
                catch (Throwable ignored) { }
            }
        }
        return null;
    }

    private static String quote(String value) {
        return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + '"';
    }

    private static String text(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : "";
    }

    private static long number(JsonObject object, String key) {
        try { return Math.max(0L, object.get(key).getAsLong()); }
        catch (Throwable ignored) { return 0L; }
    }

    private static boolean bool(JsonObject object, String key) {
        try { return object.get(key).getAsBoolean(); }
        catch (Throwable ignored) { return false; }
    }

    private static final class ResponseListener implements WebSocket.Listener {
        private final StringBuilder buffer = new StringBuilder();
        private final CompletableFuture<JsonObject> response = new CompletableFuture<>();

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public java.util.concurrent.CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                try {
                    JsonObject message = JsonParser.parseString(buffer.toString()).getAsJsonObject();
                    if (message.has("id") && message.get("id").getAsInt() == 1) response.complete(message);
                } catch (Throwable error) {
                    response.completeExceptionally(error);
                }
                buffer.setLength(0);
            }
            webSocket.request(1);
            return null;
        }
    }
}

/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package hook.utils.embed;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DiscordWebhook {
    private final String webhookUrl;
    private String content;
    private String username;
    private String avatarUrl;
    private boolean tts;
    private final List<EmbedObject> embeds = new ArrayList<EmbedObject>();

    public DiscordWebhook(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void a(String content) {
        this.content = content;
    }

    public void b(String username) {
        this.username = username;
    }

    public void c(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void a(boolean tts) {
        this.tts = tts;
    }

    public void addEmbed(EmbedObject embed) {
        this.embeds.add(embed);
    }

    public void execute() throws IOException {
    }

    private static String readResponse(InputStream input) throws IOException {
        String var2;
        if (input == null) {
            return "";
        }
        InputStream var1 = input;
        try {
            var2 = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (Throwable var5) {
            if (input != null) {
                try {
                    var1.close();
                }
                catch (Throwable var4) {
                    var5.addSuppressed(var4);
                }
            }
            throw var5;
        }
        if (input != null) {
            input.close();
        }
        return var2;
    }

    public record Thumbnail(String url) {
    }

    public record Image(String url) {
    }

    public record Footer(String text, String iconUrl) {
    }

    public record Field(String a, String b, boolean c) {
    }

    public static class EmbedObject {
        public String title;
        public String description;
        public String url;
        public Color color;
        public Footer footer;
        public Thumbnail thumbnail;
        public Image image;
        public Author author;
        public final List<Field> fields = new ArrayList<Field>();

        public EmbedObject setDescription(String value) {
            this.description = value;
            return this;
        }

        public EmbedObject setColor(Color value) {
            this.color = value;
            return this;
        }

        public EmbedObject setTitle(String value) {
            this.title = value;
            return this;
        }

        public EmbedObject setUrl(String value) {
            this.url = value;
            return this;
        }

        public EmbedObject setFooter(String text, String iconUrl) {
            this.footer = new Footer(text, iconUrl);
            return this;
        }

        public EmbedObject setImage(Image value) {
            this.image = value;
            return this;
        }

        public EmbedObject setThumbnail(String value) {
            this.thumbnail = new Thumbnail(value);
            return this;
        }

        public EmbedObject setAuthor(Author value) {
            this.author = value;
            return this;
        }

        public EmbedObject addField(String name, String value, boolean inline) {
            this.fields.add(new Field(name, value, inline));
            return this;
        }

        private JsonObject toJson() {
            JsonObject child;
            JsonObject json = new JsonObject();
            if (this.title != null) {
                json.addProperty("title", this.title);
            }
            if (this.description != null) {
                json.addProperty("description", this.description);
            }
            if (this.url != null) {
                json.addProperty("url", this.url);
            }
            if (this.color != null) {
                json.addProperty("color", (Number)(this.color.getRed() << 16 | this.color.getGreen() << 8 | this.color.getBlue()));
            }
            if (this.footer != null) {
                child = new JsonObject();
                child.addProperty("text", this.footer.text());
                if (this.footer.iconUrl() != null) {
                    child.addProperty("icon_url", this.footer.iconUrl());
                }
                json.add("footer", (JsonElement)child);
            }
            if (this.image != null) {
                child = new JsonObject();
                child.addProperty("url", this.image.url());
                json.add("image", (JsonElement)child);
            }
            if (this.thumbnail != null) {
                child = new JsonObject();
                child.addProperty("url", this.thumbnail.url());
                json.add("thumbnail", (JsonElement)child);
            }
            if (this.author != null) {
                child = new JsonObject();
                child.addProperty("name", this.author.name());
                if (this.author.url() != null) {
                    child.addProperty("url", this.author.url());
                }
                if (this.author.iconUrl() != null) {
                    child.addProperty("icon_url", this.author.iconUrl());
                }
                json.add("author", (JsonElement)child);
            }
            if (!this.fields.isEmpty()) {
                JsonArray jsonFields = new JsonArray();
                for (Field field : this.fields) {
                    JsonObject childx = new JsonObject();
                    childx.addProperty("name", field.a());
                    childx.addProperty("value", field.b());
                    childx.addProperty("inline", Boolean.valueOf(field.c()));
                    jsonFields.add((JsonElement)childx);
                }
                json.add("fields", (JsonElement)jsonFields);
            }
            return json;
        }
    }

    public record Author(String name, String url, String iconUrl) {
    }
}


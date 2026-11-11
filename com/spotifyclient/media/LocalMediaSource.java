package com.spotifyclient.media;

/** Polls the selected local Spotify backend and publishes the latest snapshot. */
public final class LocalMediaSource {
    private volatile WindowsMediaReader.Snapshot latest;
    private volatile boolean running;
    private Thread thread;

    public synchronized void start() {
        if (running) return;
        running = true;
        thread = new Thread(this::run, "Versefall local media");
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        latest = null;
        if (thread != null) thread.interrupt();
    }

    public WindowsMediaReader.Snapshot latest() {
        return latest;
    }

    private void run() {
        WindowsMediaReader windows = new WindowsMediaReader();
        try {
            while (running) {
                if (MediaBackend.isLinux()) {
                    windows.close();
                    latest = LinuxMediaReader.readSnapshot();
                    Thread.sleep(1_000L);
                    continue;
                }

                if (MediaBackend.isBrowser()) {
                    windows.close();
                    latest = BrowserMediaReader.readSnapshot();
                    Thread.sleep(750L);
                    continue;
                }

                if (!WindowsMediaReader.isSupported()) {
                    latest = null;
                    Thread.sleep(1_000L);
                    continue;
                }

                if (!windows.open()) {
                    latest = null;
                    Thread.sleep(5_000L);
                    continue;
                }
                while (running && MediaBackend.isWindows()) {
                    latest = windows.read();
                    Thread.sleep(500L);
                }
                windows.close();
                latest = null;
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (Throwable ignored) {
            latest = null;
        } finally {
            windows.close();
            latest = null;
        }
    }
}

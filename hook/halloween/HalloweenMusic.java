/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.floats.FloatConsumer
 *  net.minecraft.class_4228
 */
package hook.halloween;

import hook.feature.UiAudio;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import java.io.BufferedInputStream;
import java.io.InputStream;
import net.minecraft.class_4228;

public final class HalloweenMusic {
    static final int SR = 44100;
    private static final float MENU_FADE_IN = 1.8f;
    private static final float MENU_FADE_OUT = 5.0f;
    private static final float EVENT_FADE_IN = 2.6f;
    private static final float EVENT_HOLD = 56.0f;
    private static final float EVENT_FADE_OUT = 5.5f;
    static final Track MENU = new Track("/assets/hook/music/halloween_menu.ogg", true);
    static final Track EVENT = new Track("/assets/hook/music/halloween_event.ogg", false);
    private static volatile boolean menuWanted;
    private static volatile float menuVolume;
    private static volatile float eventVolume;
    private static volatile boolean eventRequested;
    private static volatile boolean eventStopRequested;
    private static volatile boolean beatEnabled;
    private static final int BEATS = 32;
    private static final long[] beatFrame;
    private static final float[] beatStrength;
    private static volatile int beatHead;

    private HalloweenMusic() {
    }

    public static void setMenuWanted(boolean var0, float var1) {
        menuVolume = Math.max(0.0f, Math.min(1.0f, var1));
        if (var0 != menuWanted) {
            menuWanted = var0;
        }
        if (var0 || HalloweenMusic.MENU.playing) {
            UiAudio.wake();
        }
    }

    public static void setEventVolume(float var0) {
        eventVolume = Math.max(0.0f, Math.min(1.0f, var0));
    }

    public static void playEvent(float var0) {
        HalloweenMusic.setEventVolume(var0);
        if (!HalloweenMusic.EVENT.playing || HalloweenMusic.EVENT.stopping) {
            eventRequested = true;
            UiAudio.wake();
        }
    }

    public static void stopEvent() {
        if (HalloweenMusic.EVENT.playing) {
            eventStopRequested = true;
            UiAudio.wake();
        }
    }

    public static boolean eventPlaying() {
        return HalloweenMusic.EVENT.playing || eventRequested;
    }

    public static boolean menuPlaying() {
        return HalloweenMusic.MENU.playing;
    }

    public static boolean active() {
        return HalloweenMusic.MENU.playing || HalloweenMusic.EVENT.playing || menuWanted || eventRequested;
    }

    public static boolean replacesAmbience() {
        return menuWanted || HalloweenMusic.MENU.playing && HalloweenMusic.MENU.level > 0.02f;
    }

    public static void setBeatEnabled(boolean var0) {
        beatEnabled = var0;
    }

    public static float beat() {
        if (HalloweenMusic.MENU.playing && beatEnabled) {
            long var0 = UiAudio.heardFrame();
            float var2 = 0.0f;
            int var3 = beatHead;
            for (int var4 = 0; var4 < 32; ++var4) {
                float var7;
                int var5 = (var3 - 1 - var4 + 32) % 32;
                float var6 = beatStrength[var5];
                if (var6 <= 0.0f || (var7 = (float)(var0 - beatFrame[var5]) / 44100.0f) < 0.0f) continue;
                if (var7 > 1.2f) break;
                float var8 = Math.min(1.0f, var7 / 0.035f);
                var8 = var8 * var8 * (3.0f - 2.0f * var8);
                var2 = Math.max(var2, var6 * var8 * (float)Math.exp(-var7 / 0.22f));
            }
            return var2 * HalloweenMusic.smoothLevel(HalloweenMusic.MENU.level);
        }
        return 0.0f;
    }

    private static float smoothLevel(float var0) {
        var0 = Math.max(0.0f, Math.min(1.0f, var0));
        return var0 * var0 * (3.0f - 2.0f * var0);
    }

    public static void mix(float[] var0, int var1, float var2, long var3) {
        boolean var6;
        Track var5 = MENU;
        boolean bl = var6 = menuWanted && !HalloweenMusic.EVENT.playing;
        if (var6 && !var5.playing) {
            var5.resume();
        }
        if (var5.playing) {
            var5.target = var6 ? 1.0f : 0.0f;
            var5.rateUp = 1.2597631E-5f;
            var5.rateDown = 1.0f / (44100.0f * (HalloweenMusic.EVENT.playing ? 2.0f : 5.0f));
            var5.mix(var0, var1, menuVolume * var2, true, var3);
            if (!var6 && var5.level <= 0.0f) {
                var5.pause();
            }
        }
        Track var7 = EVENT;
        if (eventRequested) {
            eventRequested = false;
            if (!var7.playing) {
                var7.open();
            }
            var7.stopping = false;
            var7.target = 1.0f;
        }
        if (eventStopRequested) {
            eventStopRequested = false;
            if (var7.playing) {
                var7.stopping = true;
            }
        }
        if (var7.playing) {
            float var8 = (float)var7.frames / 44100.0f;
            if (var8 >= 58.6f) {
                var7.stopping = true;
            }
            var7.target = var7.stopping ? 0.0f : 1.0f;
            var7.rateUp = 8.721438E-6f;
            var7.rateDown = 1.0f / (44100.0f * (var8 >= 56.0f ? 5.5f : 2.4f));
            var7.mix(var0, var1, eventVolume * var2, false, var3);
            if (var7.stopping && var7.level <= 0.0f || var7.ended) {
                var7.close();
            }
        }
    }

    static void recordBeat(long var0, float var2) {
        int var3 = beatHead;
        HalloweenMusic.beatFrame[var3] = var0;
        HalloweenMusic.beatStrength[var3] = var2;
        beatHead = (var3 + 1) % 32;
    }

    public static void closeAll() {
        MENU.close();
        EVENT.close();
        menuWanted = false;
        eventRequested = false;
        eventStopRequested = false;
    }

    static {
        menuVolume = 0.35f;
        eventVolume = 0.6f;
        beatEnabled = true;
        beatFrame = new long[32];
        beatStrength = new float[32];
    }

    static final class Track
    implements FloatConsumer {
        final String path;
        final boolean loop;
        volatile boolean playing;
        volatile boolean stopping;
        volatile float level;
        boolean ended;
        float target;
        float rateUp;
        float rateDown;
        long frames;
        private class_4228 stream;
        private float[] buf = new float[8192];
        private int len;
        private int pos;
        private float gain = -1.0f;
        private float lp1;
        private float lp2;
        private float energySlow = 1.0E-4f;
        private float energyBlock;
        private int energyCount;
        private long lastBeat = -44100L;

        Track(String var1, boolean var2) {
            this.path = var1;
            this.loop = var2;
        }

        void resume() {
            if (this.stream != null && !this.ended) {
                this.playing = true;
                this.stopping = false;
            } else {
                this.open();
            }
        }

        void pause() {
            this.playing = false;
            this.stopping = false;
            this.level = 0.0f;
        }

        void open() {
            this.closeStream();
            try {
                InputStream var1 = HalloweenMusic.class.getResourceAsStream(this.path);
                if (var1 == null) {
                    System.err.println("Hook: missing " + this.path);
                    return;
                }
                this.stream = new class_4228((InputStream)new BufferedInputStream(var1, 32768));
                this.playing = true;
                this.stopping = false;
                this.ended = false;
                this.level = 0.0f;
                this.frames = 0L;
                this.len = 0;
                this.pos = 0;
                this.gain = -1.0f;
            }
            catch (Throwable var2) {
                System.err.println("Hook: could not open " + this.path + ": " + String.valueOf(var2));
                this.closeStream();
                this.playing = false;
            }
        }

        void close() {
            this.closeStream();
            this.playing = false;
            this.stopping = false;
            this.level = 0.0f;
            this.len = 0;
            this.pos = 0;
        }

        private void closeStream() {
            if (this.stream != null) {
                try {
                    this.stream.close();
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                this.stream = null;
            }
        }

        public void accept(float var1) {
            if (this.len == this.buf.length) {
                float[] var2 = new float[this.buf.length * 2];
                System.arraycopy(this.buf, 0, var2, 0, this.len);
                this.buf = var2;
            }
            this.buf[this.len++] = var1;
        }

        private boolean ensure() {
            while (this.pos + 1 >= this.len) {
                if (this.stream == null) {
                    return false;
                }
                this.len = 0;
                this.pos = 0;
                try {
                    if (this.stream.method_59758((FloatConsumer)this)) continue;
                    if (!this.loop) {
                        this.ended = true;
                        return false;
                    }
                    this.closeStream();
                    InputStream var1 = HalloweenMusic.class.getResourceAsStream(this.path);
                    if (var1 == null) {
                        return false;
                    }
                    this.stream = new class_4228((InputStream)new BufferedInputStream(var1, 32768));
                }
                catch (Throwable var2) {
                    System.err.println("Hook: music decode failed: " + String.valueOf(var2));
                    this.ended = true;
                    this.closeStream();
                    return false;
                }
            }
            return true;
        }

        void mix(float[] var1, int var2, float var3, boolean var4, long var5) {
            if (this.gain < 0.0f) {
                this.gain = var3;
            }
            for (int var7 = 0; var7 < var2; ++var7) {
                if (this.target <= 0.0f && this.level <= 0.0f) {
                    return;
                }
                if (!this.ensure()) {
                    this.level = 0.0f;
                    return;
                }
                float var8 = this.buf[this.pos++];
                float var9 = this.buf[this.pos++];
                ++this.frames;
                this.level = this.target > this.level ? Math.min(this.target, this.level + this.rateUp) : Math.max(this.target, this.level - this.rateDown);
                this.gain += (var3 - this.gain) * 2.0E-4f;
                float var10 = HalloweenMusic.smoothLevel(this.level) * this.gain;
                var1[var7 * 2] = var1[var7 * 2] + var8 * var10;
                var1[var7 * 2 + 1] = var1[var7 * 2 + 1] + var9 * var10;
                if (!var4) continue;
                float var11 = (var8 + var9) * 0.5f;
                this.lp1 += 0.017f * (var11 - this.lp1);
                this.lp2 += 0.017f * (this.lp1 - this.lp2);
                this.energyBlock += this.lp2 * this.lp2;
                ++this.energyCount;
                if (this.energyCount != 256) continue;
                float var12 = this.energyBlock / 256.0f;
                this.energyBlock = 0.0f;
                this.energyCount = 0;
                long var13 = var5 + (long)var7;
                float var15 = var12 / (this.energySlow + 1.0E-7f);
                this.energySlow += (var12 - this.energySlow) * 0.0083f;
                if (!(var15 > 1.55f) || !((float)(var13 - this.lastBeat) > 10584.0f) || !(var12 > 2.0E-5f)) continue;
                this.lastBeat = var13;
                float var16 = Math.min(1.0f, (var15 - 1.55f) / 1.6f + 0.35f);
                HalloweenMusic.recordBeat(var13, var16);
            }
        }
    }
}


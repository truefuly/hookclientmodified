package hook.feature;

import hook.halloween.HalloweenMusic;
import hook.module.modules.client.Hook;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public final class UiAudio {
   static final int SR = 44100;
   private static final int BLOCK = 512;
   private static final Object LOCK = new Object();
   private static final ConcurrentLinkedQueue<float[]> QUEUE = new ConcurrentLinkedQueue<>();
   private static volatile float master = 1.0F;
   private static volatile boolean failed;
   public static volatile boolean muted;
   private static Thread thread;
   private static float[][] bank;
   private static long lastSlide;
   private static volatile long written;
   private static volatile long played;
   private static volatile long playedAt;

   private UiAudio() {
   }

   public static void play(UiAudio.Cue var0) {
      if (!muted && !failed) {
         try {
            if (!Hook.uiSounds.getValue()) {
               return;
            }

            if (var0 == UiAudio.Cue.SLIDE) {
               long var1 = System.nanoTime();
               if (var1 - lastSlide < 45000000L) {
                  return;
               }

               lastSlide = var1;
            }

            float var4 = (float)(Hook.uiVolume.getValue() / 100.0) * master;
            if (var4 > 0.001F) {
               QUEUE.add(new float[]{var0.ordinal(), var4});
               ensureRunning();
            }
         } catch (Throwable var3) {
         }
      }
   }

   public static void setAmbience(boolean var0) {
   }

   public static void setMaster(float var0) {
      master = Math.max(0.0F, Math.min(1.0F, var0));
   }

   public static void wake() {
      ensureRunning();
   }

   public static long heardFrame() {
      long var0 = played;
      long var2 = playedAt;
      if (var2 == 0L) {
         return 0L;
      }

      long var4 = var0 + (System.nanoTime() - var2) * 44100L / 1000000000L;
      return Math.min(var4, written);
   }

   private static void ensureRunning() {
      if (!muted && !failed) {
         synchronized (LOCK) {
            if (thread == null) {
               thread = new Thread(UiAudio::run, "Hook Audio");
               thread.setDaemon(true);
               thread.setPriority(7);
               thread.start();
            }
         }
      }
   }

   private static void run() {
      SourceDataLine var0 = null;

      try {
         AudioFormat var1 = new AudioFormat(44100.0F, 16, 2, true, false);
         var0 = AudioSystem.getSourceDataLine(var1);
         var0.open(var1, 8192);
         var0.start();
         long var29 = written;
         if (bank == null) {
            bank = UiAudio.WaveBank.bank();
         }

         ArrayList var4 = new ArrayList();
         float[] var5 = new float[1024];
         byte[] var6 = new byte[2048];
         long var7 = 0L;

         while (true) {
            float[] var9;
            while ((var9 = QUEUE.poll()) != null) {
               var4.add(new UiAudio.PlayingCue(bank[(int)var9[0]], var9[1]));
            }

            Arrays.fill(var5, 0.0F);

            for (int var10 = var4.size() - 1; var10 >= 0; var10--) {
               if (((UiAudio.PlayingCue)var4.get(var10)).mixInto(var5)) {
                  var4.remove(var10);
               }
            }

            HalloweenMusic.mix(var5, 512, master, written);

            for (int var30 = 0; var30 < var5.length; var30++) {
               float var11 = var5[var30];
               var11 /= 1.0F + Math.abs(var11) * 0.25F;
               int var12 = Math.round(Math.max(-1.0F, Math.min(1.0F, var11)) * 32767.0F);
               var6[var30 * 2] = (byte)var12;
               var6[var30 * 2 + 1] = (byte)(var12 >> 8);
            }

            var0.write(var6, 0, var6.length);
            written += 512L;
            played = var29 + var0.getLongFramePosition();
            playedAt = System.nanoTime();
            if (!var4.isEmpty() || HalloweenMusic.active()) {
               var7 = 0L;
            } else if (var7 == 0L) {
               var7 = System.nanoTime();
            } else if (System.nanoTime() - var7 > 4000000000L) {
               synchronized (LOCK) {
                  if (QUEUE.isEmpty() && !HalloweenMusic.active()) {
                     thread = null;
                     return;
                  }
               }

               var7 = 0L;
            }
         }
      } catch (Throwable var27) {
         failed = true;
         System.err.println("Hook: audio output unavailable: " + var27);
         HalloweenMusic.closeAll();
         synchronized (LOCK) {
            thread = null;
         }
      } finally {
         if (var0 != null) {
            try {
               var0.drain();
               var0.stop();
               var0.close();
            } catch (Throwable var24) {
            }
         }
      }
   }

   public static float[] renderSfx(UiAudio.Cue var0) {
      if (bank == null) {
         bank = UiAudio.WaveBank.bank();
      }

      return bank[var0.ordinal()];
   }

   public enum Cue {
      TICK,
      SLIDE,
      TAB,
      ON,
      OFF,
      OPEN,
      BACK,
      CONFIRM,
      THEME,
      GUI_OPEN,
      GUI_CLOSE;

      private static UiAudio.Cue[] $values() {
         return new UiAudio.Cue[]{TICK, SLIDE, TAB, ON, OFF, OPEN, BACK, CONFIRM, THEME, GUI_OPEN, GUI_CLOSE};
      }
   }

   private static final class PlayingCue {
      final float[] data;
      final float gain;
      int pos;

      PlayingCue(float[] var1, float var2) {
         this.data = var1;
         this.gain = var2;
      }

      boolean mixInto(float[] var1) {
         int var2 = Math.min(var1.length, this.data.length - this.pos);

         for (int var3 = 0; var3 < var2; var3++) {
            var1[var3] += this.data[this.pos + var3] * this.gain;
         }

         this.pos += var2;
         return this.pos >= this.data.length;
      }
   }

   static final class WaveBank {
      private WaveBank() {
      }

      static float[][] bank() {
         float[][] var0 = new float[UiAudio.Cue.values().length][];
         var0[UiAudio.Cue.TICK.ordinal()] = finish(tick(), 0.15F);
         var0[UiAudio.Cue.SLIDE.ordinal()] = finish(slide(), 0.07F);
         var0[UiAudio.Cue.TAB.ordinal()] = finish(seq(0.2F, new float[]{784.0F, 0.0F, 0.09F, 1.0F}, new float[]{1174.7F, 0.0F, 0.06F, 0.22F}), 0.17F);
         var0[UiAudio.Cue.ON.ordinal()] = finish(seq(0.3F, new float[]{659.3F, 0.0F, 0.14F, 1.0F}, new float[]{987.8F, 0.055F, 0.18F, 0.9F}), 0.19F);
         var0[UiAudio.Cue.OFF.ordinal()] = finish(seq(0.28F, new float[]{880.0F, 0.0F, 0.11F, 0.85F}, new float[]{659.3F, 0.05F, 0.15F, 1.0F}), 0.16F);
         var0[UiAudio.Cue.OPEN.ordinal()] = finish(add(swoosh(380.0F, 2400.0F, 0.17F, 0.5F), seq(0.32F, new float[]{1108.7F, 0.06F, 0.16F, 1.0F})), 0.16F);
         var0[UiAudio.Cue.BACK.ordinal()] = finish(add(swoosh(2200.0F, 420.0F, 0.15F, 0.45F), seq(0.26F, new float[]{830.6F, 0.035F, 0.12F, 1.0F})), 0.14F);
         var0[UiAudio.Cue.CONFIRM.ordinal()] = finish(seq(0.32F, new float[]{987.8F, 0.0F, 0.16F, 1.0F}, new float[]{1318.5F, 0.012F, 0.16F, 0.7F}), 0.17F);
         var0[UiAudio.Cue.THEME.ordinal()] = finish(
            seq(
               0.55F,
               new float[]{659.3F, 0.0F, 0.26F, 1.0F},
               new float[]{830.6F, 0.05F, 0.26F, 0.9F},
               new float[]{987.8F, 0.1F, 0.26F, 0.85F},
               new float[]{1318.5F, 0.15F, 0.3F, 0.7F}
            ),
            0.19F
         );
         var0[UiAudio.Cue.GUI_OPEN.ordinal()] = finish(
            pad(new float[]{329.6F, 493.9F, 659.3F, 1318.5F}, new float[]{0.9F, 0.7F, 0.55F, 0.16F}, 0.06F, 0.5F), 0.15F
         );
         var0[UiAudio.Cue.GUI_CLOSE.ordinal()] = finish(seq(0.45F, new float[]{493.9F, 0.0F, 0.22F, 0.9F}, new float[]{329.6F, 0.07F, 0.3F, 1.0F}), 0.12F);
         return var0;
      }

      private static float[] buf(float var0) {
         return new float[Math.round(var0 * 44100.0F)];
      }

      private static void note(float[] var0, float var1, float var2, float var3, float var4, float var5) {
         int var6 = Math.round(var2 * 44100.0F);
         double var7 = (Math.PI * 2) * var1 / 44100.0;

         for (int var9 = 0; var6 + var9 < var0.length; var9++) {
            double var10 = var9 / 44100.0;
            double var12 = (1.0 - Math.exp(-var10 / var5)) * Math.exp(-var10 / var3);
            if (var12 < 1.0E-5 && var10 > var5 * 4.0) {
               break;
            }

            double var14 = Math.sin(var7 * var9)
               + 0.16 * Math.sin(2.0 * var7 * var9) * Math.exp(-var10 / (var3 * 0.45))
               + 0.05 * Math.sin(3.0 * var7 * var9) * Math.exp(-var10 / (var3 * 0.3))
               + 0.1 * Math.sin(0.5 * var7 * var9);
            var0[var6 + var9] = var0[var6 + var9] + (float)(var4 * var12 * var14);
         }
      }

      private static float[] seq(float var0, float[]... var1) {
         float[] var2 = buf(var0);

         for (float[] var6 : var1) {
            note(var2, var6[0], var6[1], var6[2], var6[3], 0.004F);
         }

         return var2;
      }

      private static float[] pad(float[] var0, float[] var1, float var2, float var3) {
         float[] var4 = buf(1.1F);

         for (int var5 = 0; var5 < var0.length; var5++) {
            note(var4, var0[var5], 0.0F, var3, var1[var5], var2);
         }

         return var4;
      }

      private static float[] tick() {
         float[] var0 = buf(0.12F);
         note(var0, 1046.5F, 0.0F, 0.035F, 1.0F, 0.0025F);
         long var1 = 1234567L;
         float var3 = 0.0F;

         for (int var4 = 0; var4 < 264.6F; var4++) {
            var1 = var1 * 6364136223846793005L + 1442695040888963407L;
            float var5 = (float)(var1 >>> 40) / 8388608.0F - 1.0F;
            var3 += 0.25F * (var5 - var3);
            var0[var4] += var3 * 0.25F * (1.0F - var4 / 264.6F);
         }

         return var0;
      }

      private static float[] slide() {
         float[] var0 = buf(0.05F);
         note(var0, 1568.0F, 0.0F, 0.012F, 1.0F, 0.0015F);
         return var0;
      }

      private static float[] swoosh(float var0, float var1, float var2, float var3) {
         float[] var4 = buf(var2 + 0.05F);
         long var5 = 987654321L;
         float var7 = 0.0F;
         float var8 = 0.0F;
         int var9 = Math.round(var2 * 44100.0F);

         for (int var10 = 0; var10 < var9; var10++) {
            float var11 = (float)var10 / var9;
            float var12 = (float)(var0 * Math.pow(var1 / var0, var11));
            float var13 = (float)(2.0 * Math.sin(Math.PI * var12 / 44100.0));
            var5 = var5 * 6364136223846793005L + 1442695040888963407L;
            float var14 = (float)(var5 >>> 40) / 8388608.0F - 1.0F;
            var7 += var13 * var8;
            float var15 = var14 - var7 - 0.7F * var8;
            var8 += var13 * var15;
            float var16 = (float)Math.pow(Math.sin(Math.PI * var11), 2.0);
            var4[var10] += var8 * var16 * var3;
         }

         return var4;
      }

      private static float[] add(float[] var0, float[] var1) {
         float[] var2 = new float[Math.max(var0.length, var1.length)];

         for (int var3 = 0; var3 < var0.length; var3++) {
            var2[var3] += var0[var3];
         }

         for (int var4 = 0; var4 < var1.length; var4++) {
            var2[var4] += var1[var4];
         }

         return var2;
      }

      private static float[] finish(float[] var0, float var1) {
         float var2 = 1.0E-6F;

         for (float var6 : var0) {
            var2 = Math.max(var2, Math.abs(var6));
         }

         float var13 = var1 / var2;
         int var14 = Math.round(30870.0F);
         int var15 = var0.length + var14;
         float[] var16 = new float[var15];

         for (int var7 = 0; var7 < var0.length; var7++) {
            var16[var7] = var0[var7] * var13;
         }

         float[] var17 = room(var16, new int[]{1116, 1188, 1277, 1356}, new int[]{556, 441});
         float[] var8 = room(var16, new int[]{1139, 1211, 1300, 1379}, new int[]{579, 464});
         float[] var9 = new float[var15 * 2];
         int var10 = Math.round(3528.0F);

         for (int var11 = 0; var11 < var15; var11++) {
            float var12 = var11 > var15 - var10 ? (float)(var15 - var11) / var10 : 1.0F;
            var9[var11 * 2] = (var16[var11] * 0.86F + var17[var11] * 0.22F) * var12;
            var9[var11 * 2 + 1] = (var16[var11] * 0.86F + var8[var11] * 0.22F) * var12;
         }

         return var9;
      }

      private static float[] room(float[] var0, int[] var1, int[] var2) {
         float[] var3 = new float[var0.length];

         for (int var7 : var1) {
            float[] var8 = new float[var7];
            int var9 = 0;
            float var10 = 0.0F;

            for (int var11 = 0; var11 < var0.length; var11++) {
               float var12 = var8[var9];
               var10 = var12 * 0.62F + var10 * 0.38F;
               var8[var9] = var0[var11] + var10 * 0.74F;
               var9 = (var9 + 1) % var7;
               var3[var11] += var12 * 0.25F;
            }
         }

         for (int var16 : var2) {
            float[] var17 = new float[var16];
            int var18 = 0;

            for (int var19 = 0; var19 < var3.length; var19++) {
               float var20 = var17[var18];
               float var21 = var3[var19];
               var17[var18] = var21 + var20 * 0.5F;
               var3[var19] = var20 - var21;
               var18 = (var18 + 1) % var16;
            }
         }

         return var3;
      }
   }
}

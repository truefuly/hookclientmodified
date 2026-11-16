package hook.feature;

import net.minecraft.class_310;
import org.lwjgl.opengl.GL11;

/** Dev harness (-Dhook.astest): opens the client GUI, turns Anti Screenshare on and off and prints markers for capture. */
public final class AntiScreenshareTest {
    public static final boolean ENABLED = System.getProperty("hook.astest") != null;
    private static int ticks = -1;
    private static long lastProbe;
    private static int frames;
    private static long measureStart;

    /** Counted once per real frame (from the render mixin). */
    public static void frame() { frames++; }
    private static int cleans, frees;
    private static int guiOnly;
    public static void cleanPass(boolean hudOnly) { cleans++; if (hudOnly) guiOnly++; }
    public static void freeFrame() { frees++; }

    private static void begin() { frames = 0; cleans = 0; frees = 0; guiOnly = 0; measureStart = System.nanoTime(); }

    private static void report(String label) {
        double sec = (System.nanoTime() - measureStart) / 1.0e9;
        System.out.println("ASTEST FPS " + label + " = " + Math.round(frames / sec) + " frames=" + frames + " cleanPasses=" + cleans + " hudOnly=" + guiOnly + " alreadyClean=" + frees);
    }

    public static void tick() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) return;
        ticks++;
        AntiScreenshareModule m = AntiScreenshareModule.instance;
        if (m == null) return;
        if (ticks == 60) {
            if (!m.isEnabled()) m.toggle();
            if (hook.Hook.INSTANCE.GUI != null) mc.method_1507(hook.Hook.INSTANCE.GUI);
        }
        if (ticks == 89) m.captureFps.getValue(10.0);
        if (ticks == 90) begin();
        if (ticks == 150) report("on capture=" + Math.round(m.captureFps.getValue()));
        if (ticks == 160) System.out.println("ASTEST READY_ON status=" + AntiScreenshareModule.status);
        if (ticks == 170) m.captureFps.getValue(240.0);
        if (ticks == 190) begin();
        if (ticks == 250) { report("on capture=every-frame"); m.captureFps.getValue(30.0); mc.method_1507(null); }
        if (ticks == 252) begin();
        if (ticks == 259) report("on no-gui");
        if (ticks == 260 && m.isEnabled()) m.toggle();
        if (ticks == 265) begin();
        if (ticks == 315) report("off");
        if (ticks == 320) System.out.println("ASTEST READY_OFF status=" + AntiScreenshareModule.status);
        if (ticks == 400) { System.out.println("ASTEST DONE"); mc.method_1592(); }
    }

    /** Reads a few pixels of what the overlay shows (to prove it gets the full frame). */
    static void overlayProbe(int w, int h) {
        long now = System.currentTimeMillis();
        if (now - lastProbe < 2000) return;
        lastProbe = now;
        java.nio.ByteBuffer buf = org.lwjgl.BufferUtils.createByteBuffer(4 * 16);
        GL11.glReadPixels(w / 2 - 2, h / 2 - 2, 4, 4, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        long sum = 0;
        for (int i = 0; i < 64; i++) if (i % 4 != 3) sum += buf.get(i) & 0xFF;
        System.out.println("ASTEST overlay " + w + "x" + h + " avg=" + (sum / 48) + " err=" + GL11.glGetError());
    }
}

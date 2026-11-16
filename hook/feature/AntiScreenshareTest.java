package hook.feature;

import net.minecraft.class_310;
import org.lwjgl.opengl.GL11;

/** Dev harness (-Dhook.astest): opens the client GUI, turns Anti Screenshare on and off and prints markers for capture. */
public final class AntiScreenshareTest {
    public static final boolean ENABLED = System.getProperty("hook.astest") != null;
    private static int ticks = -1;
    private static long lastProbe;

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
        if (ticks == 160) System.out.println("ASTEST READY_ON status=" + AntiScreenshareModule.status);
        if (ticks == 260 && m.isEnabled()) m.toggle();
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

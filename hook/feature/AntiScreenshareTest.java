package hook.feature;

import net.minecraft.class_310;
import org.lwjgl.opengl.GL11;

/** Dev harness (-Dhook.astest): opens the client GUI, turns Anti Screenshare on and off and prints markers for capture. */
public final class AntiScreenshareTest {
    public static final boolean ENABLED = System.getProperty("hook.astest") != null;
    static final boolean OBS = "obs".equals(System.getProperty("hook.astest"));
    /** Test only: old double-buffered overlay, to compare. */
    static final boolean FORCE_DOUBLE = System.getProperty("hook.asdouble") != null;
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
        hook.module.ModuleManager mm = hook.Hook.INSTANCE.getModuleManager();
        hook.module.Module hud = mm.getModuleByClass(HudModule.class), esp = mm.getModuleByClass(EntityEspModule.class);
        if (OBS) {
            try { org.lwjgl.glfw.GLFW.glfwSetWindowTitle(mc.method_22683().method_4490(), "HookASTest"); } catch (Throwable ignored) {}
            if (ticks == 40) System.out.println("ASTEST OBS_START");
            if (ticks == 60) {
                if (!m.isEnabled()) m.toggle();
                if (hud != null && !hud.isEnabled()) hud.toggle();
                if (hook.Hook.INSTANCE.GUI != null) mc.method_1507(hook.Hook.INSTANCE.GUI);
            }
            if (ticks == 120) System.out.println("ASTEST OBS_PHASE gui");
            if (ticks == 320) { mc.method_1507(null); }
            if (ticks == 330) System.out.println("ASTEST OBS_PHASE hud");
            if (ticks == 520 && esp != null && !esp.isEnabled()) esp.toggle();
            if (ticks == 530) System.out.println("ASTEST OBS_PHASE esp");
            if (ticks == 730) { System.out.println("ASTEST OBS_PHASE off"); if (m.isEnabled()) m.toggle(); }
            if (ticks == 830) { System.out.println("ASTEST DONE"); mc.method_1592(); }
            return;
        }
        if (ticks == 60) {
            if (!m.isEnabled()) m.toggle();
            if (hud != null && !hud.isEnabled()) hud.toggle();
            if (hook.Hook.INSTANCE.GUI != null) mc.method_1507(hook.Hook.INSTANCE.GUI);
        }
        if (ticks == 90) begin();
        if (ticks == 100) { report("gui"); burst("REC_GUI"); }
        if (ticks == 185) AntiScreenshareModule.testVisible(true);
        if (ticks == 190) { begin(); burst("USER_GUI"); }
        if (ticks == 275) { report("user-gui"); AntiScreenshareModule.testVisible(false); mc.method_1507(null); }
        if (ticks == 290) begin();
        if (ticks == 300) { report("play-hud"); burst("REC_HUD"); }
        if (ticks == 385) AntiScreenshareModule.testVisible(true);
        if (ticks == 390) { begin(); burst("USER_HUD"); }
        if (ticks == 470) { report("user-hud"); AntiScreenshareModule.testVisible(false); if (esp != null && !esp.isEnabled()) esp.toggle(); }
        if (ticks == 480) begin();
        if (ticks == 490) { report("play-esp"); burst("REC_ESP"); }
        if (ticks == 575) AntiScreenshareModule.testVisible(true);
        if (ticks == 580) { begin(); burst("USER_ESP"); }
        if (ticks == 665) { report("user-esp"); AntiScreenshareModule.testVisible(false); if (m.isEnabled()) m.toggle(); }
        if (ticks == 680) { System.out.println("ASTEST DONE"); mc.method_1592(); }
    }

    private static void burst(String name) {
        System.out.println("ASTEST BURST " + name + " status=" + AntiScreenshareModule.status);
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

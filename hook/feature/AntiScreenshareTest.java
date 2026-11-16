package hook.feature;

import net.minecraft.class_310;
import org.lwjgl.opengl.GL11;

/** Dev harness (-Dhook.astest): opens the client GUI, turns Anti Screenshare on and off and prints markers for capture. */
public final class AntiScreenshareTest {
    public static final boolean ENABLED = System.getProperty("hook.astest") != null;
    static final boolean OBS = "obs".equals(System.getProperty("hook.astest"));
    static final boolean QUICK = "quick".equals(System.getProperty("hook.astest"));
    static final boolean BENCH = "bench".equals(System.getProperty("hook.astest"));
    private static long nsSum, nsMax;
    /** Test only: client 3D drawing also goes through the client layer (to check the layer path). */
    public static final boolean LAYER3D = System.getProperty("hook.aslayer3d") != null;
    private static String dumpName;
    public static int r3d;
    private static final java.util.Set<String> dirty = new java.util.TreeSet<>();
    public static void dirtyBy(String module) { dirty.add(module); }
    /** Time of one whole frame draw (both passes) in ns. */
    public static void frameTime(long ns) { nsSum += ns; nsMax = Math.max(nsMax, ns); }
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

    private static void begin() { dirty.clear(); nsSum = 0; nsMax = 0; frames = 0; cleans = 0; frees = 0; guiOnly = 0; measureStart = System.nanoTime(); }

    private static void report(String label) {
        double sec = (System.nanoTime() - measureStart) / 1.0e9;
        System.out.println("ASTEST FPS " + label + " = " + Math.round(frames / sec) + " avgMs=" + String.format("%.2f", nsSum / 1e6 / Math.max(1, frames)) + " maxMs=" + String.format("%.2f", nsMax / 1e6) + " frames=" + frames + " cleanPasses=" + cleans + " hudOnly=" + guiOnly + " alreadyClean=" + frees + " worldDirtyBy=" + dirty);
        dirty.clear();
    }

    public static void tick() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) return;
        ticks++;
        AntiScreenshareModule m = AntiScreenshareModule.instance;
        if (m == null) return;
        hook.module.ModuleManager mm = hook.Hook.INSTANCE.getModuleManager();
        hook.module.Module hud = mm.getModuleByClass(HudModule.class), esp = mm.getModuleByClass(EntityEspModule.class);
        if (BENCH) {
            hook.module.Module hb = mm.getModuleByClass(CleanHitboxesModule.class), cb = mm.getModuleByClass(ChunkBordersModule.class);
            if (mc.field_1755 instanceof net.minecraft.class_433) mc.method_1507(null);
            if (ticks == 40 && mc.method_1562() != null) {
                for (String c : new String[]{"gamemode creative", "time set day", "weather clear", "kill @e[type=!player]", "tp @s 0.5 120 0.5 0 20"}) mc.method_1562().method_45730(c);
                for (int i = 0; i < 6; i++) mc.method_1562().method_45730("summon zombie " + (i * 2 - 5) + ".5 120 " + (6 + i % 3) + ".5 {NoAI:1b,Silent:1b,NoGravity:1b,PersistenceRequired:1b}");
            }
            if (ticks == 60) {
                if (m.isEnabled()) m.toggle();
                if (esp != null && !esp.isEnabled()) esp.toggle();
                if (hb != null && !hb.isEnabled()) hb.toggle();
                if (cb != null && !cb.isEnabled()) cb.toggle();
                if (hud != null && !hud.isEnabled()) hud.toggle();
            }
            if (ticks == 100) begin();
            if (ticks % 100 == 0) {
                class_310 c = mc;
                System.out.println("ASTEST STATE t=" + ticks + " esp=" + (esp != null && esp.isEnabled()) + " cb=" + (cb != null && cb.isEnabled()) + " hud=" + (hud != null && hud.isEnabled())
                    + " as=" + m.isEnabled() + " clean=" + AntiScreenshareModule.CLEAN_PASS + " full=" + AntiScreenshareModule.FULL_PASS + " screen=" + c.field_1755 + " r3d=" + r3d + " status=" + AntiScreenshareModule.status);
                r3d = 0;
            }
            if (ticks == 260) { report("off-visuals"); burst("REC_OFF"); }
            if (ticks == 280) { if (!m.isEnabled()) m.toggle(); }
            if (ticks == 320) begin();
            if (ticks == 480) { report("on-visuals-cap30"); burst("REC_ON"); }
            if (ticks == 500) { m.captureFps.getValue(240.0); begin(); }
            if (ticks == 660) { report("on-visuals-every"); m.captureFps.getValue(30.0); burst("REC_ON_EVERY"); }
            if (ticks == 680) { if (esp != null) esp.setEnabled(false); if (hb != null) hb.setEnabled(false); if (cb != null) cb.setEnabled(false); }
            if (ticks == 700) begin();
            if (ticks == 860) { report("on-novisuals"); burst("REC_ON_PLAIN"); }
            if (ticks == 880) { if (m.isEnabled()) m.toggle(); begin(); }
            if (ticks == 1040) report("off-novisuals");
            if (ticks == 1060) { System.out.println("ASTEST DONE"); mc.method_1592(); }
            return;
        }
        if (QUICK) {
            QuickUtilitiesModule q = QuickUtilitiesModule.instance;
            if (ticks == 240 && q != null) {
                q.releaseToSelect.setValue(false);
                QuickWheelScreen.testHover = -1;
                mc.method_1507(new QuickWheelScreen());
            }
            if (ticks == 260) shot(mc, "wheel_idle");
            if (ticks == 280) QuickWheelScreen.testHover = 0;
            if (ticks == 300) shot(mc, "wheel_calc");
            if (ticks == 320) QuickWheelScreen.testHover = 3;
            if (ticks == 340) shot(mc, "wheel_empty");
            if (ticks == 360) { QuickWheelScreen.testHover = -1; mc.method_1507(new CalculatorScreen()); }
            if (ticks == 380) shot(mc, "calc_empty");
            if (ticks == 390 && mc.field_1755 instanceof CalculatorScreen c) { for (String k : new String[]{"1","2","*","(","3","+","4",")","="}) c.press(k); for (String k : new String[]{"/","8"}) c.press(k); }
            if (ticks == 410) shot(mc, "calc_result");
            if (ticks == 440) { System.out.println("ASTEST DONE"); mc.method_1592(); }
            return;
        }
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

    private static void shot(class_310 mc, String name) {
        net.minecraft.class_318.method_22690(mc.field_1697, "q_" + name + ".png", mc.method_1522(), 1, msg -> {});
        System.out.println("ASTEST SHOT " + name);
    }

    private static void burst(String name) {
        dumpName = name;
        System.out.println("ASTEST BURST " + name + " status=" + AntiScreenshareModule.status);
    }

    /** Reads a few pixels of what the overlay shows (to prove it gets the full frame). */
    static void overlayProbe(int w, int h) {
        if (dumpName != null) {
            String n = dumpName;
            dumpName = null;
            java.nio.ByteBuffer all = org.lwjgl.BufferUtils.createByteBuffer(w * h * 4);
            GL11.glReadPixels(0, 0, w, h, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, all);
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
                int i = ((h - 1 - y) * w + x) * 4;
                img.setRGB(x, y, ((all.get(i) & 255) << 16) | ((all.get(i + 1) & 255) << 8) | (all.get(i + 2) & 255));
            }
            try { javax.imageio.ImageIO.write(img, "png", new java.io.File("overlay_" + n + ".png")); } catch (Exception ignored) {}
            System.out.println("ASTEST OVERLAYDUMP " + n);
        }
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

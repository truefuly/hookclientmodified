package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.utils.EncryptedString;
import net.minecraft.class_310;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.JNI;
import org.lwjgl.system.windows.User32;

/**
 * Anti Screenshare. Every frame is drawn twice:
 *  1. the normal frame (with everything from the client) goes into a second window that sits exactly on top of the
 *     game window and is excluded from screen capture (Windows "WDA_EXCLUDEFROMCAPTURE") - that is what you see;
 *  2. then the frame is drawn again with every client visual switched off, and that clean frame goes to the real
 *     game window - that is what OBS, Discord, screenshots and screen shares see.
 * Windows 10 (2004) or newer, windowed / borderless only. Costs about half the FPS while on.
 */
public final class AntiScreenshareModule extends Module {
    static AntiScreenshareModule instance;
    /** True while the clean (capture) frame is being drawn: client visuals must stay off. */
    public static volatile boolean CLEAN_PASS = false;
    /** True while the normal frame is drawn with the module on; CLIENT_DREW records whether anything of the client showed up in it. */
    public static volatile boolean FULL_PASS = false, CLIENT_DREW = false;
    /** Clean pass that reuses the world image of the normal frame and only redraws the HUD / screens. */
    public static volatile boolean GUI_ONLY = false;
    /** Set when the world image of this frame was saved and whether the client drew into the world (ESP...). */
    public static boolean WORLD_SNAP = false, WORLD_DIRTY = false;
    private com.mojang.blaze3d.textures.GpuTexture worldCache;

    private static final int WDA_NONE = 0, WDA_EXCLUDEFROMCAPTURE = 0x11;
    private static final int GWL_EXSTYLE = -20, GWLP_HWNDPARENT = -8;
    private static final long WS_EX_NOACTIVATE = 0x08000000L, WS_EX_TOOLWINDOW = 0x80L, WS_EX_LAYERED = 0x80000L, WS_EX_TRANSPARENT = 0x20L;

    private long overlay;
    private org.lwjgl.opengl.GLCapabilities overlayCaps;
    private long overlayHwnd;
    private int readFbo;
    private boolean excluded;
    private int ox = Integer.MIN_VALUE, oy, ow, oh;
    private boolean failed;
    public static String status = "off";

    final hook.module.setting.NumberSetting captureFps = new hook.module.setting.NumberSetting(EncryptedString.of("Capture FPS"), 10.0, 240.0, 30.0, 5.0);
    /** Last clean frame, shown to recorders on frames where no new clean frame is drawn. */
    private com.mojang.blaze3d.textures.GpuTexture cleanCache;
    private long lastClean;

    public AntiScreenshareModule() {
        super(EncryptedString.of("Anti Screenshare"), EncryptedString.of("Screen recordings, screenshots and Discord/OBS shares only see the plain game - no client GUI, HUD, ESP or nametags. You still see everything. Windowed/borderless, Windows 10 2004+. Capture FPS keeps the cost low."), -1, Category.MISC);
        this.captureFps.setDescription(EncryptedString.of("How often the clean frame for recordings is drawn per second. Lower = more FPS for you. Discord streams at 30, OBS usually 60. 240 = every frame"));
        this.addSettings(this.captureFps);
        instance = this;
    }

    /** True when this frame needs a fresh clean frame; otherwise the cached one is put back into the game window. */
    public static boolean wantsCleanFrame(int w, int h) {
        AntiScreenshareModule m = instance;
        if (m == null) return true;
        double fps = m.captureFps.getValue();
        if (fps >= 240.0) return true;
        var c = m.cleanCache;
        if (c == null || c.isClosed() || c.getWidth(0) != w || c.getHeight(0) != h) return true;
        return System.nanoTime() - m.lastClean >= (long) (1.0e9 / Math.max(1.0, fps));
    }

    /** Keeps a copy of the clean frame that was just drawn. */
    public static void storeClean(com.mojang.blaze3d.textures.GpuTexture main, int w, int h) {
        AntiScreenshareModule m = instance;
        if (m == null) return;
        // steady pacing: next capture frame is due one interval after the last one, without drifting
        long now = System.nanoTime(), iv = (long) (1.0e9 / Math.max(1.0, m.captureFps.getValue()));
        m.lastClean = now - m.lastClean > 2 * iv ? now : m.lastClean + iv;
        if (m.captureFps.getValue() >= 240.0) { m.freeCache(); return; }
        var dev = com.mojang.blaze3d.systems.RenderSystem.getDevice();
        var c = m.cleanCache;
        if (c == null || c.isClosed() || c.getWidth(0) != w || c.getHeight(0) != h) {
            m.freeCache();
            c = m.cleanCache = dev.createTexture(() -> "Hook AntiScreenshare clean frame",
                com.mojang.blaze3d.textures.GpuTexture.USAGE_COPY_DST | com.mojang.blaze3d.textures.GpuTexture.USAGE_COPY_SRC,
                com.mojang.blaze3d.textures.TextureFormat.RGBA8, w, h, 1, 1);
        }
        dev.createCommandEncoder().copyTextureToTexture(main, c, 0, 0, 0, 0, 0, w, h);
    }

    /** Puts the last clean frame back into the main target so the game window (what recorders see) stays clean. */
    public static void restoreClean(com.mojang.blaze3d.textures.GpuTexture main, int w, int h) {
        AntiScreenshareModule m = instance;
        if (m == null || m.cleanCache == null) return;
        com.mojang.blaze3d.systems.RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(m.cleanCache, main, 0, 0, 0, 0, 0, w, h);
    }

    /** Called right after the world (before the HUD) was drawn in the normal frame. */
    public static void afterWorld(net.minecraft.class_276 target) {
        AntiScreenshareModule m = instance;
        if (m == null || !FULL_PASS) return;
        WORLD_DIRTY = CLIENT_DREW;
        if (WORLD_DIRTY) return; // ESP & co. are in the world image, the clean frame needs the full world again
        int w = target.field_1482, h = target.field_1481;
        if (!wantsCleanFrame(w, h)) return;
        var main = target.method_30277();
        if (main == null) return;
        var dev = com.mojang.blaze3d.systems.RenderSystem.getDevice();
        var c = m.worldCache;
        if (c == null || c.isClosed() || c.getWidth(0) != w || c.getHeight(0) != h) {
            if (c != null) try { c.close(); } catch (Throwable ignored) {}
            c = m.worldCache = dev.createTexture(() -> "Hook AntiScreenshare world",
                com.mojang.blaze3d.textures.GpuTexture.USAGE_COPY_DST | com.mojang.blaze3d.textures.GpuTexture.USAGE_COPY_SRC,
                com.mojang.blaze3d.textures.TextureFormat.RGBA8, w, h, 1, 1);
        }
        dev.createCommandEncoder().copyTextureToTexture(main, c, 0, 0, 0, 0, 0, w, h);
        WORLD_SNAP = true;
    }

    /** Puts the saved world image back so the clean pass only has to draw the HUD on top. */
    public static boolean restoreWorld(com.mojang.blaze3d.textures.GpuTexture main, int w, int h) {
        AntiScreenshareModule m = instance;
        if (m == null || m.worldCache == null || m.worldCache.isClosed()) return false;
        com.mojang.blaze3d.systems.RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(m.worldCache, main, 0, 0, 0, 0, 0, w, h);
        return true;
    }

    private void freeCache() {
        if (this.worldCache != null) {
            try { this.worldCache.close(); } catch (Throwable ignored) {}
            this.worldCache = null;
        }
        if (this.cleanCache != null) {
            try { this.cleanCache.close(); } catch (Throwable ignored) {}
            this.cleanCache = null;
        }
    }

    public static boolean active() {
        AntiScreenshareModule m = instance;
        return m != null && m.isEnabled() && !m.failed;
    }

    @Override
    public void onEnable() {
        this.failed = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        // the window is destroyed on the render thread at the next frame
        super.onDisable();
    }

    /** Called on the render thread right after the normal frame was drawn into the main render target. */
    public static void afterFullFrame(int colorTexId, int texW, int texH) {
        AntiScreenshareModule m = instance;
        if (m == null) return;
        try {
            if (!m.isEnabled() || m.failed) { m.destroy(); return; }
            m.present(colorTexId, texW, texH);
        } catch (Throwable t) {
            m.failed = true;
            status = "failed: " + t;
            System.err.println("[AntiScreenshare] " + t);
            try { m.destroy(); } catch (Throwable ignored) {}
        }
    }

    /** Called every frame when the module is off, to clean up. */
    public static void idle() {
        AntiScreenshareModule m = instance;
        if (m != null && m.overlay != 0L && (!m.isEnabled() || m.failed)) m.destroy();
    }

    private long mainHandle() {
        return class_310.method_1551().method_22683().method_4490();
    }

    private void create() {
        long main = this.mainHandle();
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUSED, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUS_ON_SHOW, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_MOUSE_PASSTHROUGH, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);
        int[] w = new int[1], h = new int[1];
        GLFW.glfwGetWindowSize(main, w, h);
        this.overlay = GLFW.glfwCreateWindow(Math.max(1, w[0]), Math.max(1, h[0]), "HookOverlay", 0L, main);
        GLFW.glfwDefaultWindowHints();
        if (this.overlay == 0L) throw new IllegalStateException("overlay window could not be created");
        this.overlayHwnd = GLFWNativeWin32.glfwGetWin32Window(this.overlay);
        long mainHwnd = GLFWNativeWin32.glfwGetWin32Window(main);
        // owned by the game window: stays right above it, minimises with it, never shows in the taskbar or takes focus
        User32.SetWindowLongPtr(this.overlayHwnd, GWLP_HWNDPARENT, mainHwnd);
        long ex = User32.GetWindowLongPtr(this.overlayHwnd, GWL_EXSTYLE);
        User32.SetWindowLongPtr(this.overlayHwnd, GWL_EXSTYLE, ex | WS_EX_NOACTIVATE | WS_EX_TOOLWINDOW | WS_EX_LAYERED | WS_EX_TRANSPARENT);
        User32.SetLayeredWindowAttributes(this.overlayHwnd, 0, (byte) 255, User32.LWA_ALPHA);
        this.excluded = setAffinity(this.overlayHwnd, WDA_EXCLUDEFROMCAPTURE);
        if (!this.excluded) throw new IllegalStateException("Windows refused to hide the overlay from capture (needs Windows 10 2004+)");
        long prev = GLFW.glfwGetCurrentContext();
        org.lwjgl.opengl.GLCapabilities mainCaps = org.lwjgl.opengl.GL.getCapabilities();
        GLFW.glfwMakeContextCurrent(this.overlay);
        this.overlayCaps = org.lwjgl.opengl.GL.createCapabilities();
        GLFW.glfwSwapInterval(0);
        this.readFbo = GL30.glGenFramebuffers();
        GLFW.glfwMakeContextCurrent(prev);
        org.lwjgl.opengl.GL.setCapabilities(mainCaps);
        this.ox = Integer.MIN_VALUE;
        status = "on";
    }

    private static boolean setAffinity(long hwnd, int affinity) {
        long fn = User32.getLibrary().getFunctionAddress("SetWindowDisplayAffinity");
        if (fn == 0L) return false;
        return JNI.callPI(hwnd, affinity, fn) != 0;
    }

    private void present(int tex, int texW, int texH) {
        long main = this.mainHandle();
        if (GLFW.glfwGetWindowAttrib(main, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE) {
            if (this.overlay != 0L) GLFW.glfwHideWindow(this.overlay);
            return;
        }
        if (this.overlay == 0L) this.create();
        // follow the game window's client area exactly
        int[] x = new int[1], y = new int[1], w = new int[1], h = new int[1];
        GLFW.glfwGetWindowPos(main, x, y);
        GLFW.glfwGetWindowSize(main, w, h);
        if (x[0] != this.ox || y[0] != this.oy || w[0] != this.ow || h[0] != this.oh) {
            this.ox = x[0]; this.oy = y[0]; this.ow = w[0]; this.oh = h[0];
            GLFW.glfwSetWindowPos(this.overlay, x[0], y[0]);
            GLFW.glfwSetWindowSize(this.overlay, Math.max(1, w[0]), Math.max(1, h[0]));
        }
        if (GLFW.glfwGetWindowAttrib(this.overlay, GLFW.GLFW_VISIBLE) == GLFW.GLFW_FALSE) GLFW.glfwShowWindow(this.overlay);

        // make sure the game's GL work on the frame is submitted before the other context reads it
        long fence = GL32.glFenceSync(GL32.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
        GL11.glFlush();
        long prev = GLFW.glfwGetCurrentContext();
        org.lwjgl.opengl.GLCapabilities mainCaps = org.lwjgl.opengl.GL.getCapabilities();
        GLFW.glfwMakeContextCurrent(this.overlay);
        org.lwjgl.opengl.GL.setCapabilities(this.overlayCaps);
        long back = 0L;
        try {
            GL32.glWaitSync(fence, 0, GL32.GL_TIMEOUT_IGNORED);
            int[] fw = new int[1], fh = new int[1];
            GLFW.glfwGetFramebufferSize(this.overlay, fw, fh);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, this.readFbo);
            GL30.glFramebufferTexture2D(GL30.GL_READ_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, tex, 0);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
            GL11.glViewport(0, 0, fw[0], fh[0]);
            GL30.glBlitFramebuffer(0, 0, texW, texH, 0, 0, fw[0], fh[0], GL11.GL_COLOR_BUFFER_BIT, texW == fw[0] && texH == fh[0] ? GL11.GL_NEAREST : GL11.GL_LINEAR);
            if (AntiScreenshareTest.ENABLED) AntiScreenshareTest.overlayProbe(fw[0], fh[0]);
            GL30.glFramebufferTexture2D(GL30.GL_READ_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, 0, 0);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
            back = GL32.glFenceSync(GL32.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
            GL11.glFlush();
            GLFW.glfwSwapBuffers(this.overlay);
        } finally {
            GLFW.glfwMakeContextCurrent(prev);
            org.lwjgl.opengl.GL.setCapabilities(mainCaps);
            GL32.glDeleteSync(fence);
            // the game must not overwrite the frame before the overlay has read it
            if (back != 0L) { GL32.glWaitSync(back, 0, GL32.GL_TIMEOUT_IGNORED); GL32.glDeleteSync(back); }
        }
    }

    private void destroy() {
        this.freeCache();
        if (this.overlay == 0L) return;
        long prev = GLFW.glfwGetCurrentContext();
        org.lwjgl.opengl.GLCapabilities mainCaps = org.lwjgl.opengl.GL.getCapabilities();
        try {
            GLFW.glfwMakeContextCurrent(this.overlay);
            if (this.overlayCaps != null) org.lwjgl.opengl.GL.setCapabilities(this.overlayCaps);
            if (this.readFbo != 0) GL30.glDeleteFramebuffers(this.readFbo);
        } catch (Throwable ignored) {
        } finally {
            GLFW.glfwMakeContextCurrent(prev);
            org.lwjgl.opengl.GL.setCapabilities(mainCaps);
        }
        GLFW.glfwDestroyWindow(this.overlay);
        this.overlay = 0L;
        this.overlayHwnd = 0L;
        this.readFbo = 0;
        this.overlayCaps = null;
        status = "off";
    }
}

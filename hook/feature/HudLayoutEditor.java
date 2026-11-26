/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.feature.UiEasing;
import hook.feature.UiIcon;
import hook.feature.UiPalette;
import hook.feature.UiRenderer;
import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HudLayoutEditor {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");
    private static final float EDGE = 10.0f;
    private static final float STACK_GAP = 7.0f;
    private static final float ROW_H = 22.0f;
    private static final float ROW_GAP = 4.0f;
    private final Map<HudSection, adsdzhecwnzmbs> places = new EnumMap<HudSection, adsdzhecwnzmbs>(HudSection.class);
    private final Map<HudSection, float[]> bounds = new EnumMap<HudSection, float[]>(HudSection.class);
    private final Map<String, aemkvoltyxggkb> rows = new LinkedHashMap<String, aemkvoltyxggkb>();
    private final Map<String, Float> widthCache = new LinkedHashMap<String, Float>(512, 0.75f, true);
    private List<String> moduleNames = List.of();
    private List<String> sortedModuleNames = List.of();
    private float sortedAtSize = -1.0f;
    private final ArrayList<String> removedRows = new ArrayList();
    private float u = 1.0f;
    private int lastSw = 1920;
    private float dt;
    private long last;
    private float time;
    private boolean loaded;
    private HudSection dragging;
    private double grabX;
    private double grabY;
    private Path file;
    private adhvjlahyjurfh lastOptions = new adhvjlahyjurfh();
    private final Map<String, float[]> anims = new HashMap<String, float[]>();

    public void setFile(Path var1) {
        this.file = var1;
    }

    public void render(UiRenderer var1, int var2, int var3, HudDataSource var4, adhvjlahyjurfh var5, float var6, boolean var7, double var8, double var10) {
        long var12 = System.nanoTime();
        this.dt = this.last == 0L ? 0.016f : Math.min(0.1f, (float)(var12 - this.last) / 1.0E9f);
        this.last = var12;
        this.time += this.dt;
        this.load();
        this.lastOptions = var5;
        this.u = Math.max(0.5f, (float)var3 / 1080.0f * var5.scale);
        this.lastSw = var2;
        this.lastSh = var3;
        this.snappedX = false;
        this.snappedY = false;
        if (this.dragging != null) {
            adsdzhecwnzmbs var14 = this.places.computeIfAbsent(this.dragging, var0 -> new adsdzhecwnzmbs());
            float[] var15 = this.bounds.get((Object)this.dragging);
            if (var15 != null) {
                float nx = this.snapX((float)(var8 - this.grabX), var15[2], var2);
                float ny = this.snapY((float)(var10 - this.grabY), var15[3], var3);
                this.placeFromPixel(var14, nx, ny, var15[2], var15[3], var2, var3);
            }
        }
        if (this.resizingSection != null || this.resizingPart != null) this.updateResize(var8, var10, var2, var3);
        if (var7) {
            this.drawGrid(var1, var2, var3, var6);
        }
        var1.pushAlpha(var6);
        float var16 = this.d(10.0f);
        var16 = this.element(var1, HudSection.WATERMARK, var5.watermark, var2, var3, var16, var7, var8, var10, () -> this.watermarkSize(var1, var5), (var3x, var4x) -> this.drawWatermark(var1, var3x, var4x, var5));
        var16 = this.element(var1, HudSection.INFO, var5.info && var4.inWorld(), var2, var3, var16, var7, var8, var10, () -> this.infoSize(var1, var4), (var3x, var4x) -> this.drawInfo(var1, var3x, var4x, var4));
        var16 = this.element(var1, HudSection.PLAYER, var5.player && var4.inWorld(), var2, var3, var16, var7, var8, var10, () -> this.playerSize(var1, var4), (var3x, var4x) -> this.drawPlayer(var1, var3x, var4x, var4));
        this.element(var1, HudSection.COORDS, var5.coords && var4.inWorld(), var2, var3, var16, var7, var8, var10, () -> this.coordsSize(var1, var4, var5), (var4x, var5x) -> this.drawCoords(var1, var4x, var5x, var4, var5));
        this.drawModules(var1, var5.modules, var2, var3, var4, var7, var8, var10);
        List<EffectRow> fx = this.effectRows(var4, var7);
        this.element(var1, HudSection.EFFECT_TIMER, var5.effectTimer && !fx.isEmpty() && var4.inWorld(), var2, var3, 0.0f, var7, var8, var10, () -> this.effectsSize(var1, fx, var5), (ex, ey) -> this.drawEffects(var1, ex, ey, fx, var5));
        this.musicBar(var1, var2, var3, var7, var8, var10);
        SchematicBuilderModule.Progress bp = SchematicBuilderModule.progress();
        if (bp == null && var7) bp = new SchematicBuilderModule.Progress("Pumpkin Farm", 1384, 2210, 62.6, "~4m 10s left \u00B7 done at 19:42", "Building", false);
        final SchematicBuilderModule.Progress fbp = bp;
        this.element(var1, HudSection.BUILDER, fbp != null && var4.inWorld(), var2, var3, 0.0f, var7, var8, var10, () -> this.builderSize(var1, fbp), (bx, by) -> this.drawBuilder(var1, bx, by, fbp, var5));
        if (this.draggingPart != null) {
            float g = guiScale();
            float[] pr = this.partRect(this.draggingPart);
            float nx = (float)(var8 - this.grabX), ny = (float)(var10 - this.grabY);
            if (pr != null) {
                nx = this.snapX(nx, pr[2], var2);
                ny = this.snapY(ny, pr[3], var3);
            }
            VanillaHud.move(this.draggingPart, nx / g, ny / g);
        }
        if (var7) {
            this.drawSnapGuides(var1, var2, var3);
        }
        if (var7) {
            this.drawVanillaFrames(var1, var8, var10);
            this.drawResetButton(var1, var2, var8, var10);
        } else if (chatOpen) {
            this.drawEditorButton(var1, var2, var3, var8, var10);
        } else {
            this.editorButton[2] = 0.0f;
        }
        var1.popAlpha();
    }

    // ------------------------------------------------------------------ vanilla parts + reset button

    private VanillaHud.Part draggingPart;
    private final float[] resetBounds = new float[4];

    // ------------------------------------------------------------------ editor mode (button in the chat) + corner resize handles

    /** True while the HUD editor is on (chat open and the "HUD Editor" button was clicked). */
    public static volatile boolean editing;
    /** Set by HudModule every frame: the chat screen is open. */
    public static volatile boolean chatOpen;
    private final float[] editorButton = new float[4];
    private HudSection resizingSection;
    private VanillaHud.Part resizingPart;
    private float rsX, rsY, rsW, rsH, rsScale;

    /** "HUD Editor" button in the middle of the screen while the chat is open. */
    private void drawEditorButton(UiRenderer ui, int sw, int sh, double mx, double my) {
        String text = "HUD Editor";
        // never tiny on small windows; sits just under the crosshair so it is not covered by it
        float k = Math.max(1.0f, this.u) * 1.3f;
        float th = 12.0f * k;
        float w = this.textW(ui, text, th, 1) + 44.0f * k;
        float h = 30.0f * k;
        float x = sw / 2.0f - w / 2.0f, y = sh / 2.0f + 14.0f * k;
        boolean hot = mx >= x && mx < x + w && my >= y && my < y + h;
        float a = this.anim("editorbtn", hot ? 1.0f : 0.0f, 14.0f);
        ui.round(x, y, w, h, h / 2.0f, UiEasing.mix(UiRenderer.argb(0.88f, 20, 21, 25), UiPalette.accent(0.9f), a * 0.55f));
        ui.outline(x, y, w, h, h / 2.0f, Math.max(1.0f, this.u), UiEasing.mix(UiRenderer.white(0.22f), UiPalette.accent(0.95f), a));
        // small 2x2 layout icon in front of the label
        float ic = 4.0f * k, gap = 1.5f * k, ix = x + 14.0f * k, iy = y + h / 2.0f - ic - gap / 2.0f;
        for (int i = 0; i < 4; i++) ui.round(ix + (i % 2) * (ic + gap), iy + (i / 2) * (ic + gap), ic, ic, k, UiRenderer.white(0.9f));
        ui.text(text, ix + ic * 2.0f + gap + 8.0f * k, y + (h - th) / 2.0f - 0.5f * k, th, 1, UiRenderer.white(0.97f));
        this.editorButton[0] = x; this.editorButton[1] = y; this.editorButton[2] = w; this.editorButton[3] = h;
    }

    /** Self-test: window-pixel center of the chat's HUD Editor button (or null). */
    float[] editorButtonCenter() {
        return this.editorButton[2] > 0.0f ? new float[]{this.editorButton[0] + this.editorButton[2] / 2.0f, this.editorButton[1] + this.editorButton[3] / 2.0f} : null;
    }

    private float handleSize() {
        return Math.max(10.0f, this.d(11.0f));
    }

    /** Grip in the bottom-right corner of a frame: drag it to resize. */
    private void drawHandle(UiRenderer ui, float fx, float fy, float fw, float fh, float hot) {
        float s = this.handleSize();
        float x = this.hx(fx + fw) - s / 2.0f, y = this.hy(fy + fh) - s / 2.0f;
        ui.round(x, y, s, s, s * 0.3f, UiEasing.mix(UiRenderer.argb(0.95f, 28, 29, 34), UiPalette.accent(0.95f), 0.35f + 0.65f * hot));
        ui.outline(x, y, s, s, s * 0.3f, Math.max(1.0f, this.u * 0.8f), UiRenderer.white(0.55f + 0.4f * hot));
        // diagonal grip dots
        float dot = Math.max(1.0f, s * 0.14f);
        for (int i = 0; i < 3; i++) for (int j = 0; j <= i; j++) {
            float px = x + s * 0.72f - j * s * 0.22f, py = y + s * 0.72f - (i - j) * s * 0.22f;
            ui.rect(px - dot / 2.0f, py - dot / 2.0f, dot, dot, UiRenderer.white(0.9f));
        }
    }

    private final java.util.EnumMap<HudSection, float[]> stackSlots = new java.util.EnumMap<>(HudSection.class);

    private boolean onHandle(double mx, double my, float fx, float fy, float fw, float fh) {
        float r = this.handleSize() * 0.9f + 2.0f;
        return Math.abs(mx - this.hx(fx + fw)) <= r && Math.abs(my - this.hy(fy + fh)) <= r;
    }

    // a corner off the screen edge would be unreachable: keep the handle fully on screen
    private float hx(float x) {
        float s = this.handleSize();
        return clamp(x, s / 2.0f + 1.0f, this.lastSw - s / 2.0f - 1.0f);
    }

    private float hy(float y) {
        float s = this.handleSize();
        return clamp(y, s / 2.0f + 1.0f, this.lastSh - s / 2.0f - 1.0f);
    }

    /** Self-test: window-pixel position of a section's corner handle (or null). */
    float[] handleOf(HudSection section) {
        float[] b = this.bounds.get(section);
        if (b == null) return null;
        float pad = this.d(4.0f);
        return new float[]{this.hx(b[0] + b[2] + pad), this.hy(b[1] + b[3] + pad)};
    }

    float[] handleOf(VanillaHud.Part part) {
        float[] r = this.partRect(part);
        if (r == null) return null;
        float pad = this.d(4.0f);
        return new float[]{this.hx(r[0] + r[2] + pad), this.hy(r[1] + r[3] + pad)};
    }

    /** While a corner handle is held: size follows the mouse, the top-left corner stays where it is. */
    private void updateResize(double mx, double my, int sw, int sh) {
        float pad = this.d(4.0f);
        float w0 = this.rsW + pad, h0 = this.rsH + pad; // grab point = frame corner
        float diag = w0 * w0 + h0 * h0;
        if (diag <= 1.0f) return;
        float f = (float) (((mx - this.rsX) * w0 + (my - this.rsY) * h0) / diag);
        if (this.resizingSection != null) {
            float s = Math.max(0.5f, Math.min(2.5f, Math.round(this.rsScale * f * 100.0f) / 100.0f));
            this.scales.put(this.resizingSection, s);
            adsdzhecwnzmbs place = this.places.computeIfAbsent(this.resizingSection, k -> new adsdzhecwnzmbs());
            float k = s / this.rsScale;
            this.placeFromPixel(place, this.rsX, this.rsY, this.rsW * k, this.rsH * k, sw, sh);
        } else if (this.resizingPart != null) {
            VanillaHud.setScale(this.resizingPart, this.rsScale * f);
        }
    }

    private int lastSh = 1080;
    private boolean snappedX;
    private boolean snappedY;

    // ------------------------------------------------------------------ alignment grid + snapping

    /** Grid every 10 GUI units (stronger every 50) with the screen's center lines highlighted. */
    private void drawGrid(UiRenderer ui, int sw, int sh, float alpha) {
        float step = Math.max(6.0f, guiScale() * 10.0f);
        float cx = sw / 2.0f, cy = sh / 2.0f;
        ui.pushAlpha(alpha);
        int i = 1;
        for (float o = step; cx + o < sw || cx - o > 0.0f; o += step, i++) {
            int col = UiRenderer.white(i % 5 == 0 ? 0.10f : 0.045f);
            ui.rect(cx + o, 0.0f, 1.0f, sh, col);
            ui.rect(cx - o - 1.0f, 0.0f, 1.0f, sh, col);
        }
        i = 1;
        for (float o = step; cy + o < sh || cy - o > 0.0f; o += step, i++) {
            int col = UiRenderer.white(i % 5 == 0 ? 0.10f : 0.045f);
            ui.rect(0.0f, cy + o, sw, 1.0f, col);
            ui.rect(0.0f, cy - o - 1.0f, sw, 1.0f, col);
        }
        int center = UiPalette.accent(0.45f);
        ui.rect(cx - 0.5f, 0.0f, 1.0f, sh, center);
        ui.rect(0.0f, cy - 0.5f, sw, 1.0f, center);
        ui.popAlpha();
    }

    /** Bright center line while a dragged element is snapped to it. */
    private void drawSnapGuides(UiRenderer ui, int sw, int sh) {
        float t = Math.max(2.0f, this.u * 2.0f);
        if (this.snappedX) ui.rect(sw / 2.0f - t / 2.0f, 0.0f, t, sh, UiPalette.accent(0.95f));
        if (this.snappedY) ui.rect(0.0f, sh / 2.0f - t / 2.0f, sw, t, UiPalette.accent(0.95f));
    }

    private float snapX(float x, float w, int sw) {
        float c = sw / 2.0f;
        if (Math.abs(x + w / 2.0f - c) < this.d(8.0f)) {
            this.snappedX = true;
            return c - w / 2.0f;
        }
        return x;
    }

    private float snapY(float y, float h, int sh) {
        float c = sh / 2.0f;
        if (Math.abs(y + h / 2.0f - c) < this.d(8.0f)) {
            this.snappedY = true;
            return c - h / 2.0f;
        }
        return y;
    }

    private static float guiScale() {
        try {
            return Math.max(1.0f, net.minecraft.class_310.method_1551().method_22683().method_4495());
        } catch (Throwable t) {
            return 2.0f;
        }
    }

    /** Window-pixel rectangle of a vanilla part, or null. */
    private float[] partRect(VanillaHud.Part part) {
        float[] r = VanillaHud.rect(part);
        if (r == null) return null;
        float g = guiScale();
        return new float[]{r[0] * g, r[1] * g, r[2] * g, r[3] * g};
    }

    private void drawVanillaFrames(UiRenderer ui, double mx, double my) {
        for (VanillaHud.Part part : VanillaHud.Part.values()) {
            float[] r = this.partRect(part);
            if (r == null) continue;
            boolean visible = VanillaHud.state(part).visible;
            boolean hot = this.draggingPart == part || mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3];
            float h = this.anim("vanilla:" + part.name(), hot ? 1.0f : 0.0f, 14.0f);
            float pad = this.d(4.0f);
            if (!visible) ui.round(r[0] - pad, r[1] - pad, r[2] + pad * 2.0f, r[3] + pad * 2.0f, this.d(10.0f), UiRenderer.argb(0.35f, 10, 10, 14));
            ui.outline(r[0] - pad, r[1] - pad, r[2] + pad * 2.0f, r[3] + pad * 2.0f, this.d(10.0f), Math.max(1.0f, this.u), visible ? UiRenderer.white(0.22f + 0.35f * h) : UiRenderer.argb(0.5f + 0.3f * h, 230, 120, 120));
            if (visible) this.drawHandle(ui, r[0] - pad, r[1] - pad, r[2] + pad * 2.0f, r[3] + pad * 2.0f,
                    this.resizingPart == part ? 1.0f : this.onHandle(mx, my, r[0] - pad, r[1] - pad, r[2] + pad * 2.0f, r[3] + pad * 2.0f) ? 0.7f : h * 0.3f);
            if (visible && VanillaHud.preview(part)) ui.round(r[0] - pad, r[1] - pad, r[2] + pad * 2.0f, r[3] + pad * 2.0f, this.d(10.0f), UiRenderer.white(0.06f));
            String label = part.label + (visible ? "  " + Math.round(VanillaHud.state(part).scale * 100.0f) + "%" : "  (hidden)");
            float lw = this.textW(ui, label, this.d(10.0f), 1) + this.d(12.0f);
            float lh = this.d(16.0f);
            float lx = r[0] + r[2] / 2.0f - lw / 2.0f;
            float ly = r[1] - pad - lh - this.d(4.0f);
            if (ly < 0.0f) ly = r[1] + r[3] + pad + this.d(4.0f);
            ui.pushAlpha(0.55f + 0.45f * h);
            ui.round(lx, ly, lw, lh, lh / 2.0f, UiRenderer.argb(0.85f, 20, 21, 25));
            ui.text(label, lx + this.d(6.0f), ly + this.d(2.5f), this.d(10.0f), 1, visible ? UiRenderer.white(0.85f) : UiRenderer.argb(0.9f, 240, 150, 150));
            ui.popAlpha();
        }
    }

    private void drawResetButton(UiRenderer ui, int sw, double mx, double my) {
        String undoText = "Undo", allText = "Reset All";
        float h = this.d(24.0f), gap = this.d(8.0f);
        float uw = this.textW(ui, undoText, this.d(11.0f), 1) + this.d(28.0f);
        float aw = this.textW(ui, allText, this.d(11.0f), 1) + this.d(28.0f);
        float x = sw / 2.0f - (uw + gap + aw) / 2.0f;
        // below the boss bar area, above the crosshair: never covers the boss bar or its label
        float y = net.minecraft.class_310.method_1551().method_22683().method_4506() * 0.30f;
        this.pill(ui, "undo", undoText, x, y, uw, h, mx, my, this.canUndo());
        this.undoBounds[0] = x; this.undoBounds[1] = y; this.undoBounds[2] = uw; this.undoBounds[3] = h;
        float ax = x + uw + gap;
        this.pill(ui, "reset", allText, ax, y, aw, h, mx, my, true);
        this.resetBounds[0] = ax; this.resetBounds[1] = y; this.resetBounds[2] = aw; this.resetBounds[3] = h;
        String hint = "Drag to move  \u00B7  Scroll or drag the corner to resize  \u00B7  Right-click to hide / show  \u00B7  Undo: back one step";
        ui.textCentered(hint, sw / 2.0f, y + h + this.d(6.0f), this.d(9.5f), 0, UiRenderer.white(0.6f));
    }

    private final float[] undoBounds = new float[4];

    private void pill(UiRenderer ui, String id, String text, float x, float y, float w, float h, double mx, double my, boolean on) {
        boolean hot = on && mx >= x && mx < x + w && my >= y && my < y + h;
        float a = this.anim(id, hot ? 1.0f : 0.0f, 14.0f);
        ui.pushAlpha(on ? 1.0f : 0.45f);
        ui.round(x, y, w, h, h / 2.0f, UiEasing.mix(UiRenderer.argb(0.85f, 20, 21, 25), UiPalette.accent(0.85f), a * 0.6f));
        ui.outline(x, y, w, h, h / 2.0f, Math.max(1.0f, this.u), UiEasing.mix(UiRenderer.white(0.18f), UiPalette.accent(0.9f), a));
        ui.textCentered(text, x + w / 2.0f, y + (h - this.d(11.0f)) / 2.0f - this.d(0.5f), this.d(11.0f), 1, UiRenderer.white(0.95f));
        ui.popAlpha();
    }

    private VanillaHud.Part partAt(double mx, double my) {
        for (VanillaHud.Part part : VanillaHud.Part.values()) {
            float[] r = this.partRect(part);
            if (r != null && mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) return part;
        }
        return null;
    }

    /** HookVisuals elements hidden with a right click in the editor. */
    private final java.util.EnumSet<HudSection> hiddenSections = java.util.EnumSet.noneOf(HudSection.class);

    /** Right click in the editor: hide / show any element (HookVisuals or vanilla). */
    public boolean rightClick(double mx, double my) {
        if (!editing) return false;
        HudSection[] sections = HudSection.values();
        for (int i = sections.length - 1; i >= 0; --i) {
            float[] b = this.bounds.get(sections[i]);
            if (b == null || mx < b[0] || mx >= b[0] + b[2] || my < b[1] || my >= b[1] + b[3]) continue;
            if (!this.hiddenSections.remove(sections[i])) this.hiddenSections.add(sections[i]);
            this.save();
            return true;
        }
        VanillaHud.Part part = this.partAt(mx, my);
        if (part == null) return false;
        VanillaHud.toggle(part);
        this.save();
        return true;
    }

    private final Map<HudSection, Float> scales = new EnumMap<HudSection, Float>(HudSection.class);

    float[] boundsOf(HudSection section) {
        return this.bounds.get(section);
    }

    float scaleOf(HudSection section) {
        Float s = this.scales.get(section);
        return s == null ? 1.0f : s;
    }

    /** Mouse wheel in the editor: resize whatever HUD element is under the mouse (ours, music HUD or vanilla). */
    public boolean scroll(double mx, double my, double amount) {
        if (amount == 0.0 || !editing) return false;
        float step = amount > 0.0 ? 0.05f : -0.05f;
        HudSection[] sections = HudSection.values();
        for (int i = sections.length - 1; i >= 0; --i) {
            float[] b = this.bounds.get(sections[i]);
            if (b == null || mx < b[0] || mx >= b[0] + b[2] || my < b[1] || my >= b[1] + b[3]) continue;
            float s = Math.max(0.5f, Math.min(2.5f, Math.round((this.scaleOf(sections[i]) + step) * 20.0f) / 20.0f));
            this.scales.put(sections[i], s);
            this.save();
            return true;
        }
        if (VerseFallHudAnchors.scrollAtMouse(step)) return true;
        VanillaHud.Part part = this.partAt(mx, my);
        if (part == null) return false;
        VanillaHud.resize(part, step);
        this.save();
        return true;
    }

    private float d(float var1) {
        return var1 * this.u;
    }

    private float element(UiRenderer var1, HudSection var2, boolean var3, int var4, int var5, float var6, boolean var7, double var8, double var10, aetekhtbjzavpx var12, DrawAction var13) {
        adsdzhecwnzmbs var22;
        // hidden with a right click in the editor: gone in game, shown faded (to bring it back) in the editor
        boolean hid = this.hiddenSections.contains(var2);
        if (hid && !var7) var3 = false;
        float var14 = this.anim("el:" + var2.name(), var3 ? 1.0f : 0.0f, 9.0f);
        if (var14 < 0.01f) {
            this.bounds.remove((Object)var2);
            // an element that is only gone for a moment keeps its slot in the column, so the ones below never jump up
            adsdzhecwnzmbs hp = this.places.get((Object)var2);
            float[] hs = this.stackSlots.get(var2);
            if (!hid && hs != null && (hp == null || !hp.moved) && var2 != HudSection.COORDS) return var6 + hs[0] * this.u + this.d(7.0f);
            return var6;
        }
        float baseU = this.u;
        this.u = baseU * this.scaleOf(var2); // per-element size (mouse wheel in the HUD editor)
        float[] var15 = var12.get();
        float var16 = var15[0];
        float var17 = var15[1];
        this.u = baseU;
        float[] var18 = this.position(var2, var16, var17, var4, var5, var6);
        float var19 = var18[0];
        float var20 = var18[1];
        this.bounds.put(var2, new float[]{var19, var20, var16, var17});
        float var21 = UiEasing.out(var14);
        var1.pushAlpha(var21 * (hid ? 0.3f : 1.0f));
        var1.pushTransform(0.0f, (1.0f - var21) * this.d(-6.0f), 1.0f);
        this.u = baseU * this.scaleOf(var2);
        try {
            var13.draw(var19, var20);
        } finally {
            this.u = baseU;
        }
        var1.popTransform();
        var1.popAlpha();
        if (var7) {
            this.editFrame(var1, var2, var19, var20, var16, var17, var8, var10);
        }
        boolean var23 = ((var22 = this.places.get((Object)var2)) == null || !var22.moved) && var2 != HudSection.COORDS;
        if (!var23) return var6;
        // the column of not-yet-moved elements: each one keeps the tallest height it had, so the elements below
        // never jump up and down when its content gets shorter or longer for a moment
        float sc = this.scaleOf(var2);
        float[] slot = this.stackSlots.get(var2);
        float norm = var17 / Math.max(0.01f, baseU);
        if (slot == null || slot[1] != sc) this.stackSlots.put(var2, slot = new float[]{norm, sc});
        else if (norm > slot[0]) slot[0] = norm;
        return var6 + slot[0] * baseU + this.d(7.0f);
    }

    private void glass(UiRenderer var1, float var2, float var3, float var4, float var5, float var6, adhvjlahyjurfh var7) {
        var1.shadow(var2, var3, var4, var5, var6, this.d(12.0f), UiRenderer.argb(0.28f, 0, 0, 0));
        var1.round(var2, var3, var4, var5, var6, UiPalette.glass(Math.max(0.0f, Math.min(1.0f, var7.opacity))));
        var1.roundGradient(var2, var3, var4, var5 * 0.6f, var6, UiRenderer.white(0.055f), UiRenderer.white(0.0f));
        var1.outline(var2, var3, var4, var5, var6, Math.max(1.0f, this.u * 0.85f), UiRenderer.white(0.075f));
    }

    private float textW(UiRenderer var1, String var2, float var3, int var4) {
        String var5 = var4 + "|" + var3 + "|" + var2;
        Float var6 = this.widthCache.get(var5);
        if (var6 == null) {
            if (this.widthCache.size() > 512) {
                this.widthCache.remove(this.widthCache.keySet().iterator().next());
            }
            var6 = Float.valueOf(var1.textWidth(var2, var3, var4));
            this.widthCache.put(var5, var6);
        }
        return var6.floatValue();
    }

    private float[] watermarkSize(UiRenderer var1, adhvjlahyjurfh var2) {
        this.lastOptions = var2;
        float var3 = this.d(10.0f) + this.d(8.0f) + this.textW(var1, "HookVisuals", this.d(14.0f), 1) + this.d(12.0f);
        if (var2.time) {
            var3 += this.d(10.0f) + this.textW(var1, this.clock(), this.d(12.5f), 0);
        }
        return new float[]{var3, this.d(32.0f)};
    }

    private String clock() {
        return LocalTime.now().format(CLOCK);
    }

    private void drawWatermark(UiRenderer var1, float var2, float var3, adhvjlahyjurfh var4) {
        float var8;
        float var5 = this.d(32.0f);
        float[] var6 = this.watermarkSize(var1, var4);
        this.glass(var1, var2, var3, var6[0], var5, var5 / 2.0f, var4);
        float var10 = var8 = var2 + this.d(10.0f);
        var1.text("HookVisuals", var10, var3 + (var5 - this.d(14.0f)) / 2.0f - this.d(1.0f), this.d(14.0f), 1, UiRenderer.white(0.96f));
        if (var4.time) {
            float var11 = var10 + this.textW(var1, "HookVisuals", this.d(14.0f), 1) + this.d(9.0f);
            var1.rect(var11, var3 + var5 * 0.3f, Math.max(1.0f, this.u), var5 * 0.4f, UiRenderer.white(0.14f));
            var1.text(this.clock(), var11 + this.d(9.0f), var3 + (var5 - this.d(12.5f)) / 2.0f - this.d(0.5f), this.d(12.5f), 0, UiRenderer.white(0.62f));
        }
    }

    private List<String[]> infoChips(HudDataSource var1) {
        ArrayList<String[]> var2 = new ArrayList<String[]>();
        var2.add(new String[]{"ACTIVITY", String.valueOf(var1.fps()), "fps"});
        var2.add(new String[]{"SIGNAL", var1.ping(), "ms"});
        String var3 = var1.server();
        if (var3 != null && !var3.isBlank()) {
            var2.add(new String[]{"GLOBE", var3, ""});
        }
        return var2;
    }

    private float chipW(UiRenderer var1, String[] var2) {
        float var3 = this.d(14.0f) + this.d(6.0f) + this.textW(var1, var2[1], this.d(12.5f), 1);
        if (!var2[2].isEmpty()) {
            var3 += this.d(3.0f) + this.textW(var1, var2[2], this.d(11.0f), 0);
        }
        return var3;
    }

    private float[] infoSize(UiRenderer var1, HudDataSource var2) {
        float var3 = this.d(12.0f);
        List<String[]> var4 = this.infoChips(var2);
        for (int var5 = 0; var5 < var4.size(); ++var5) {
            var3 += this.chipW(var1, var4.get(var5)) + (var5 > 0 ? this.d(14.0f) : 0.0f);
        }
        return new float[]{var3 + this.d(12.0f), this.d(28.0f)};
    }

    private void drawInfo(UiRenderer var1, float var2, float var3, HudDataSource var4) {
        float[] var5 = this.infoSize(var1, var4);
        float var6 = var5[1];
        this.glass(var1, var2, var3, var5[0], var6, var6 / 2.0f, this.lastOptions);
        float var7 = var2 + this.d(12.0f);
        List<String[]> var8 = this.infoChips(var4);
        for (int var9 = 0; var9 < var8.size(); ++var9) {
            String[] var10 = var8.get(var9);
            if (var9 > 0) {
                var1.circle(var7 + this.d(7.0f), var3 + var6 / 2.0f, this.d(1.5f), UiRenderer.white(0.25f));
                var7 += this.d(14.0f);
            }
            float var11 = this.d(14.0f);
            var1.icon(UiIcon.valueOf(var10[0]), var7, var3 + (var6 - var11) / 2.0f, var11, var9 == 0 ? this.accent(0.95f, 0.0f) : UiRenderer.white(0.8f));
            float var12 = var7 + var11 + this.d(6.0f);
            boolean var13 = var10[2].isEmpty();
            float var14 = var1.text(var10[1], var12, var3 + (var6 - this.d(12.5f)) / 2.0f - this.d(1.0f), this.d(12.5f), var13 ? 0 : 1, UiRenderer.white(var13 ? 0.66f : 0.95f));
            if (!var13) {
                var1.text(var10[2], var12 + var14 + this.d(3.0f), var3 + (var6 - this.d(11.0f)) / 2.0f - this.d(0.5f), this.d(11.0f), 0, UiRenderer.white(0.5f));
            }
            var7 += this.chipW(var1, var10);
        }
    }

    private float[] playerSize(UiRenderer var1, HudDataSource var2) {
        String var3 = this.hp(var2);
        float var4 = this.d(8.0f) + this.d(18.0f) + this.d(8.0f) + this.textW(var1, var2.playerName(), this.d(12.5f), 1) + this.d(12.0f) + this.d(12.0f) + this.d(4.0f) + this.textW(var1, var3, this.d(12.0f), 1) + this.d(12.0f);
        return new float[]{var4, this.d(30.0f)};
    }

    private String hp(HudDataSource var1) {
        float var2 = Math.max(0.0f, var1.health());
        return var2 == (float)Math.round(var2) ? String.valueOf(Math.round(var2)) : String.format(Locale.ROOT, "%.1f", Float.valueOf(var2));
    }

    private void drawPlayer(UiRenderer var1, float var2, float var3, HudDataSource var4) {
        float[] var5 = this.playerSize(var1, var4);
        float var6 = var5[1];
        this.glass(var1, var2, var3, var5[0], var6, var6 / 2.0f, this.lastOptions);
        float var7 = this.d(18.0f);
        float var8 = var2 + this.d(8.0f);
        var1.round(var8 - this.d(1.5f), var3 + (var6 - var7) / 2.0f - this.d(1.5f), var7 + this.d(3.0f), var7 + this.d(3.0f), this.d(6.0f), UiRenderer.white(0.1f));
        var1.head(var8, var3 + (var6 - var7) / 2.0f, var7);
        float var9 = var8 + var7 + this.d(8.0f);
        float var10 = var1.text(var4.playerName(), var9, var3 + (var6 - this.d(12.5f)) / 2.0f - this.d(1.0f), this.d(12.5f), 1, UiRenderer.white(0.95f));
        float var11 = var9 + var10 + this.d(12.0f);
        float var12 = this.d(12.0f);
        var1.icon(UiIcon.HEART, var11, var3 + (var6 - var12) / 2.0f, var12, this.accent(0.95f, 0.2f));
        var1.text(this.hp(var4), var11 + var12 + this.d(4.0f), var3 + (var6 - this.d(12.0f)) / 2.0f - this.d(1.0f), this.d(12.0f), 1, UiRenderer.white(0.85f));
    }

    private String[] axes(HudDataSource var1, boolean var2) {
        double var3 = var2 ? (var1.nether() ? 8.0 : 0.125) : 1.0;
        return new String[]{String.valueOf((long)Math.floor(var1.x() * var3)), String.valueOf((long)Math.floor(var1.y())), String.valueOf((long)Math.floor(var1.z() * var3))};
    }

    private float axesW(UiRenderer var1, String[] var2, float var3) {
        float var4 = 0.0f;
        String[] var5 = new String[]{"X", "Y", "Z"};
        for (int var6 = 0; var6 < 3; ++var6) {
            var4 += this.textW(var1, var5[var6], var3 * 0.8f, 1) + this.d(4.0f) + this.textW(var1, var2[var6], var3, 1) + (var6 < 2 ? this.d(10.0f) : 0.0f);
        }
        return var4;
    }

    private static String facing(float var0) {
        String[] var1 = new String[]{"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        int var2 = Math.floorMod(Math.round(var0 % 360.0f / 45.0f), 8);
        return var1[var2];
    }

    private float[] coordsSize(UiRenderer var1, HudDataSource var2, adhvjlahyjurfh var3) {
        float var4 = this.d(10.0f) + this.d(18.0f) + this.d(10.0f) + this.axesW(var1, this.axes(var2, false), this.d(13.0f)) + this.d(12.0f);
        if (var3.direction) {
            var4 += this.textW(var1, HudLayoutEditor.facing(var2.yaw()), this.d(10.5f), 1) + this.d(14.0f) + this.d(4.0f);
        }
        float var5 = this.d(32.0f);
        if (var3.netherCoords) {
            float var6 = this.d(10.0f) + this.netherTagW(var1, var2) + this.d(8.0f) + this.axesW(var1, this.axes(var2, true), this.d(11.0f)) + this.d(12.0f);
            var4 = Math.max(var4, var6);
            var5 += this.d(16.0f);
        }
        return new float[]{var4, var5};
    }

    private void drawCoords(UiRenderer var1, float var2, float var3, HudDataSource var4, adhvjlahyjurfh var5) {
        float[] var6 = this.coordsSize(var1, var4, var5);
        float var7 = var6[0];
        float var8 = var6[1];
        this.glass(var1, var2, var3, var7, var8, this.d(16.0f), var5);
        float var9 = this.d(18.0f);
        float var10 = var2 + this.d(10.0f) + var9 / 2.0f;
        float var11 = var3 + this.d(16.0f);
        var1.outline(var10 - var9 / 2.0f, var11 - var9 / 2.0f, var9, var9, var9 / 2.0f, Math.max(1.0f, this.u * 1.2f), UiRenderer.white(0.42f));
        double var12 = Math.toRadians(var4.yaw());
        float var14 = (float)(-Math.sin(var12));
        float var15 = (float)Math.cos(var12);
        float var16 = var9 / 2.0f - this.d(3.5f);
        var1.glow(var10, var11, var9, this.accent(0.1f, 0.4f));
        var1.circle(var10 - var14 * var16, var11 - var15 * var16, this.d(2.4f), this.accent(1.0f, 0.4f));
        var1.circle(var10 + var14 * var16, var11 + var15 * var16, this.d(1.6f), UiRenderer.white(0.35f));
        var1.circle(var10, var11, this.d(1.4f), UiRenderer.white(0.85f));
        float var17 = var2 + this.d(10.0f) + var9 + this.d(10.0f);
        this.drawAxes(var1, this.axes(var4, false), var17, var3 + this.d(16.0f) - this.d(7.5f), this.d(13.0f), 0.95f);
        if (var5.direction) {
            String var18 = HudLayoutEditor.facing(var4.yaw());
            float var19 = this.textW(var1, var18, this.d(10.5f), 1) + this.d(14.0f);
            float var20 = var2 + var7 - this.d(8.0f) - var19;
            float var21 = var3 + this.d(16.0f) - this.d(9.0f);
            var1.round(var20, var21, var19, this.d(18.0f), this.d(9.0f), UiRenderer.white(0.09f));
            var1.textCentered(var18, var20 + var19 / 2.0f, var21 + this.d(3.0f), this.d(10.5f), 1, UiRenderer.white(0.85f));
        }
        if (var5.netherCoords) {
            float var22 = var3 + this.d(32.0f) - this.d(3.0f);
            String var23 = var4.nether() ? "OVERWORLD" : "NETHER";
            float var24 = this.netherTagW(var1, var4);
            var1.round(var2 + this.d(10.0f), var22 - this.d(1.0f), var24, this.d(14.0f), this.d(7.0f), UiRenderer.white(0.07f));
            var1.textCentered(var23, var2 + this.d(10.0f) + var24 / 2.0f, var22 + this.d(1.5f), this.d(8.5f), 1, UiRenderer.white(0.5f));
            this.drawAxes(var1, this.axes(var4, true), var2 + this.d(10.0f) + var24 + this.d(8.0f), var22, this.d(11.0f), 0.6f);
        }
    }

    private float netherTagW(UiRenderer var1, HudDataSource var2) {
        return this.textW(var1, var2.nether() ? "OVERWORLD" : "NETHER", this.d(8.5f), 1) + this.d(12.0f);
    }

    private void drawAxes(UiRenderer var1, String[] var2, float var3, float var4, float var5, float var6) {
        String[] var7 = new String[]{"X", "Y", "Z"};
        float var8 = var3;
        for (int var9 = 0; var9 < 3; ++var9) {
            float var10 = var1.text(var7[var9], var8, var4 + var5 * 0.12f, var5 * 0.8f, 1, UiRenderer.white(0.38f * var6));
            var8 += var10 + this.d(4.0f);
            var8 += var1.text(var2[var9], var8, var4, var5, 1, UiRenderer.white(var6)) + this.d(10.0f);
        }
    }

    private void drawModules(UiRenderer var1, boolean var2, int var3, int var4, HudDataSource var5, boolean var6, double var7, double var9) {
        float baseU = this.u;
        this.u = baseU * this.scaleOf(HudSection.MODULES);
        try {
            this.drawModules0(var1, var2, var3, var4, var5, var6, var7, var9, baseU);
        } finally {
            this.u = baseU;
        }
    }

    private void drawModules0(UiRenderer var1, boolean var2, int var3, int var4, HudDataSource var5, boolean var6, double var7, double var9, float baseU) {
        List<String> var13;
        float var11 = this.anim("el:MODULES", var2 ? 1.0f : 0.0f, 9.0f);
        float var12 = this.d(12.5f);
        var13 = var2 ? var5.modules() : List.of();
        if (var12 != this.sortedAtSize || !var13.equals(this.moduleNames)) {
            this.moduleNames = List.copyOf(var13);
            ArrayList<String> var14 = new ArrayList<String>(var13);
            var14.sort((var3x, var4x) -> {
                int var5x = Float.compare(this.textW(var1, var4x, var12, 0), this.textW(var1, var3x, var12, 0));
                return var5x != 0 ? var5x : var3x.compareTo(var4x);
            });
            this.sortedModuleNames = var14;
            this.sortedAtSize = var12;
        }
        List<String> var37 = this.sortedModuleNames;
        for (aemkvoltyxggkb var16 : this.rows.values()) {
            var16.active = false;
        }
        int var38 = 0;
        while (var38 < var37.size()) {
            aemkvoltyxggkb var39 = this.rows.computeIfAbsent(var37.get(var38), var0 -> new aemkvoltyxggkb());
            var39.active = true;
            var39.index = var38++;
        }
        float var40 = this.d(26.0f);
        float var17 = this.d(22.0f);
        float var18 = 0.0f;
        for (String var20 : var37) {
            var18 = Math.max(var18, this.rowW(var1, var20, var12));
        }
        float var41 = Math.max(this.d(90.0f), var18);
        float var42 = Math.max(var17, (float)var37.size() * var40 - this.d(4.0f));
        if (var11 < 0.01f && this.rows.isEmpty()) {
            this.bounds.remove((Object)HudSection.MODULES);
        } else {
            float scaledU = this.u;
            this.u = baseU;
            float[] var21 = this.position(HudSection.MODULES, var41, var42, var3, var4, 0.0f);
            this.u = scaledU;
            float var22 = var21[0];
            float var23 = var21[1];
            boolean var24 = var22 + var41 / 2.0f > (float)var3 / 2.0f;
            this.bounds.put(HudSection.MODULES, new float[]{var22, var23, var41, var42});
            float var25 = 1.0f - (float)Math.exp(-13.0f * this.dt);
            var1.pushAlpha(UiEasing.out(var11));
            this.removedRows.clear();
            ArrayList<String> var26 = this.removedRows;
            for (Map.Entry<String, aemkvoltyxggkb> var28 : this.rows.entrySet()) {
                float var34;
                aemkvoltyxggkb var29 = var28.getValue();
                String var30 = var28.getKey();
                if (var29.active) {
                    var29.target = (float)var29.index * var40;
                }
                if (Float.isNaN(var29.y)) {
                    var29.y = var29.target;
                }
                var29.y += (var29.target - var29.y) * var25;
                var29.presence = var29.presence + ((var29.active ? 1.0f : 0.0f) - var29.presence) * (1.0f - (float)Math.exp(-11.0f * this.dt));
                if (!var29.active && var29.presence < 0.015f) {
                    var26.add(var30);
                    continue;
                }
                float var31 = UiEasing.out(var29.presence);
                float var32 = this.rowW(var1, var30, var12);
                float var33 = (1.0f - var31) * this.d(18.0f);
                float f = var34 = var24 ? var22 + var41 - var32 + var33 : var22 - var33;
                float var35 = var23 + var29.y;
                if (var35 + var17 + this.d(12.0f) < 0.0f || var35 - this.d(12.0f) > (float)var4 || var34 + var32 + this.d(12.0f) < 0.0f || var34 - this.d(12.0f) > (float)var3) continue;
                var1.pushAlpha(var31);
                this.glassRow(var1, var34, var35, var32, var17);
                float var36 = (float)var29.index * 0.045f;
                if (this.lastOptions.rainbow) {
                    var1.outline(var34, var35, var32, var17, var17 / 2.0f, Math.max(1.0f, this.u * 0.85f), this.accent(0.22f, var36));
                }
                var1.text(var30, var34 + this.d(11.0f), var35 + (var17 - var12) / 2.0f - this.d(1.0f), var12, 0, this.lastOptions.rainbow ? this.accent(0.97f, var36) : UiRenderer.white(0.95f));
                var1.popAlpha();
            }
            for (String var44 : var26) {
                this.rows.remove(var44);
            }
            var1.popAlpha();
            if (var6) {
                this.u = baseU;
                this.editFrame(var1, HudSection.MODULES, var22, var23, var41, var42, var7, var9);
            }
        }
    }

    private float rowW(UiRenderer var1, String var2, float var3) {
        return this.d(11.0f) + this.textW(var1, var2, var3, 0) + this.d(11.0f);
    }

    private void glassRow(UiRenderer var1, float var2, float var3, float var4, float var5) {
        adhvjlahyjurfh var6 = this.lastOptions;
        var1.shadow(var2, var3, var4, var5, var5 / 2.0f, this.d(9.0f), UiRenderer.argb(0.22f, 0, 0, 0));
        var1.round(var2, var3, var4, var5, var5 / 2.0f, UiPalette.glass(Math.max(0.0f, Math.min(1.0f, var6.opacity))));
        var1.roundGradient(var2, var3, var4, var5 * 0.65f, var5 / 2.0f, UiRenderer.white(0.05f), UiRenderer.white(0.0f));
        var1.outline(var2, var3, var4, var5, var5 / 2.0f, Math.max(1.0f, this.u * 0.85f), UiRenderer.white(0.065f));
    }

    // ------------------------------------------------------------------ music bar (VerseFall lyrics bar as a normal element)

    /**
     * The music / lyrics bar is drawn by VerseFall, but placed, anchored and sized here like every other element:
     * its size comes from VerseFall (HUD-space units -> window pixels), our layout decides where it goes and VerseFall
     * is told to draw it there.
     */
    private void musicBar(UiRenderer ui, int sw, int sh, boolean editor, double mx, double my) {
        com.spotifyclient.hud.Draggable bar = VerseFallHudAnchors.lyricsBar();
        if (bar == null) {
            this.bounds.remove(HudSection.MUSIC_BAR);
            return;
        }
        float hudW = com.spotifyclient.hud.HudSpace.width();
        float k = hudW > 0.0f ? sw / hudW : 1.0f; // window pixels per VerseFall HUD unit
        float s = this.scaleOf(HudSection.MUSIC_BAR);
        VerseFallHudAnchors.musicBarScale = s;
        boolean show = bar.interactive() && bar.width() > 0.0f;
        if (!editor && this.hiddenSections.contains(HudSection.MUSIC_BAR)) {
            // hidden: VerseFall still draws it, so park it far off screen
            this.bounds.remove(HudSection.MUSIC_BAR);
            bar.drag().setTarget(-100000.0f, -100000.0f);
            bar.drag().syncToTarget();
            return;
        }
        this.element(ui, HudSection.MUSIC_BAR, show, sw, sh, 0.0f, editor, mx, my,
                () -> new float[]{bar.width() * s * k, bar.height() * s * k},
                (ex, ey) -> {
                    bar.drag().setTarget(ex / k, ey / k);
                    bar.drag().syncToTarget();
                });
    }

    // ------------------------------------------------------------------ effect timer

    private static final List<EffectRow> PREVIEW = List.of(
            new EffectRow(null, "Speed II", 1820, 3600, false, 0xFF33EBFF, false),
            new EffectRow(null, "Strength", 160, 3600, false, 0xFFFFC700, false),
            new EffectRow(null, "Fire Resistance", 0, 0, true, 0xFFFF9900, false));

    /** Live effects, or a sample list while the HUD editor is open and nothing is active (so it can be placed). */
    private List<EffectRow> effectRows(HudDataSource src, boolean editor) {
        List<EffectRow> rows = src.inWorld() ? src.effects() : List.of();
        return rows.isEmpty() && editor ? PREVIEW : rows;
    }

    private static String clockOf(EffectRow e) {
        if (e.infinite()) return "";
        int s = Math.max(0, e.ticks() / 20);
        if (s >= 3600) return String.format(Locale.ROOT, "%d:%02d:%02d", s / 3600, s / 60 % 60, s % 60);
        return String.format(Locale.ROOT, "%d:%02d", s / 60, s % 60);
    }

    private float effectRowH(adhvjlahyjurfh o) {
        return this.d(o.effectBars ? 30.0f : 26.0f);
    }

    // ------------------------------------------------------------------ schematic builder progress card

    private float[] builderSize(UiRenderer ui, SchematicBuilderModule.Progress p) {
        if (p == null) return new float[]{this.d(220.0f), this.d(64.0f)};
        float w = Math.max(this.d(220.0f), Math.max(this.textW(ui, p.name(), this.d(12.0f), 1) + this.d(90.0f), this.textW(ui, p.eta(), this.d(10.0f), 0) + this.d(24.0f)));
        return new float[]{Math.min(w, this.d(340.0f)), this.d(64.0f)};
    }

    private void drawBuilder(UiRenderer ui, float x, float y, SchematicBuilderModule.Progress p, adhvjlahyjurfh o) {
        if (p == null) return;
        float[] size = this.builderSize(ui, p);
        float w = size[0], h = size[1];
        this.glass(ui, x, y, w, h, this.d(14.0f), o);
        float pad = this.d(12.0f);
        String pct = p.total() <= 0 ? "--" : String.format(java.util.Locale.ROOT, "%.1f%%", p.percent());
        ui.text(p.name(), x + pad, y + this.d(8.0f), this.d(12.0f), 1, UiRenderer.white(0.95f));
        ui.textRight(pct, x + w - pad, y + this.d(8.0f), this.d(12.0f), 1, p.percent() >= 99.95 ? UiRenderer.argb(0.95f, 120, 235, 150) : this.accent(0.95f, 0.0f));
        // progress bar
        float bx = x + pad, by = y + this.d(27.0f), bw = w - pad * 2.0f, bh = this.d(6.0f);
        ui.round(bx, by, bw, bh, bh / 2.0f, UiRenderer.white(0.08f));
        float frac = (float) Math.max(0.0, Math.min(1.0, p.percent() / 100.0));
        float shown = this.anim("builderbar", frac, 6.0f);
        if (shown > 0.001f) {
            int c1 = this.accent(0.95f, 0.0f), c2 = this.accent(0.95f, 0.35f);
            ui.round(bx, by, Math.max(bh, bw * shown), bh, bh / 2.0f, UiEasing.mix(c1, c2, 0.5f));
            // soft moving shine on the filled part
            float sx = bx + (bw * shown) * ((this.time * 0.35f) % 1.0f);
            ui.round(Math.max(bx, sx - this.d(10.0f)), by, Math.min(this.d(20.0f), bx + bw * shown - Math.max(bx, sx - this.d(10.0f))), bh, bh / 2.0f, UiRenderer.white(0.18f));
        }
        String count = String.format(java.util.Locale.ROOT, "%,d / %,d blocks", p.done(), p.total());
        ui.text(count, bx, y + this.d(38.0f), this.d(10.0f), 0, UiRenderer.white(0.62f));
        ui.textRight(p.state(), x + w - pad, y + this.d(38.0f), this.d(10.0f), 0, p.problem() ? UiRenderer.argb(0.95f, 255, 175, 90) : UiRenderer.white(0.62f));
        ui.text(p.eta(), bx, y + this.d(50.0f), this.d(10.0f), 0, UiRenderer.white(0.8f));
    }

    private float[] effectsSize(UiRenderer ui, List<EffectRow> rows, adhvjlahyjurfh o) {
        float nameW = 0.0f, timeW = 0.0f;
        for (EffectRow e : rows) {
            nameW = Math.max(nameW, this.textW(ui, e.name(), this.d(12.0f), 1));
            timeW = Math.max(timeW, this.textW(ui, clockOf(e), this.d(11.5f), 0));
        }
        float icon = o.effectIcons ? this.d(18.0f) + this.d(8.0f) : 0.0f;
        float w = this.d(10.0f) + icon + nameW + this.d(16.0f) + timeW + this.d(12.0f);
        float h = this.d(6.0f) + rows.size() * this.effectRowH(o) + this.d(4.0f);
        return new float[]{Math.max(this.d(120.0f), w), h};
    }

    private void drawEffects(UiRenderer ui, float x, float y, List<EffectRow> rows, adhvjlahyjurfh o) {
        float[] size = this.effectsSize(ui, rows, o);
        float w = size[0];
        this.glass(ui, x, y, w, size[1], this.d(14.0f), o);
        float rowH = this.effectRowH(o);
        float ry = y + this.d(6.0f);
        for (int i = 0; i < rows.size(); i++) {
            EffectRow e = rows.get(i);
            boolean ending = !e.infinite() && e.ticks() < 200;
            // last 10 seconds: gentle blink
            float blink = ending ? 0.55f + 0.45f * (float)Math.abs(Math.cos(this.time * 4.0f)) : 1.0f;
            int rgb = e.color() & 0xFFFFFF;
            float cx = x + this.d(10.0f);
            float mid = ry + this.d(11.0f);
            if (o.effectIcons) {
                float is = this.d(18.0f);
                ui.round(cx - this.d(2.0f), mid - is / 2.0f - this.d(2.0f), is + this.d(4.0f), is + this.d(4.0f), this.d(6.0f), (Math.round(0.16f * 255) << 24) | rgb);
                ui.pushAlpha(blink);
                if (e.icon() != null) ui.guiSprite(e.icon(), cx, mid - is / 2.0f, is);
                else ui.circle(cx + is / 2.0f, mid, is / 3.0f, 0xFF000000 | rgb);
                ui.popAlpha();
                cx += is + this.d(8.0f);
            }
            ui.text(e.name(), cx, mid - this.d(12.0f) / 2.0f - this.d(1.0f), this.d(12.0f), 1, e.bad() ? UiRenderer.argb(0.95f, 255, 140, 140) : UiRenderer.white(0.95f));
            String t = clockOf(e);
            int tc = ending ? UiRenderer.argb(blink, 255, 120, 110) : UiRenderer.white(0.62f);
            ui.textRight(t, x + w - this.d(12.0f), mid - this.d(11.5f) / 2.0f - this.d(1.0f), this.d(11.5f), 0, tc);
            if (o.effectBars) {
                float bx = cx, bw = x + w - this.d(12.0f) - bx;
                float by = ry + this.d(22.0f);
                float bh = Math.max(1.0f, this.d(2.5f));
                ui.round(bx, by, bw, bh, bh / 2.0f, UiRenderer.white(0.08f));
                float frac = e.infinite() || e.total() <= 0 ? 1.0f : Math.max(0.0f, Math.min(1.0f, e.ticks() / (float)e.total()));
                if (frac > 0.0f) ui.round(bx, by, Math.max(bh, bw * frac), bh, bh / 2.0f, (Math.round(0.9f * blink * 255) << 24) | rgb);
            }
            ry += rowH;
        }
    }

    private float[] position(HudSection var1, float var2, float var3, int var4, int var5, float var6) {
        float[] fArray;
        adsdzhecwnzmbs var7 = this.places.get((Object)var1);
        if (var7 != null && var7.moved) {
            float var8 = var7.centerX ? (float)var4 / 2.0f + this.d(var7.ox) - var2 / 2.0f : var7.right ? (float)var4 - this.d(var7.ox) - var2 : this.d(var7.ox);
            float var9 = var7.centerY ? (float)var5 / 2.0f + this.d(var7.oy) - var3 / 2.0f : var7.bottom ? (float)var5 - this.d(var7.oy) - var3 : this.d(var7.oy);
            return new float[]{HudLayoutEditor.clamp(var8, 0.0f, (float)var4 - var2), HudLayoutEditor.clamp(var9, 0.0f, (float)var5 - var3)};
        }
        switch (var1.ordinal()) {
            case 3: {
                float[] fArray2 = new float[2];
                fArray2[0] = this.d(10.0f);
                fArray = fArray2;
                fArray2[1] = (float)var5 - this.d(10.0f) - var3;
                break;
            }
            case 6: {
                fArray = new float[]{(float)var4 / 2.0f - var2 / 2.0f, this.d(8.0f)};
                break;
            }
            case 5: {
                fArray = new float[]{(float)var4 - this.d(10.0f) - var2, (float)var5 * 0.34f};
                break;
            }
            case 7: {
                fArray = new float[]{(float)var4 / 2.0f - var2 / 2.0f, this.d(64.0f)};
                break;
            }
            case 4: {
                float[] fArray3 = new float[2];
                fArray3[0] = (float)var4 - this.d(10.0f) - var2;
                fArray = fArray3;
                fArray3[1] = this.d(10.0f);
                break;
            }
            default: {
                float[] fArray4 = new float[2];
                fArray4[0] = this.d(10.0f);
                fArray = fArray4;
                fArray4[1] = var6;
            }
        }
        return fArray;
    }

    private void placeFromPixel(adsdzhecwnzmbs var1, float var2, float var3, float var4, float var5, int var6, int var7) {
        var2 = HudLayoutEditor.clamp(var2, 0.0f, (float)var6 - var4);
        var3 = HudLayoutEditor.clamp(var3, 0.0f, (float)var7 - var5);
        var1.moved = true;
        // anchor to the nearest of left / center / right and top / middle / bottom, so a centered element stays
        // centered (and edge elements stay at their edge) when the window is resized
        float cx = var2 + var4 / 2.0f - (float)var6 / 2.0f;
        float cy = var3 + var5 / 2.0f - (float)var7 / 2.0f;
        // the edge nearest to it is what stays put (content that grows grows away from that edge); the middle only
        // when it really sits on the center line, otherwise it would grow both ways and seem to wander
        var1.centerX = Math.abs(cx) < Math.max(8.0f, (float)var6 / 20.0f);
        var1.centerY = Math.abs(cy) < 3.0f;
        var1.right = !var1.centerX && cx > 0.0f;
        var1.bottom = !var1.centerY && cy > 0.0f;
        var1.ox = (var1.centerX ? cx : var1.right ? (float)var6 - var2 - var4 : var2) / this.u;
        var1.oy = (var1.centerY ? cy : var1.bottom ? (float)var7 - var3 - var5 : var3) / this.u;
    }

    private void editFrame(UiRenderer var1, HudSection var2, float var3, float var4, float var5, float var6, double var7, double var9) {
        boolean var11 = this.dragging == var2 || var7 >= (double)var3 && var7 < (double)(var3 + var5) && var9 >= (double)var4 && var9 < (double)(var4 + var6);
        float var12 = this.anim("edit:" + var2.name(), var11 ? 1.0f : 0.0f, 14.0f);
        float var13 = this.d(4.0f);
        var1.outline(var3 - var13, var4 - var13, var5 + var13 * 2.0f, var6 + var13 * 2.0f, this.d(10.0f), Math.max(1.0f, this.u), UiRenderer.white(0.22f + 0.35f * var12));
        this.drawHandle(var1, var3 - var13, var4 - var13, var5 + var13 * 2.0f, var6 + var13 * 2.0f,
                this.resizingSection == var2 ? 1.0f : this.onHandle(var7, var9, var3 - var13, var4 - var13, var5 + var13 * 2.0f, var6 + var13 * 2.0f) ? 0.7f : var12 * 0.3f);
        String label = var2.label + (this.hiddenSections.contains(var2) ? "  (hidden)" : "  " + Math.round(this.scaleOf(var2) * 100.0f) + "%");
        float var14 = this.textW(var1, label, this.d(10.0f), 1) + this.d(12.0f);
        float var15 = this.d(16.0f);
        boolean var16 = var3 + var5 / 2.0f > (float)this.lastSw / 2.0f;
        float var17 = var16 ? var3 - var13 - this.d(6.0f) - var14 : var3 + var5 + var13 + this.d(6.0f);
        float var18 = var2 == HudSection.MODULES ? var4 : var4 + (var6 - var15) / 2.0f;
        var1.pushAlpha(0.5f + 0.5f * var12);
        var1.round(var17, var18, var14, var15, var15 / 2.0f, UiRenderer.argb(0.85f, 20, 21, 25));
        var1.text(label, var17 + this.d(6.0f), var18 + this.d(2.5f), this.d(10.0f), 1, UiRenderer.white(0.85f));
        var1.popAlpha();
    }

    public boolean mousePressed(double var1, double var3) {
        if (!editing) {
            if (this.editorButton[2] > 0.0f && var1 >= this.editorButton[0] && var1 < this.editorButton[0] + this.editorButton[2] && var3 >= this.editorButton[1] && var3 < this.editorButton[1] + this.editorButton[3]) {
                editing = true;
                return true;
            }
            return false;
        }
        // corner handles first (they sit on the frame corner, partly outside the element)
        HudSection[] secs = HudSection.values();
        float pad = this.d(4.0f);
        for (int i = secs.length - 1; i >= 0; --i) {
            float[] b = this.bounds.get(secs[i]);
            if (b == null || !this.onHandle(var1, var3, b[0] - pad, b[1] - pad, b[2] + pad * 2.0f, b[3] + pad * 2.0f)) continue;
            this.resizingSection = secs[i];
            this.rsX = b[0]; this.rsY = b[1]; this.rsW = b[2]; this.rsH = b[3]; this.rsScale = this.scaleOf(secs[i]);
            return true;
        }
        for (VanillaHud.Part p : VanillaHud.Part.values()) {
            float[] r = this.partRect(p);
            if (r == null || !VanillaHud.state(p).visible || !this.onHandle(var1, var3, r[0] - pad, r[1] - pad, r[2] + pad * 2.0f, r[3] + pad * 2.0f)) continue;
            this.resizingPart = p;
            this.rsX = r[0]; this.rsY = r[1]; this.rsW = r[2]; this.rsH = r[3]; this.rsScale = VanillaHud.state(p).scale;
            return true;
        }
        if (this.resetBounds[2] > 0.0f && var1 >= this.resetBounds[0] && var1 < this.resetBounds[0] + this.resetBounds[2] && var3 >= this.resetBounds[1] && var3 < this.resetBounds[1] + this.resetBounds[3]) {
            this.resetLayout();
            return true;
        }
        if (this.undoBounds[2] > 0.0f && var1 >= this.undoBounds[0] && var1 < this.undoBounds[0] + this.undoBounds[2] && var3 >= this.undoBounds[1] && var3 < this.undoBounds[1] + this.undoBounds[3]) {
            this.undoLast();
            return true;
        }
        VanillaHud.Part part = this.partAt(var1, var3);
        if (part != null) {
            float[] r = this.partRect(part);
            this.draggingPart = part;
            this.grabX = var1 - r[0];
            this.grabY = var3 - r[1];
            return true;
        }
        HudSection[] var5 = HudSection.values();
        for (int var6 = var5.length - 1; var6 >= 0; --var6) {
            float[] var7 = this.bounds.get((Object)var5[var6]);
            if (var7 == null || !(var1 >= (double)var7[0]) || !(var1 < (double)(var7[0] + var7[2])) || !(var3 >= (double)var7[1]) || !(var3 < (double)(var7[1] + var7[3]))) continue;
            this.dragging = var5[var6];
            this.grabX = var1 - (double)var7[0];
            this.grabY = var3 - (double)var7[1];
            return true;
        }
        return false;
    }

    public void mouseReleased() {
        if (this.resizingSection != null || this.resizingPart != null) {
            this.resizingSection = null;
            this.resizingPart = null;
            this.save();
        }
        if (this.draggingPart != null) {
            this.draggingPart = null;
            this.save();
        }
        if (this.dragging != null) {
            this.dragging = null;
            this.save();
        }
    }

    /** "Reset All": every element back to its default place, size and visibility (can be undone too). */
    public void resetLayout() {
        this.places.clear();
        this.scales.clear();
        this.hiddenSections.clear();
        VanillaHud.reset();
        VerseFallHudAnchors.reset();
        this.save();
    }

    private float anim(String var1, float var2, float var3) {
        float[] var4 = this.anims.computeIfAbsent(var1, var1x -> new float[]{var2});
        var4[0] = var4[0] + (var2 - var4[0]) * (1.0f - (float)Math.exp(-var3 * this.dt));
        if (Math.abs(var2 - var4[0]) < 0.001f) {
            var4[0] = var2;
        }
        return var4[0];
    }

    private int accent(float var1, float var2) {
        if (!this.lastOptions.rainbow) {
            return UiPalette.accent(var1);
        }
        float var3 = (this.time * 0.08f * this.lastOptions.rainbowSpeed + var2) % 1.0f;
        int var4 = Color.HSBtoRGB(var3 < 0.0f ? var3 + 1.0f : var3, 0.5f, 1.0f) & 0xFFFFFF;
        return Math.round(Math.max(0.0f, Math.min(1.0f, var1)) * 255.0f) << 24 | var4;
    }

    private static float clamp(float var0, float var1, float var2) {
        return var2 < var1 ? var1 : Math.max(var1, Math.min(var2, var0));
    }

    public void load() {
        if (!this.loaded) {
            this.loaded = true;
            try {
                if (this.file != null && Files.isRegularFile(this.file, new LinkOption[0])) {
                    this.apply(Files.readString(this.file, StandardCharsets.UTF_8));
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.lastSaved = this.serialize();
        }
    }

    /** Replaces the whole layout (our elements + vanilla parts) with a saved one. */
    private void apply(String layout) {
        this.places.clear();
        this.scales.clear();
        this.hiddenSections.clear();
        VanillaHud.reset();
        for (String var2 : layout.split("\n")) {
            String[] var3 = var2.trim().split(",");
            try {
                if (VanillaHud.parse(var3)) continue;
                if (var3.length == 2 && var3[0].equals("HIDE")) {
                    this.hiddenSections.add(HudSection.valueOf(var3[1]));
                    continue;
                }
                if (var3.length == 3 && var3[0].equals("SCALE")) {
                    this.scales.put(HudSection.valueOf(var3[1]), Float.parseFloat(var3[2]));
                    continue;
                }
                if (var3.length != 5) continue;
                adsdzhecwnzmbs var4 = new adsdzhecwnzmbs();
                var4.moved = true;
                var4.right = var3[1].equals("R");
                var4.centerX = var3[1].equals("C");
                var4.bottom = var3[2].equals("B");
                var4.centerY = var3[2].equals("M");
                var4.ox = Float.parseFloat(var3[3]);
                var4.oy = Float.parseFloat(var3[4]);
                this.places.put(HudSection.valueOf(var3[0]), var4);
            } catch (IllegalArgumentException ignored) {
                // unknown element or broken line: skip it
            }
        }
    }

    /** Earlier layouts, newest first: "Undo" in the editor steps back through them. */
    private final java.util.ArrayDeque<String> undo = new java.util.ArrayDeque<>();
    private String lastSaved;

    /** Self-test: the layout as it would be saved. */
    String layoutText() {
        return this.serialize();
    }

    boolean canUndo() {
        return !this.undo.isEmpty();
    }

    /** "Undo": back to the layout before the last change (move, resize, hide, ...). */
    public void undoLast() {
        String prev = this.undo.pollFirst();
        if (prev == null) return;
        this.apply(prev);
        this.lastSaved = prev;
        this.write(prev);
    }

    private void save() {
        String now = this.serialize();
        if (this.lastSaved != null && !now.equals(this.lastSaved)) {
            this.undo.addFirst(this.lastSaved);
            while (this.undo.size() > 50) this.undo.removeLast();
        }
        this.lastSaved = now;
        this.write(now);
    }

    private void write(String text) {
        try {
            if (this.file != null) {
                Files.createDirectories(this.file.getParent(), new FileAttribute[0]);
                Files.writeString(this.file, (CharSequence)text, StandardCharsets.UTF_8, new OpenOption[0]);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private String serialize() {
        {
            {
                StringBuilder var1 = new StringBuilder();
                for (Map.Entry<HudSection, adsdzhecwnzmbs> var3 : this.places.entrySet()) {
                    adsdzhecwnzmbs var4 = var3.getValue();
                    if (!var4.moved) continue;
                    var1.append(var3.getKey().name()).append(',').append(var4.centerX ? "C" : var4.right ? "R" : "L").append(',').append(var4.centerY ? "M" : var4.bottom ? "B" : "T").append(',').append(String.format(Locale.ROOT, "%.1f,%.1f", Float.valueOf(var4.ox), Float.valueOf(var4.oy))).append('\n');
                }
                for (Map.Entry<HudSection, Float> sc : this.scales.entrySet()) {
                    var1.append("SCALE,").append(sc.getKey().name()).append(',').append(String.format(Locale.ROOT, "%.2f", sc.getValue())).append('\n');
                }
                for (HudSection hs : this.hiddenSections) var1.append("HIDE,").append(hs.name()).append('\n');
                var1.append(VanillaHud.serialize());
                return var1.toString();
            }
        }
    }

    public static enum HudSection {
        WATERMARK("Watermark"),
        INFO("Info"),
        PLAYER("Player"),
        COORDS("Coordinates"),
        MODULES("Modules"),
        EFFECT_TIMER("Effect Timer"),
        MUSIC_BAR("Music Bar"),
        BUILDER("Schematic Builder");

        final String label;

        private HudSection(String nullxx) {
            this.label = nullxx;
        }
    }

    public static final class adhvjlahyjurfh {
        public boolean watermark = true;
        public boolean time = true;
        public boolean info = true;
        public boolean player;
        public boolean coords = true;
        public boolean direction = true;
        public boolean netherCoords;
        public boolean modules = true;
        public float scale = 1.0f;
        public float opacity = 0.62f;
        public boolean rainbow;
        public float rainbowSpeed = 1.0f;
        public boolean effectTimer;
        public boolean effectIcons = true;
        public boolean effectBars = true;
    }

    private static final class adsdzhecwnzmbs {
        boolean moved;
        boolean right;
        boolean bottom;
        boolean centerX;
        boolean centerY;
        float ox;
        float oy;
        private static final String a7e2a8a8db56243c3 = "wjWs3DYET7y52zJ6Qq386ZoiUzd5Q-J9LK0tbGNZHUs";

        private adsdzhecwnzmbs() {
        }
    }

    @FunctionalInterface
    private static interface aetekhtbjzavpx {
        public float[] get();
    }

    @FunctionalInterface
    private static interface DrawAction {
        public void draw(float var1, float var2);
    }

    public static interface HudDataSource {
        public int fps();

        public String ping();

        public String server();

        public boolean inWorld();

        public double x();

        public double y();

        public double z();

        public float yaw();

        public boolean nether();

        public String playerName();

        public float health();

        public List<String> modules();

        default List<EffectRow> effects() {
            return List.of();
        }
    }

    /** One active potion effect for the Effect Timer (icon = GUI sprite id, ticks left, total for the bar). */
    public record EffectRow(Object icon, String name, int ticks, int total, boolean infinite, int color, boolean bad) {
    }

    private static final class aemkvoltyxggkb {
        float y = Float.NaN;
        float presence;
        float target;
        int index;
        boolean active;

        private aemkvoltyxggkb() {
        }
    }
}


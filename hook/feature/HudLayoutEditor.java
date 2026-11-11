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
        if (this.dragging != null) {
            adsdzhecwnzmbs var14 = this.places.computeIfAbsent(this.dragging, var0 -> new adsdzhecwnzmbs());
            float[] var15 = this.bounds.get((Object)this.dragging);
            if (var15 != null) {
                this.placeFromPixel(var14, (float)(var8 - this.grabX), (float)(var10 - this.grabY), var15[2], var15[3], var2, var3);
            }
        }
        var1.pushAlpha(var6);
        float var16 = this.d(10.0f);
        var16 = this.element(var1, HudSection.WATERMARK, var5.watermark, var2, var3, var16, var7, var8, var10, () -> this.watermarkSize(var1, var5), (var3x, var4x) -> this.drawWatermark(var1, var3x, var4x, var5));
        var16 = this.element(var1, HudSection.INFO, var5.info && var4.inWorld(), var2, var3, var16, var7, var8, var10, () -> this.infoSize(var1, var4), (var3x, var4x) -> this.drawInfo(var1, var3x, var4x, var4));
        var16 = this.element(var1, HudSection.PLAYER, var5.player && var4.inWorld(), var2, var3, var16, var7, var8, var10, () -> this.playerSize(var1, var4), (var3x, var4x) -> this.drawPlayer(var1, var3x, var4x, var4));
        this.element(var1, HudSection.COORDS, var5.coords && var4.inWorld(), var2, var3, var16, var7, var8, var10, () -> this.coordsSize(var1, var4, var5), (var4x, var5x) -> this.drawCoords(var1, var4x, var5x, var4, var5));
        this.drawModules(var1, var5.modules, var2, var3, var4, var7, var8, var10);
        var1.popAlpha();
    }

    private float d(float var1) {
        return var1 * this.u;
    }

    private float element(UiRenderer var1, HudSection var2, boolean var3, int var4, int var5, float var6, boolean var7, double var8, double var10, aetekhtbjzavpx var12, DrawAction var13) {
        adsdzhecwnzmbs var22;
        float var14 = this.anim("el:" + var2.name(), var3 ? 1.0f : 0.0f, 9.0f);
        if (var14 < 0.01f) {
            this.bounds.remove((Object)var2);
            return var6;
        }
        float[] var15 = var12.get();
        float var16 = var15[0];
        float var17 = var15[1];
        float[] var18 = this.position(var2, var16, var17, var4, var5, var6);
        float var19 = var18[0];
        float var20 = var18[1];
        this.bounds.put(var2, new float[]{var19, var20, var16, var17});
        float var21 = UiEasing.out(var14);
        var1.pushAlpha(var21);
        var1.pushTransform(0.0f, (1.0f - var21) * this.d(-6.0f), 1.0f);
        var13.draw(var19, var20);
        var1.popTransform();
        var1.popAlpha();
        if (var7) {
            this.editFrame(var1, var2, var19, var20, var16, var17, var8, var10);
        }
        boolean var23 = ((var22 = this.places.get((Object)var2)) == null || !var22.moved) && var2 != HudSection.COORDS;
        return var23 ? var6 + (var17 + this.d(7.0f)) * var21 : var6;
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
            float[] var21 = this.position(HudSection.MODULES, var41, var42, var3, var4, 0.0f);
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

    private float[] position(HudSection var1, float var2, float var3, int var4, int var5, float var6) {
        float[] fArray;
        adsdzhecwnzmbs var7 = this.places.get((Object)var1);
        if (var7 != null && var7.moved) {
            float var8 = var7.right ? (float)var4 - this.d(var7.ox) - var2 : this.d(var7.ox);
            float var9 = var7.bottom ? (float)var5 - this.d(var7.oy) - var3 : this.d(var7.oy);
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
        var1.right = var2 + var4 / 2.0f > (float)var6 / 2.0f;
        var1.bottom = var3 + var5 / 2.0f > (float)var7 / 2.0f;
        var1.ox = (var1.right ? (float)var6 - var2 - var4 : var2) / this.u;
        var1.oy = (var1.bottom ? (float)var7 - var3 - var5 : var3) / this.u;
    }

    private void editFrame(UiRenderer var1, HudSection var2, float var3, float var4, float var5, float var6, double var7, double var9) {
        boolean var11 = this.dragging == var2 || var7 >= (double)var3 && var7 < (double)(var3 + var5) && var9 >= (double)var4 && var9 < (double)(var4 + var6);
        float var12 = this.anim("edit:" + var2.name(), var11 ? 1.0f : 0.0f, 14.0f);
        float var13 = this.d(4.0f);
        var1.outline(var3 - var13, var4 - var13, var5 + var13 * 2.0f, var6 + var13 * 2.0f, this.d(10.0f), Math.max(1.0f, this.u), UiRenderer.white(0.22f + 0.35f * var12));
        float var14 = this.textW(var1, var2.label, this.d(10.0f), 1) + this.d(12.0f);
        float var15 = this.d(16.0f);
        boolean var16 = var3 + var5 / 2.0f > (float)this.lastSw / 2.0f;
        float var17 = var16 ? var3 - var13 - this.d(6.0f) - var14 : var3 + var5 + var13 + this.d(6.0f);
        float var18 = var2 == HudSection.MODULES ? var4 : var4 + (var6 - var15) / 2.0f;
        var1.pushAlpha(0.5f + 0.5f * var12);
        var1.round(var17, var18, var14, var15, var15 / 2.0f, UiRenderer.argb(0.85f, 20, 21, 25));
        var1.text(var2.label, var17 + this.d(6.0f), var18 + this.d(2.5f), this.d(10.0f), 1, UiRenderer.white(0.85f));
        var1.popAlpha();
    }

    public boolean mousePressed(double var1, double var3) {
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
        if (this.dragging != null) {
            this.dragging = null;
            this.save();
        }
    }

    public void resetLayout() {
        this.places.clear();
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

    private void load() {
        if (!this.loaded) {
            this.loaded = true;
            try {
                if (this.file != null && Files.isRegularFile(this.file, new LinkOption[0])) {
                    for (String var2 : Files.readAllLines(this.file, StandardCharsets.UTF_8)) {
                        String[] var3 = var2.trim().split(",");
                        if (var3.length != 5) continue;
                        adsdzhecwnzmbs var4 = new adsdzhecwnzmbs();
                        var4.moved = true;
                        var4.right = var3[1].equals("R");
                        var4.bottom = var3[2].equals("B");
                        var4.ox = Float.parseFloat(var3[3]);
                        var4.oy = Float.parseFloat(var3[4]);
                        this.places.put(HudSection.valueOf(var3[0]), var4);
                    }
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    private void save() {
        try {
            if (this.file != null) {
                StringBuilder var1 = new StringBuilder();
                for (Map.Entry<HudSection, adsdzhecwnzmbs> var3 : this.places.entrySet()) {
                    adsdzhecwnzmbs var4 = var3.getValue();
                    if (!var4.moved) continue;
                    var1.append(var3.getKey().name()).append(',').append(var4.right ? "R" : "L").append(',').append(var4.bottom ? "B" : "T").append(',').append(String.format(Locale.ROOT, "%.1f,%.1f", Float.valueOf(var4.ox), Float.valueOf(var4.oy))).append('\n');
                }
                Files.createDirectories(this.file.getParent(), new FileAttribute[0]);
                Files.writeString(this.file, (CharSequence)var1.toString(), StandardCharsets.UTF_8, new OpenOption[0]);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static enum HudSection {
        WATERMARK("Watermark"),
        INFO("Info"),
        PLAYER("Player"),
        COORDS("Coordinates"),
        MODULES("Modules");

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
    }

    private static final class adsdzhecwnzmbs {
        boolean moved;
        boolean right;
        boolean bottom;
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


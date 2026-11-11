/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_12178
 *  net.minecraft.class_12179
 *  net.minecraft.class_12180
 *  net.minecraft.class_1297
 *  net.minecraft.class_1922
 *  net.minecraft.class_2244
 *  net.minecraft.class_2281
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2350$class_2351
 *  net.minecraft.class_238
 *  net.minecraft.class_2382
 *  net.minecraft.class_239
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_259
 *  net.minecraft.class_265
 *  net.minecraft.class_2680
 *  net.minecraft.class_2741
 *  net.minecraft.class_2745
 *  net.minecraft.class_2756
 *  net.minecraft.class_2769
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_3726
 *  net.minecraft.class_3965
 *  net.minecraft.class_636
 *  net.minecraft.class_638
 *  net.minecraft.class_9848
 */
package hook.feature;

import hook.mixin.BlockOverlayInteractionAccessor;
import hook.mixin.HookGameRendererAccessor;
import hook.module.ext.ClientModule;
import hook.module.ext.EventHandler;
import hook.module.ext.ModuleCategory;
import hook.module.ext.event.Render3DEvent;
import hook.module.ext.setting.BooleanSetting;
import hook.module.ext.setting.ColorSetting;
import hook.module.ext.setting.ModeSetting;
import hook.module.ext.setting.NumberSetting;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12178;
import net.minecraft.class_12179;
import net.minecraft.class_12180;
import net.minecraft.class_1297;
import net.minecraft.class_1922;
import net.minecraft.class_2244;
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2745;
import net.minecraft.class_2756;
import net.minecraft.class_2769;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3726;
import net.minecraft.class_3965;
import net.minecraft.class_636;
import net.minecraft.class_638;
import net.minecraft.class_9848;

@Environment(value=EnvType.CLIENT)
public final class BlockOverlayModule
extends ClientModule {
    private static final String BOTH = "Both";
    private static final String OUTLINE = "Outline";
    private static final String FILL = "Fill";
    private static final String GRADIENT = "Gradient";
    private static final String SOLID = "Solid";
    private static final String RAINBOW = "Rainbow";
    private static final float FADE_IN = 0.12f;
    private static final float FADE_OUT = 0.2f;
    private static final double SNAP_DISTANCE = 6.0;
    private static final float FLOW_SPEED = 0.35f;
    private static final double SEGMENT = 0.125;
    private static final double FILL_INFLATE = 0.002;
    private static final double LINE_LIFT = 0.004;
    private static final double GLOW_LIFT = 0.002;
    private final ModeSetting style;
    private final ModeSetting colorMode;
    private final ColorSetting color;
    private final ColorSetting secondColor;
    private final BooleanSetting flow;
    private final NumberSetting fillOpacity;
    private final NumberSetting width;
    private final BooleanSetting glow;
    private final BooleanSetting smooth;
    private final NumberSetting smoothSpeed;
    private final BooleanSetting miningProgress;
    private final BooleanSetting faceOnly;
    private final BooleanSetting seeThrough;
    private class_638 level;
    private class_2338 pos;
    private class_2680 state;
    private class_2338 partner;
    private class_2680 partnerState;
    private final List<double[]> edges;
    private List<class_238> boxes;
    private class_238 localBounds;
    private class_238 goal;
    private class_238 box;
    private class_2350 face;
    private class_238 faceBox;
    private float presence;
    private float progress;
    private long lastFrame;
    private class_243 eye;
    private double time;
    private float fade;
    private double ax;
    private double bx;
    private double ay;
    private double by;
    private double az;
    private double bz;

    public BlockOverlayModule() {
        super("block-overlay", "Block Overlay", "A smooth, animated outline and fill on the block you look at.", ModuleCategory.VISUALS);
        String var10002 = "style";
        String var10005 = "Style";
        String var10006 = BOTH;
        String[] var10007 = new String[]{BOTH, OUTLINE, FILL};
        this.style = this.addSetting(var10002, new ModeSetting(var10005, var10006, var10007));
        var10002 = "color-mode";
        var10005 = "Color Mode";
        var10006 = GRADIENT;
        var10007 = new String[]{GRADIENT, SOLID, RAINBOW};
        this.colorMode = this.addSetting(var10002, new ModeSetting(var10005, var10006, var10007));
        this.color = this.addSetting("color", new ColorSetting("Color", -871878));
        this.secondColor = this.addSetting("second-color", new ColorSetting("Second Color", -6480));
        this.flow = this.addSetting("flow", new BooleanSetting("Flow", true));
        this.fillOpacity = this.addSetting("fill-opacity", new NumberSetting("Fill Opacity", 20.0f, 2.0f, 60.0f).step(1.0f));
        this.width = this.addSetting("width", new NumberSetting("Width", 2.0f, 1.0f, 5.0f).step(0.5f));
        this.glow = this.addSetting("glow", new BooleanSetting("Glow", true));
        this.smooth = this.addSetting("smooth", new BooleanSetting("Smooth", true));
        this.smoothSpeed = this.addSetting("smooth-speed", new NumberSetting("Smooth Speed", 16.0f, 4.0f, 40.0f).step(1.0f));
        this.miningProgress = this.addSetting("mining-progress", new BooleanSetting("Mining Progress", true));
        this.faceOnly = this.addSetting("face-only", new BooleanSetting("Face Only", false));
        this.seeThrough = this.addSetting("see-through", new BooleanSetting("See Through", false));
        this.edges = new ArrayList<double[]>();
        this.boxes = List.of();
        this.secondColor.visibleWhen(() -> this.colorMode.is(GRADIENT));
        this.flow.visibleWhen(() -> !this.colorMode.is(SOLID));
        this.fillOpacity.visibleWhen(() -> !this.style.is(OUTLINE));
        this.width.visibleWhen(() -> !this.style.is(FILL));
        this.glow.visibleWhen(() -> !this.style.is(FILL));
        this.smoothSpeed.visibleWhen(this.smooth::get);
    }

    @Override
    public void onDisable() {
        this.reset();
    }

    @EventHandler
    public void onRender3D(Render3DEvent var1) {
        class_310 var2 = class_310.method_1551();
        if (var2.field_1687 != null && var2.field_1724 != null) {
            if (var2.field_1687 != this.level) {
                this.reset();
                this.level = var2.field_1687;
            }
            long var3 = System.nanoTime();
            float var5 = this.lastFrame == 0L ? 0.0f : Math.min((float)(var3 - this.lastFrame) / 1.0E9f, 0.1f);
            this.lastFrame = var3;
            class_3965 var6 = BlockOverlayModule.target(var2);
            if (var6 != null && this.adopt(var2, var6)) {
                this.box = this.box != null && !(this.presence < 0.02f) && this.smooth.get() && !(this.box.method_1005().method_1022(this.goal.method_1005()) > 6.0) ? BlockOverlayModule.lerp(this.box, this.goal, 1.0f - (float)Math.exp(-this.smoothSpeed.get() * var5)) : this.goal;
                this.presence = Math.min(1.0f, this.presence + var5 / 0.12f);
            } else {
                var6 = null;
                this.presence = Math.max(0.0f, this.presence - var5 / 0.2f);
                if (this.presence == 0.0f) {
                    this.reset();
                    this.level = var2.field_1687;
                    return;
                }
            }
            if (this.box != null) {
                this.updateProgress(var2, var5, var6 != null);
                this.eye = var2.field_1773.method_19418().method_71156();
                this.time = (double)(var3 % 3600000000000L) / 1.0E9;
                this.draw();
            }
        } else {
            this.reset();
        }
    }

    private static class_3965 target(class_310 var0) {
        class_3965 var1;
        class_239 class_2392 = var0.field_1765;
        if (!(class_2392 instanceof class_3965) || (var1 = (class_3965)class_2392).method_17783() != class_239.class_240.field_1332 || var1.method_17781()) {
            return null;
        }
        if (!((HookGameRendererAccessor)var0.field_1773).hook$shouldRenderBlockOutline()) {
            return null;
        }
        class_2338 var3 = var1.method_17777();
        return !var0.field_1687.method_8320(var3).method_26215() && var0.field_1687.method_8621().method_11952(var3) ? var1 : null;
    }

    private boolean adopt(class_310 var1, class_3965 var2) {
        class_2680 var9;
        class_2338 var6;
        class_638 var3 = var1.field_1687;
        class_2338 var4 = var2.method_17777();
        class_2338 var5 = BlockOverlayModule.partner(var3, var4, var3.method_8320(var4));
        class_2338 class_23382 = var6 = var5 != null && var5.method_10265((class_2382)var4) < 0 ? var5 : var4;
        class_2338 var7 = var5 == null ? null : (var6.equals((Object)var4) ? var5 : var4);
        class_2680 var8 = var3.method_8320(var6);
        class_2680 class_26802 = var9 = var7 == null ? null : var3.method_8320(var7);
        if (!var6.equals((Object)this.pos) || var8 != this.state || var9 != this.partnerState) {
            class_1297 var10 = var1.method_1560();
            class_3726 var11 = var10 == null ? class_3726.method_16194() : class_3726.method_16195((class_1297)var10);
            class_265 var12 = var8.method_26172((class_1922)var3, var6, var11);
            if (var7 != null) {
                var12 = class_259.method_1084((class_265)var12, (class_265)var9.method_26172((class_1922)var3, var7, var11).method_66507((class_2382)var7.method_10059((class_2382)var6)));
            }
            if ((var12 = var12.method_1097()).method_1110()) {
                return false;
            }
            if (!var6.equals((Object)this.pos)) {
                this.progress = 0.0f;
            }
            this.pos = var6;
            this.state = var8;
            this.partner = var7;
            this.partnerState = var9;
            this.edges.clear();
            var12.method_1104((var1x, var3x, var5x, var7x, var9x, var11x) -> {
                double[] var10001 = new double[]{var1x, var3x, var5x, var7x, var9x, var11x};
                this.edges.add(var10001);
            });
            this.boxes = var12.method_1090();
            this.localBounds = var12.method_1107();
            this.goal = this.localBounds.method_996(var6);
        }
        this.face = var2.method_17780();
        this.faceBox = this.boxOnFace(var2.method_17784().method_1023((double)this.pos.method_10263(), (double)this.pos.method_10264(), (double)this.pos.method_10260()));
        return true;
    }

    private static class_2338 partner(class_638 var0, class_2338 var1, class_2680 var2) {
        class_2338 var3 = null;
        if (var2.method_28498((class_2769)class_2741.field_12533)) {
            var3 = var2.method_11654((class_2769)class_2741.field_12533) == class_2756.field_12607 ? var1.method_10084() : var1.method_10074();
        } else if (var2.method_26204() instanceof class_2244) {
            var3 = var1.method_10093(class_2244.method_24163((class_2680)var2));
        } else if (var2.method_26204() instanceof class_2281 && var2.method_11654((class_2769)class_2281.field_10770) != class_2745.field_12569) {
            var3 = class_2281.method_72581((class_2338)var1, (class_2680)var2);
        }
        return var3 != null && var0.method_8320(var3).method_27852(var2.method_26204()) ? var3 : null;
    }

    private class_238 boxOnFace(class_243 var1) {
        double var2 = 0.001;
        for (class_238 var5 : this.boxes) {
            boolean var6;
            if (!(var6 = (switch (this.face) {
                case class_2350.field_11034 -> {
                    if (Math.abs(var1.field_1352 - var5.field_1320) < var2) {
                        yield true;
                    }
                    yield false;
                }
                case class_2350.field_11039 -> {
                    if (Math.abs(var1.field_1352 - var5.field_1323) < var2) {
                        yield true;
                    }
                    yield false;
                }
                case class_2350.field_11036 -> {
                    if (Math.abs(var1.field_1351 - var5.field_1325) < var2) {
                        yield true;
                    }
                    yield false;
                }
                case class_2350.field_11033 -> {
                    if (Math.abs(var1.field_1351 - var5.field_1322) < var2) {
                        yield true;
                    }
                    yield false;
                }
                case class_2350.field_11035 -> {
                    if (Math.abs(var1.field_1350 - var5.field_1324) < var2) {
                        yield true;
                    }
                    yield false;
                }
                case class_2350.field_11043 -> {
                    if (Math.abs(var1.field_1350 - var5.field_1321) < var2) {
                        yield true;
                    }
                    yield false;
                }
                default -> throw new MatchException(null, null);
            })) || !var5.method_1014(var2).method_1006(var1)) continue;
            return var5;
        }
        return this.localBounds;
    }

    private void updateProgress(class_310 var1, float var2, boolean var3) {
        BlockOverlayInteractionAccessor var6;
        class_2338 var7;
        float var4 = 0.0f;
        class_636 var5 = var1.field_1761;
        if (var3 && this.miningProgress.get() && var5 != null && var5.method_2923() && (var7 = (var6 = (BlockOverlayInteractionAccessor)var5).hook$blockOverlayDestroyPos()) != null && (var7.equals((Object)this.pos) || var7.equals((Object)this.partner))) {
            var4 = class_3532.method_15363((float)var6.hook$blockOverlayDestroyProgress(), (float)0.0f, (float)1.0f);
        }
        this.progress += (var4 - this.progress) * (1.0f - (float)Math.exp(-18.0f * var2));
    }

    private void reset() {
        this.level = null;
        this.pos = null;
        this.state = null;
        this.partner = null;
        this.partnerState = null;
        this.edges.clear();
        this.boxes = List.of();
        this.localBounds = null;
        this.goal = null;
        this.box = null;
        this.face = null;
        this.faceBox = null;
        this.presence = 0.0f;
        this.progress = 0.0f;
        this.lastFrame = 0L;
    }

    private void draw() {
        this.fade = this.presence * this.presence * (3.0f - 2.0f * this.presence);
        double var1 = 0.94 + 0.06 * (double)this.fade;
        double[] var3 = BlockOverlayModule.axis(this.box.field_1323, this.box.field_1320, this.goal.field_1323, this.goal.field_1320, (double)this.pos.method_10263() - this.goal.field_1323, var1);
        double[] var4 = BlockOverlayModule.axis(this.box.field_1322, this.box.field_1325, this.goal.field_1322, this.goal.field_1325, (double)this.pos.method_10264() - this.goal.field_1322, var1);
        double[] var5 = BlockOverlayModule.axis(this.box.field_1321, this.box.field_1324, this.goal.field_1321, this.goal.field_1324, (double)this.pos.method_10260() - this.goal.field_1321, var1);
        this.ax = var3[0];
        this.bx = var3[1];
        this.ay = var4[0];
        this.by = var4[1];
        this.az = var5[0];
        this.bz = var5[1];
        boolean var6 = !this.style.is(FILL);
        boolean var7 = !this.style.is(OUTLINE);
        float var8 = this.fillOpacity.get() / 100.0f;
        if (var7) {
            if (this.faceOnly.get()) {
                this.faceFill(this.faceBox, this.face, var8, 0.002);
            } else {
                for (class_238 var10 : this.boxes) {
                    this.boxFill(var10, var8, 0.002);
                }
            }
        }
        if (this.progress > 0.01f) {
            this.progressFill(Math.min(0.45f, var8 * 1.4f + 0.16f));
        }
        if (var6) {
            this.outline(this.faceOnly.get() ? BlockOverlayModule.faceEdges(this.faceBox, this.face) : this.edges);
        }
    }

    private static double[] axis(double var0, double var2, double var4, double var6, double var8, double var10) {
        double var12 = var6 - var4;
        double var14 = var12 > 1.0E-6 ? (var2 - var0) / var12 : 1.0;
        double var16 = (var0 + var2) * 0.5;
        double var18 = var16 * (1.0 - var10) + (var0 + var8 * var14) * var10;
        double[] var10000 = new double[]{var18, var14 * var10};
        return var10000;
    }

    private class_243 world(double var1, double var3, double var5) {
        return new class_243(this.ax + this.bx * var1, this.ay + this.by * var3, this.az + this.bz * var5);
    }

    private void outline(List<double[]> var1) {
        boolean var2 = !this.colorMode.is(SOLID);
        ArrayList<double[]> var3 = new ArrayList<double[]>(var1.size() * 4);
        ArrayList<Integer> var4 = new ArrayList<Integer>(var1.size() * 4);
        for (double[] var6 : var1) {
            double var7 = Math.sqrt(BlockOverlayModule.sq(var6[3] - var6[0]) + BlockOverlayModule.sq(var6[4] - var6[1]) + BlockOverlayModule.sq(var6[5] - var6[2]));
            int var9 = var2 ? class_3532.method_15340((int)((int)Math.ceil(var7 / 0.125)), (int)1, (int)16) : 1;
            for (int var10 = 0; var10 < var9; ++var10) {
                double var11 = (double)var10 / (double)var9;
                double var13 = (double)(var10 + 1) / (double)var9;
                double var15 = (var11 + var13) * 0.5;
                double[] var10001 = new double[]{class_3532.method_16436((double)var11, (double)var6[0], (double)var6[3]), class_3532.method_16436((double)var11, (double)var6[1], (double)var6[4]), class_3532.method_16436((double)var11, (double)var6[2], (double)var6[5]), class_3532.method_16436((double)var13, (double)var6[0], (double)var6[3]), class_3532.method_16436((double)var13, (double)var6[1], (double)var6[4]), class_3532.method_16436((double)var13, (double)var6[2], (double)var6[5])};
                var3.add(var10001);
                var4.add(this.colorAt(class_3532.method_16436((double)var15, (double)var6[0], (double)var6[3]), class_3532.method_16436((double)var15, (double)var6[1], (double)var6[4]), class_3532.method_16436((double)var15, (double)var6[2], (double)var6[5])));
            }
        }
        float var17 = this.width.get();
        if (this.glow.get()) {
            for (int var18 = 0; var18 < var3.size(); ++var18) {
                double[] var20 = (double[])var3.get(var18);
                int var8 = (Integer)var4.get(var18);
                this.line(var20, class_9848.method_75601((int)var8, (float)(0.34f * this.fade)), var17 + 3.0f, 0.002);
                this.line(var20, class_9848.method_75601((int)var8, (float)(0.13f * this.fade)), var17 + 8.0f, 0.002);
            }
        }
        for (int var19 = 0; var19 < var3.size(); ++var19) {
            this.line((double[])var3.get(var19), class_9848.method_75601((int)((Integer)var4.get(var19)), (float)this.fade), var17, 0.004);
        }
    }

    private void line(double[] var1, int var2, float var3, double var4) {
        class_12178 var6 = class_12180.method_75546((class_243)this.lift(this.world(var1[0], var1[1], var1[2]), var4), (class_243)this.lift(this.world(var1[3], var1[4], var1[5]), var4), (int)var2, (float)var3);
        if (this.seeThrough.get()) {
            var6.method_75533();
        }
    }

    private class_243 lift(class_243 var1, double var2) {
        class_243 var4 = this.eye.method_1020(var1);
        double var5 = var4.method_1033();
        return var5 < 1.0E-6 ? var1 : var1.method_1019(var4.method_1021(var2 / var5));
    }

    private void boxFill(class_238 var1, float var2, double var3) {
        for (class_2350 var8 : class_2350.values()) {
            this.faceFill(var1, var8, var2, var3);
        }
    }

    private void faceFill(class_238 var1, class_2350 var2, float var3, double var4) {
        class_243 var6 = this.world(var1.field_1323, var1.field_1322, var1.field_1321).method_1023(var4, var4, var4);
        class_243 var7 = this.world(var1.field_1320, var1.field_1325, var1.field_1324).method_1031(var4, var4, var4);
        class_243 var8 = BlockOverlayModule.faceCentre(var1, var2);
        int var9 = this.colorAt(var8.field_1352, var8.field_1351, var8.field_1350);
        int var10 = class_3532.method_15340((int)Math.round(var3 * this.fade * 255.0f), (int)0, (int)255) << 24 | var9 & 0xFFFFFF;
        class_12178 var11 = class_12180.method_75548((class_243)var6, (class_243)var7, (class_2350)var2, (class_12179)class_12179.method_75539((int)var10));
        if (this.seeThrough.get()) {
            var11.method_75533();
        }
    }

    private void progressFill(float var1) {
        double var2 = 0.004;
        if (this.faceOnly.get()) {
            class_238 var4 = this.faceBox;
            class_238 var5 = this.face.method_10166() == class_2350.class_2351.field_11052 ? var4.method_35580(var4.method_17939() * (double)(1.0f - this.progress) * 0.5, 0.0, var4.method_17941() * (double)(1.0f - this.progress) * 0.5) : new class_238(var4.field_1323, var4.field_1322, var4.field_1321, var4.field_1320, var4.field_1322 + var4.method_17940() * (double)this.progress, var4.field_1324);
            this.faceFill(var5, this.face, var1, var2);
        } else {
            double var8 = this.localBounds.field_1322 + this.localBounds.method_17940() * (double)this.progress;
            for (class_238 var7 : this.boxes) {
                if (var7.field_1322 >= var8) continue;
                this.boxFill(new class_238(var7.field_1323, var7.field_1322, var7.field_1321, var7.field_1320, Math.min(var7.field_1325, var8), var7.field_1324), var1, var2);
            }
        }
    }

    private int colorAt(double var1, double var3, double var5) {
        double var10;
        int var7 = this.color.argb();
        if (this.colorMode.is(SOLID)) {
            return var7;
        }
        double var8 = (BlockOverlayModule.norm(var1, this.localBounds.field_1323, this.localBounds.field_1320) + BlockOverlayModule.norm(var3, this.localBounds.field_1322, this.localBounds.field_1325) + BlockOverlayModule.norm(var5, this.localBounds.field_1321, this.localBounds.field_1324)) / 3.0;
        double d = var10 = this.flow.get() ? this.time * (double)0.35f : 0.0;
        if (this.colorMode.is(RAINBOW)) {
            double var14 = var8 * 0.5 - var10;
            return var7 & 0xFF000000 | class_3532.method_15369((float)((float)(var14 - Math.floor(var14))), (float)0.65f, (float)1.0f) & 0xFFFFFF;
        }
        double var12 = this.flow.get() ? 0.5 - 0.5 * Math.cos(Math.PI * 2 * (var8 - var10)) : var8;
        return class_9848.method_61319((float)((float)var12), (int)var7, (int)this.secondColor.argb());
    }

    private static double norm(double var0, double var2, double var4) {
        return var4 - var2 < 1.0E-6 ? 0.5 : (var0 - var2) / (var4 - var2);
    }

    private static class_243 faceCentre(class_238 var0, class_2350 var1) {
        class_243 var2 = var0.method_1005();
        return switch (var1) {
            case class_2350.field_11034 -> new class_243(var0.field_1320, var2.field_1351, var2.field_1350);
            case class_2350.field_11039 -> new class_243(var0.field_1323, var2.field_1351, var2.field_1350);
            case class_2350.field_11036 -> new class_243(var2.field_1352, var0.field_1325, var2.field_1350);
            case class_2350.field_11033 -> new class_243(var2.field_1352, var0.field_1322, var2.field_1350);
            case class_2350.field_11035 -> new class_243(var2.field_1352, var2.field_1351, var0.field_1324);
            case class_2350.field_11043 -> new class_243(var2.field_1352, var2.field_1351, var0.field_1321);
            default -> throw new MatchException(null, null);
        };
    }

    private static List<double[]> faceEdges(class_238 var0, class_2350 var1) {
        double[][] var10000;
        switch (var1.method_10166()) {
            case field_11048: {
                double var8 = var1 == class_2350.field_11034 ? var0.field_1320 : var0.field_1323;
                var10000 = new double[4][];
                int var17 = 0;
                double[] var28 = new double[]{var8, var0.field_1322, var0.field_1321};
                var10000[var17] = var28;
                var17 = 1;
                var28 = new double[]{var8, var0.field_1325, var0.field_1321};
                var10000[var17] = var28;
                var17 = 2;
                var28 = new double[]{var8, var0.field_1325, var0.field_1324};
                var10000[var17] = var28;
                var17 = 3;
                var28 = new double[]{var8, var0.field_1322, var0.field_1324};
                var10000[var17] = var28;
                break;
            }
            case field_11052: {
                double var7 = var1 == class_2350.field_11036 ? var0.field_1325 : var0.field_1322;
                var10000 = new double[4][];
                int var13 = 0;
                double[] var24 = new double[]{var0.field_1323, var7, var0.field_1321};
                var10000[var13] = var24;
                var13 = 1;
                var24 = new double[]{var0.field_1320, var7, var0.field_1321};
                var10000[var13] = var24;
                var13 = 2;
                var24 = new double[]{var0.field_1320, var7, var0.field_1324};
                var10000[var13] = var24;
                var13 = 3;
                var24 = new double[]{var0.field_1323, var7, var0.field_1324};
                var10000[var13] = var24;
                break;
            }
            case field_11051: {
                double var3 = var1 == class_2350.field_11035 ? var0.field_1324 : var0.field_1321;
                var10000 = new double[4][];
                int var10002 = 0;
                double[] var10003 = new double[]{var0.field_1323, var0.field_1322, var3};
                var10000[var10002] = var10003;
                var10002 = 1;
                var10003 = new double[]{var0.field_1320, var0.field_1322, var3};
                var10000[var10002] = var10003;
                var10002 = 2;
                var10003 = new double[]{var0.field_1320, var0.field_1325, var3};
                var10000[var10002] = var10003;
                var10002 = 3;
                var10003 = new double[]{var0.field_1323, var0.field_1325, var3};
                var10000[var10002] = var10003;
                break;
            }
            default: {
                throw new MatchException(null, null);
            }
        }
        double[][] var2 = var10000;
        ArrayList<double[]> var9 = new ArrayList<double[]>(4);
        for (int var4 = 0; var4 < 4; ++var4) {
            double[] var5 = var2[var4];
            double[] var6 = var2[(var4 + 1) % 4];
            double[] var10001 = new double[]{var5[0], var5[1], var5[2], var6[0], var6[1], var6[2]};
            var9.add(var10001);
        }
        return var9;
    }

    private static class_238 lerp(class_238 var0, class_238 var1, float var2) {
        return new class_238(class_3532.method_16436((double)var2, (double)var0.field_1323, (double)var1.field_1323), class_3532.method_16436((double)var2, (double)var0.field_1322, (double)var1.field_1322), class_3532.method_16436((double)var2, (double)var0.field_1321, (double)var1.field_1321), class_3532.method_16436((double)var2, (double)var0.field_1320, (double)var1.field_1320), class_3532.method_16436((double)var2, (double)var0.field_1325, (double)var1.field_1325), class_3532.method_16436((double)var2, (double)var0.field_1324, (double)var1.field_1324));
    }

    private static double sq(double var0) {
        return var0 * var0;
    }
}


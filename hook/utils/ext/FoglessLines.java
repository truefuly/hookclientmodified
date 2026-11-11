/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_12179
 *  net.minecraft.class_12180
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_4184
 *  org.joml.Vector3fc
 */
package hook.utils.ext;

import hook.mixin.HookGameRendererAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12179;
import net.minecraft.class_12180;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public final class FoglessLines {
    private static final double NEAR = 0.05;
    private final class_243 eye;
    private final Vector3fc forward;
    private final double pixelPerDepth;

    private FoglessLines(class_243 var1, Vector3fc var2, double var3) {
        this.eye = var1;
        this.forward = var2;
        this.pixelPerDepth = var3;
    }

    public static FoglessLines forFrame(float var0) {
        class_4184 var1 = class_310.method_1551().field_1773.method_19418();
        return new FoglessLines(var1.method_71156(), var1.method_19335(), FoglessLines.pixelScale(var0));
    }

    public static double pixelScale(float var0) {
        class_310 var1 = class_310.method_1551();
        class_4184 var2 = var1.field_1773.method_19418();
        float var3 = ((HookGameRendererAccessor)var1.field_1773).hook$getFov(var2, var0, true);
        return 2.0 * Math.tan(Math.toRadians(var3) * 0.5) / (double)Math.max(1, var1.method_22683().method_4506());
    }

    public static FoglessLines forView(class_243 var0, Vector3fc var1, double var2) {
        return new FoglessLines(var0, var1, var2);
    }

    public void line(class_243 var1, class_243 var2, int var3, float var4) {
        double var5 = this.depth(var1);
        double var7 = this.depth(var2);
        if (!(var5 < 0.05) || !(var7 < 0.05)) {
            if (var5 < 0.05) {
                var1 = var1.method_35590(var2, (0.05 - var5) / (var7 - var5));
                var5 = 0.05;
            } else if (var7 < 0.05) {
                var2 = var2.method_35590(var1, (0.05 - var7) / (var5 - var7));
                var7 = 0.05;
            }
            class_243 var9 = var1.method_1020(this.eye).method_1036(var2.method_1020(var1));
            double var10 = var9.method_1033();
            if (!(var10 < 1.0E-9)) {
                double var12 = (double)var4 * 0.5 * this.pixelPerDepth / var10;
                class_243 var14 = var9.method_1021(var12 * var5);
                class_243 var15 = var9.method_1021(var12 * var7);
                class_12180.method_75547((class_243)var1.method_1019(var14), (class_243)var2.method_1019(var15), (class_243)var2.method_1020(var15), (class_243)var1.method_1020(var14), (class_12179)class_12179.method_75539((int)var3)).method_75533();
            }
        }
    }

    public void box(class_238 var1, int var2, float var3) {
        double var4 = var1.field_1323;
        double var6 = var1.field_1322;
        double var8 = var1.field_1321;
        double var10 = var1.field_1320;
        double var12 = var1.field_1325;
        double var14 = var1.field_1324;
        for (double var19 : new double[]{var6, var12}) {
            this.line(new class_243(var4, var19, var8), new class_243(var10, var19, var8), var2, var3);
            this.line(new class_243(var10, var19, var8), new class_243(var10, var19, var14), var2, var3);
            this.line(new class_243(var10, var19, var14), new class_243(var4, var19, var14), var2, var3);
            this.line(new class_243(var4, var19, var14), new class_243(var4, var19, var8), var2, var3);
        }
        this.line(new class_243(var4, var6, var8), new class_243(var4, var12, var8), var2, var3);
        this.line(new class_243(var10, var6, var8), new class_243(var10, var12, var8), var2, var3);
        this.line(new class_243(var10, var6, var14), new class_243(var10, var12, var14), var2, var3);
        this.line(new class_243(var4, var6, var14), new class_243(var4, var12, var14), var2, var3);
    }

    private double depth(class_243 var1) {
        return (var1.field_1352 - this.eye.field_1352) * (double)this.forward.x() + (var1.field_1351 - this.eye.field_1351) * (double)this.forward.y() + (var1.field_1350 - this.eye.field_1350) * (double)this.forward.z();
    }
}


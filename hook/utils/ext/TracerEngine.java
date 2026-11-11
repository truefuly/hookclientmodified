/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_408
 *  net.minecraft.class_4184
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 */
package hook.utils.ext;

import hook.utils.RenderVisibility;
import hook.utils.ext.FoglessLines;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_408;
import net.minecraft.class_4184;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

@Environment(value=EnvType.CLIENT)
public final class TracerEngine {
    private static final float WIDTH = 1.0f;
    private static boolean active;
    private static class_243 cameraPos;
    private static class_243 origin;
    private static FoglessLines lines;
    private static final Matrix4f viewProjection;

    private TracerEngine() {
    }

    public static void beginFrame(Matrix4fc var0, Matrix4fc var1) {
        class_4184 var3;
        RenderVisibility.beginWorldFrame(var0, var1);
        active = false;
        lines = null;
        class_310 var2 = class_310.method_1551();
        if (var2.field_1724 != null && var2.field_1687 != null && (var2.field_1755 == null || var2.field_1755 instanceof class_408) && (var3 = var2.field_1773.method_19418()) != null && var3.method_19332()) {
            viewProjection.set(var1).mul(var0);
            Matrix4f var4 = viewProjection.invert(new Matrix4f());
            Vector4f var5 = var4.transform(new Vector4f(0.0f, 0.0f, 0.0f, 1.0f));
            Vector4f var6 = var4.transform(new Vector4f(0.0f, 0.0f, -1.0f, 0.0f));
            cameraPos = var3.method_71156();
            origin = cameraPos.method_1031((double)(var5.x / var5.w), (double)(var5.y / var5.w), (double)(var5.z / var5.w));
            class_243 var7 = cameraPos.method_1031((double)(var6.x / var6.w), (double)(var6.y / var6.w), (double)(var6.z / var6.w));
            Vector3f var8 = new Vector3f((float)(TracerEngine.origin.field_1352 - var7.field_1352), (float)(TracerEngine.origin.field_1351 - var7.field_1351), (float)(TracerEngine.origin.field_1350 - var7.field_1350)).normalize();
            double var9 = 2.0 / (double)(var1.m11() * (float)Math.max(1, var2.method_22683().method_4506()));
            lines = FoglessLines.forView(var7, (Vector3fc)var8, var9);
            active = true;
        }
    }

    public static void draw(class_243 var0, int var1) {
        if (active) {
            lines.line(origin, var0, var1, 1.0f);
        }
    }

    public static void stem(class_243 var0, double var1, int var3) {
        if (active) {
            lines.line(var0, var0.method_1031(0.0, var1, 0.0), var3, 1.0f);
        }
    }

    public static Vector4f clip(class_243 var0) {
        return !active ? null : viewProjection.transform(new Vector4f((float)(var0.field_1352 - TracerEngine.cameraPos.field_1352), (float)(var0.field_1351 - TracerEngine.cameraPos.field_1351), (float)(var0.field_1350 - TracerEngine.cameraPos.field_1350), 1.0f));
    }

    public static boolean isInFront(class_4184 var0, class_243 var1) {
        class_243 var2 = var1.method_1020(var0.method_71156());
        Vector3fc var3 = var0.method_19335();
        return var2.field_1352 * (double)var3.x() + var2.field_1351 * (double)var3.y() + var2.field_1350 * (double)var3.z() > 0.05;
    }

    static {
        cameraPos = class_243.field_1353;
        origin = class_243.field_1353;
        viewProjection = new Matrix4f();
    }
}


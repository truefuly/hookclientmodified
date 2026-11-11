/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_10366
 *  net.minecraft.class_11286
 *  net.minecraft.class_12075
 *  net.minecraft.class_12160
 *  net.minecraft.class_12176
 *  net.minecraft.class_12177
 *  net.minecraft.class_12180
 *  net.minecraft.class_12180$class_12181
 *  net.minecraft.class_12185
 *  net.minecraft.class_12185$class_12186
 *  net.minecraft.class_156
 *  net.minecraft.class_310
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  net.minecraft.class_4597$class_4598
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package hook.utils.ext;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_10366;
import net.minecraft.class_11286;
import net.minecraft.class_12075;
import net.minecraft.class_12160;
import net.minecraft.class_12176;
import net.minecraft.class_12177;
import net.minecraft.class_12180;
import net.minecraft.class_12185;
import net.minecraft.class_156;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public final class ShaderOverlay {
    private static final MethodHandle SHADER_PACK_IN_USE = ShaderOverlay.irisProbe();
    private static final class_12185 COLLECTOR = new class_12185();
    private static class_11286 projectionBuffer;

    private ShaderOverlay() {
    }

    private static MethodHandle irisProbe() {
        if (!FabricLoader.getInstance().isModLoaded("iris")) {
            return null;
        }
        try {
            Class<?> var0 = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object var1 = var0.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
            return MethodHandles.publicLookup().findVirtual(var0, "isShaderPackInUse", MethodType.methodType(Boolean.TYPE)).bindTo(var1);
        }
        catch (LinkageError | ReflectiveOperationException | RuntimeException var2) {
            return null;
        }
    }

    public static boolean shaderPackInUse() {
        if (SHADER_PACK_IN_USE == null) {
            return false;
        }
        try {
            return (boolean) SHADER_PACK_IN_USE.invokeExact();
        }
        catch (Throwable var1) {
            return false;
        }
    }

    public static class_12180.class_12181 capture() {
        if (!ShaderOverlay.shaderPackInUse()) {
            return null;
        }
        if (ShaderOverlay.pending()) {
            COLLECTOR.method_75558();
        }
        return class_12180.method_75554((class_12176)COLLECTOR);
    }

    public static boolean pending() {
        return !COLLECTOR.method_75560().isEmpty();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(class_4184 var0, Matrix4f var1, Matrix4f var2, GpuBufferSlice var3, GpuBufferSlice var4) {
        if (ShaderOverlay.pending()) {
            class_12160 var5 = new class_12160();
            class_12160 var6 = new class_12160();
            long var7 = class_156.method_658();
            for (class_12185.class_12186 var10 : COLLECTOR.method_75558()) {
                var10.method_75563().method_75531((class_12177)(var10.method_75562() ? var6 : var5), var10.method_75857(var7));
            }
            if (!var5.method_76659() || !var6.method_76659()) {
                class_12075 var18 = new class_12075();
                var18.field_63079 = var0.method_19332();
                var18.field_63078 = var0.method_71156();
                var18.field_63077 = var0.method_19328();
                var18.field_63081 = new Quaternionf((Quaternionfc)var0.method_23767());
                class_310 var19 = class_310.method_1551();
                if (projectionBuffer == null) {
                    projectionBuffer = new class_11286("hook overlay");
                }
                class_4597.class_4598 var11 = var19.method_22940().method_23000();
                class_4587 var12 = new class_4587();
                Matrix4fStack var13 = RenderSystem.getModelViewStack();
                GpuBufferSlice var14 = RenderSystem.getShaderFog();
                RenderSystem.backupProjectionMatrix();
                RenderSystem.setProjectionMatrix((GpuBufferSlice)projectionBuffer.method_71123(var2), (class_10366)class_10366.field_54953);
                var13.pushMatrix();
                var13.mul((Matrix4fc)var1);
                try {
                    if (!var5.method_76659()) {
                        RenderSystem.setShaderFog((GpuBufferSlice)var3);
                        var5.method_75477(var12, (class_4597)var11, var18, var1);
                        var11.method_22993();
                    }
                    if (!var6.method_76659()) {
                        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(var19.method_1522().method_30278(), 1.0);
                        RenderSystem.setShaderFog((GpuBufferSlice)var4);
                        var6.method_75477(var12, (class_4597)var11, var18, var1);
                        var11.method_22993();
                    }
                }
                finally {
                    var13.popMatrix();
                    RenderSystem.restoreProjectionMatrix();
                    RenderSystem.setShaderFog((GpuBufferSlice)var14);
                }
            }
        }
    }
}


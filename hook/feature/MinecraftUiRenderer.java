/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_10799
 *  net.minecraft.class_11231
 *  net.minecraft.class_11244
 *  net.minecraft.class_11246
 *  net.minecraft.class_12137
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_4588
 *  net.minecraft.class_7532
 *  net.minecraft.class_8030
 *  net.minecraft.class_8685
 *  org.joml.Matrix3x2fStack
 *  org.joml.Matrix3x2fc
 */
package hook.feature;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import hook.feature.UiRenderer;
import hook.feature.UiTextureData;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_11231;
import net.minecraft.class_11244;
import net.minecraft.class_11246;
import net.minecraft.class_12137;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4588;
import net.minecraft.class_7532;
import net.minecraft.class_8030;
import net.minecraft.class_8685;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2fc;

public final class MinecraftUiRenderer
extends UiRenderer {
    private static final Map<class_1792, class_1799> STACKS = new HashMap<class_1792, class_1799>();
    private static Supplier<class_8685> profileSkin;
    private final class_332 ctx;
    private final float guiScale;
    private int pushed;
    private static final Field STATE;
    private static final Field SCISSORS;
    private static final Method PEEK;
    private final class_11246 state;
    private final Deque<class_8030> clips = new ArrayDeque<class_8030>();
    private final class_8030 inheritedClip;
    private lIIllI batch;
    private UiTextureData batchTexture;
    private class_8030 clip;

    private static Field stateField() {
        for (Field var3 : class_332.class.getDeclaredFields()) {
            if (var3.getType() != class_11246.class) continue;
            var3.setAccessible(true);
            return var3;
        }
        throw new IllegalStateException("Missing GUI render state");
    }

    private static Field scissorField() {
        for (Field var3 : class_332.class.getDeclaredFields()) {
            if (var3.getType().getEnclosingClass() != class_332.class) continue;
            for (Method var7 : var3.getType().getDeclaredMethods()) {
                if (var7.getParameterCount() != 0 || var7.getReturnType() != class_8030.class) continue;
                var3.setAccessible(true);
                return var3;
            }
        }
        throw new IllegalStateException("Missing GUI clipping state");
    }

    private static Method scissorPeek() {
        for (Method var3 : SCISSORS.getType().getDeclaredMethods()) {
            if (var3.getParameterCount() != 0 || var3.getReturnType() != class_8030.class || !var3.getName().equals("peekLast") && !var3.getName().equals("method_70863")) continue;
            var3.setAccessible(true);
            return var3;
        }
        throw new IllegalStateException("Missing GUI clip accessor");
    }

    public MinecraftUiRenderer(class_332 var1, float var2) {
        this.ctx = var1;
        this.guiScale = var2 <= 0.0f ? 1.0f : var2;
        try {
            this.state = (class_11246)STATE.get(var1);
            this.clip = this.inheritedClip = (class_8030)PEEK.invoke(SCISSORS.get(var1), new Object[0]);
        }
        catch (ReflectiveOperationException var4) {
            throw new IllegalStateException("Cannot access GUI drawing state", var4);
        }
    }

    @Override
    protected void fillRaw(int var1, int var2, int var3, int var4, int var5) {
        if (var3 > var1 && var4 > var2) {
            this.quad(null, var1, var2, var3, var4, 0.0f, 0.0f, 0.0f, 0.0f, var5, var5);
        }
    }

    @Override
    protected void gradientRaw(int var1, int var2, int var3, int var4, int var5, int var6) {
        if (var3 > var1 && var4 > var2) {
            this.quad(null, var1, var2, var3, var4, 0.0f, 0.0f, 0.0f, 0.0f, var5, var6);
        }
    }

    @Override
    protected void blitRaw(UiTextureData var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10) {
        class_2960 var11;
        if (var4 > 0 && var5 > 0 && (var11 = MinecraftUiRenderer.upload(var1)) != null) {
            this.quad(var1, var2, var3, var2 + var4, var3 + var5, (float)var6 / (float)var1.w, (float)var7 / (float)var1.h, (float)(var6 + var8) / (float)var1.w, (float)(var7 + var9) / (float)var1.h, var10, var10);
        }
    }

    private void quad(UiTextureData var1, int var2, int var3, int var4, int var5, float var6, float var7, float var8, float var9, int var10, int var11) {
        Matrix3x2fStack var12 = this.ctx.method_51448();
        if (this.batch != null && this.batchTexture == var1) {
            this.batch.append((Matrix3x2fc)var12, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11);
        } else {
            lIIllI var16;
            class_11231 var13 = class_11231.method_70899();
            if (var1 != null) {
                class_2960 var14 = (class_2960)var1.handle;
                class_1044 var15 = class_310.method_1551().method_1531().method_4619(var14);
                var13 = class_11231.method_70900((GpuTextureView)var15.method_71659(), (class_12137)var15.method_75484());
            }
            if (!(var16 = new lIIllI(var1 == null ? class_10799.field_56879 : class_10799.field_56883, var13, this.clip)).append((Matrix3x2fc)var12, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11)) {
                return;
            }
            this.state.method_71059();
            this.state.method_70919((class_11244)var16);
            this.batch = var16;
            this.batchTexture = var1;
        }
    }

    private void barrier() {
        this.batch = null;
        this.batchTexture = null;
    }

    @Override
    public void pushTransform(float var1, float var2, float var3) {
        Matrix3x2fStack var4 = this.ctx.method_51448();
        var4.pushMatrix();
        var4.translate(var1, var2);
        var4.scale(var3, var3);
        ++this.pushed;
    }

    @Override
    public void popTransform() {
        if (this.pushed > 0) {
            --this.pushed;
            this.ctx.method_51448().popMatrix();
        }
    }

    @Override
    public float scale() {
        Matrix3x2fStack var1 = this.ctx.method_51448();
        float var2 = (float)Math.sqrt(var1.m00() * var1.m00() + var1.m01() * var1.m01());
        return var2 * this.guiScale;
    }

    @Override
    public void pushClip(float var1, float var2, float var3, float var4) {
        class_8030 var6;
        this.ctx.method_44379(Math.round(var1), Math.round(var2), Math.round(var1 + var3), Math.round(var2 + var4));
        class_8030 var5 = new class_8030(Math.round(var1), Math.round(var2), Math.round(var1 + var3) - Math.round(var1), Math.round(var2 + var4) - Math.round(var2)).method_65185((Matrix3x2fc)this.ctx.method_51448());
        class_8030 class_80302 = var6 = this.clips.isEmpty() ? this.inheritedClip : this.clips.peek();
        if (var6 != null) {
            var5 = var6.method_49701(var5);
        }
        if (var5 == null) {
            var5 = class_8030.method_48248();
        }
        this.clips.push(var5);
        this.clip = var5;
        this.barrier();
    }

    @Override
    public void popClip() {
        this.ctx.method_44380();
        if (!this.clips.isEmpty()) {
            this.clips.pop();
        }
        this.clip = this.clips.isEmpty() ? this.inheritedClip : this.clips.peek();
        this.barrier();
    }

    @Override
    public void head(float var1, float var2, float var3) {
        this.barrier();
        this.state.method_71059();
        try {
            class_8685 var5;
            class_310 var4 = class_310.method_1551();
            if (var4.field_1724 != null) {
                var5 = var4.field_1724.method_52814();
            } else {
                if (profileSkin == null) {
                    profileSkin = var4.method_1582().method_73544(var4.method_53462(), false);
                }
                var5 = profileSkin.get();
            }
            if (var5 != null) {
                this.ctx.method_51448().pushMatrix();
                this.ctx.method_51448().translate((float)Math.round(var1), (float)Math.round(var2));
                class_7532.method_52722((class_332)this.ctx, (class_8685)var5, (int)0, (int)0, (int)Math.round(var3));
                this.ctx.method_51448().popMatrix();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void item(Object var1, float var2, float var3, float var4) {
        this.barrier();
        this.state.method_71059();
        if (var1 instanceof class_1792) {
            class_1792 var5 = (class_1792)var1;
            class_1799 var6 = STACKS.computeIfAbsent(var5, class_1799::new);
            Matrix3x2fStack var7 = this.ctx.method_51448();
            var7.pushMatrix();
            var7.translate(var2, var3);
            var7.scale(var4 / 16.0f, var4 / 16.0f);
            this.ctx.method_51427(var6, 0, 0);
            var7.popMatrix();
        }
    }

    static class_2960 upload(UiTextureData var0) {
        Object object = var0.handle;
        if (object instanceof class_2960) {
            class_2960 var2 = (class_2960)object;
            return var2;
        }
        if (var0.handle != null) {
            return null;
        }
        try {
            class_1011 image = new class_1011(var0.w, var0.h, false);
            for (int var3 = 0; var3 < var0.h; ++var3) {
                for (int var4 = 0; var4 < var0.w; ++var4) {
                    image.method_61941(var4, var3, var0.argb[var3 * var0.w + var4]);
                }
            }
            String var7 = "kui/" + var0.key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
            class_2960 var8 = class_2960.method_60655((String)"hook", (String)var7);
            class_310.method_1551().method_1531().method_4616(var8, (class_1044)new UiTexture(var0.key, image));
            var0.handle = var8;
            return var8;
        }
        catch (Throwable var5) {
            System.err.println("Hook: texture upload failed for " + var0.key + ": " + String.valueOf(var5));
            var0.handle = Boolean.FALSE;
            return null;
        }
    }

    static {
        STATE = MinecraftUiRenderer.stateField();
        SCISSORS = MinecraftUiRenderer.scissorField();
        PEEK = MinecraftUiRenderer.scissorPeek();
    }

    static final class lIIllI
    implements class_11244 {
        private final RenderPipeline pipeline;
        private final class_11231 texture;
        private final class_8030 clip;
        private float[] xyuv = new float[256];
        private int[] colors = new int[64];
        private int vertices;
        private int left = Integer.MAX_VALUE;
        private int top = Integer.MAX_VALUE;
        private int right = Integer.MIN_VALUE;
        private int bottom = Integer.MIN_VALUE;

        lIIllI(RenderPipeline var1, class_11231 var2, class_8030 var3) {
            this.pipeline = var1;
            this.texture = var2;
            this.clip = var3;
        }

        boolean append(Matrix3x2fc var1, int var2, int var3, int var4, int var5, float var6, float var7, float var8, float var9, int var10, int var11) {
            float var12 = var1.m00() * (float)var2 + var1.m10() * (float)var3 + var1.m20();
            float var13 = var1.m01() * (float)var2 + var1.m11() * (float)var3 + var1.m21();
            float var14 = var1.m00() * (float)var2 + var1.m10() * (float)var5 + var1.m20();
            float var15 = var1.m01() * (float)var2 + var1.m11() * (float)var5 + var1.m21();
            float var16 = var1.m00() * (float)var4 + var1.m10() * (float)var5 + var1.m20();
            float var17 = var1.m01() * (float)var4 + var1.m11() * (float)var5 + var1.m21();
            float var18 = var1.m00() * (float)var4 + var1.m10() * (float)var3 + var1.m20();
            float var19 = var1.m01() * (float)var4 + var1.m11() * (float)var3 + var1.m21();
            int var20 = (int)Math.floor(Math.min(Math.min(var12, var14), Math.min(var16, var18)));
            int var21 = (int)Math.floor(Math.min(Math.min(var13, var15), Math.min(var17, var19)));
            int var22 = (int)Math.ceil(Math.max(Math.max(var12, var14), Math.max(var16, var18)));
            int var23 = (int)Math.ceil(Math.max(Math.max(var13, var15), Math.max(var17, var19)));
            if (this.clip == null || var22 > this.clip.method_49620() && var20 < this.clip.method_49621() && var23 > this.clip.method_49618() && var21 < this.clip.method_49619()) {
                if (this.vertices + 4 > this.colors.length) {
                    this.colors = Arrays.copyOf(this.colors, this.colors.length * 2);
                    this.xyuv = Arrays.copyOf(this.xyuv, this.xyuv.length * 2);
                }
                this.vertex(var12, var13, var6, var7, var10);
                this.vertex(var14, var15, var6, var9, var11);
                this.vertex(var16, var17, var8, var9, var11);
                this.vertex(var18, var19, var8, var7, var10);
                this.left = Math.min(this.left, var20);
                this.top = Math.min(this.top, var21);
                this.right = Math.max(this.right, var22);
                this.bottom = Math.max(this.bottom, var23);
                return true;
            }
            return false;
        }

        private void vertex(float var1, float var2, float var3, float var4, int var5) {
            int var6 = this.vertices * 4;
            this.xyuv[var6] = var1;
            this.xyuv[var6 + 1] = var2;
            this.xyuv[var6 + 2] = var3;
            this.xyuv[var6 + 3] = var4;
            this.colors[this.vertices++] = var5;
        }

        public void method_70917(class_4588 var1) {
            for (int var2 = 0; var2 < this.vertices; ++var2) {
                int var3 = var2 * 4;
                class_4588 var4 = var1.method_22912(this.xyuv[var3], this.xyuv[var3 + 1], 0.0f);
                if (this.pipeline != class_10799.field_56879) {
                    var4.method_22913(this.xyuv[var3 + 2], this.xyuv[var3 + 3]);
                }
                var4.method_39415(this.colors[var2]);
            }
        }

        public RenderPipeline comp_4055() {
            return this.pipeline;
        }

        public class_11231 comp_4056() {
            return this.texture;
        }

        public class_8030 comp_4069() {
            return this.clip;
        }

        public class_8030 comp_4274() {
            class_8030 var1 = new class_8030(this.left, this.top, this.right - this.left, this.bottom - this.top);
            return this.clip == null ? var1 : var1.method_49701(this.clip);
        }
    }

    private static final class UiTexture
    extends class_1043 {
        private UiTexture(String var1, class_1011 var2) {
            super(() -> "Hook " + var1, var2);
            this.field_63613 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        }
    }
}


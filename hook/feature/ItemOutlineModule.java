/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  net.minecraft.class_10444$class_10445
 *  net.minecraft.class_12247
 *  net.minecraft.class_12247$class_12248
 *  net.minecraft.class_12247$class_4750
 *  net.minecraft.class_1802
 *  net.minecraft.class_1921
 *  net.minecraft.class_2960
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  net.minecraft.class_4618
 *  net.minecraft.class_777
 *  net.minecraft.class_811
 *  net.minecraft.class_918
 */
package hook.feature;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import hook.Hook;
import hook.mixin.RenderLayerAccessor;
import hook.mixin.RenderPipelinesAccessor;
import hook.mixin.RenderSetupAccessor;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.StorageBlocksSetting;
import hook.utils.ColorUtil;
import hook.utils.EncryptedString;
import java.awt.Color;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.class_10444;
import net.minecraft.class_12247;
import net.minecraft.class_1802;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4618;
import net.minecraft.class_777;
import net.minecraft.class_811;
import net.minecraft.class_918;

public final class ItemOutlineModule
extends Module {
    private final StorageBlocksSetting color = new StorageBlocksSetting(EncryptedString.of("Color")).add("outline", "Outline", class_1802.field_8601, true, new Color(255, 75, 255)).colorOnly();
    private final BooleanSetting rainbow = new BooleanSetting(EncryptedString.of("Rainbow"), false).setDescription(EncryptedString.of("Cycles the outline through the rainbow"));
    private final NumberSetting rainbowSpeed = new NumberSetting(EncryptedString.of("Rainbow Speed"), 0.1, 10.0, 2.0, 0.1);
    private final BooleanSetting pulse = new BooleanSetting(EncryptedString.of("Pulse"), true).setDescription(EncryptedString.of("Makes the glow breathe in and out"));
    private final NumberSetting thickness = new NumberSetting(EncryptedString.of("Thickness"), 1.0, 15.0, 5.0, 0.5);
    private final NumberSetting fade = new NumberSetting(EncryptedString.of("Fade"), 0.0, 100.0, 75.0, 1.0).getValue(EncryptedString.of("Softens the outer glow; 0 keeps only the crisp rim"));
    private static final Map<class_1921, class_1921> OUTLINE_LAYERS = new WeakHashMap<class_1921, class_1921>();
    private static volatile boolean renderingOutline;
    private static volatile boolean failed;
    private static RenderPipeline outlinePipeline;
    private static boolean pipelineAttempted;

    public ItemOutlineModule() {
        super(EncryptedString.of("Item Outline"), EncryptedString.of("Adds an animated neon outline to items in your first-person hands"), -1, Category.VISUALS);
        this.addSettings(this.color, this.rainbow, this.rainbowSpeed, this.pulse, this.thickness, this.fade);
    }

    private int currentRgb() {
        Color selected = this.rainbow.getValue() ? ColorUtil.rainbow(this.rainbowSpeed.getValue(), 0, 255) : this.color.entries().get(0).color();
        return (selected.getRed() & 0xFF) << 16 | (selected.getGreen() & 0xFF) << 8 | selected.getBlue() & 0xFF;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void renderHeldItem(class_811 display, class_4587 matrices, class_4597 vertices, int light, int overlay, int[] colors, List<class_777> quads, class_1921 originalLayer) {
        class_1921 outlineLayer;
        ItemOutlineModule module;
        if (!failed && !renderingOutline && ItemOutlineModule.isFirstPersonHand(display) && (module = ItemOutlineModule.getModule()) != null && module.isEnabled() && (outlineLayer = ItemOutlineModule.getOutlineLayer(originalLayer)) != null) {
            renderingOutline = true;
            try {
                class_4618 outlines = new class_4618();
                float thickness = module.thickness.getFloatValue() * 0.01f;
                float fade = (float)module.fade.getValue() / 100.0f;
                int rgb = module.currentRgb();
                float pulse = 1.0f;
                if (module.pulse.getValue()) {
                    pulse = 0.72f + 0.28f * (float)(0.5 + 0.5 * Math.sin((double)System.nanoTime() / 4.2E8));
                }
                for (int layer = 3; layer > 0; --layer) {
                    float layerPulse = layer == 1 ? 1.0f : pulse;
                    float expansion = thickness * (float)layer / 3.0f * (layer == 1 ? 1.0f : 0.85f + 0.3f * pulse);
                    int alpha = layer == 1 ? 255 : Math.round(255.0f * fade * (layer == 2 ? 0.52f : 0.24f) * layerPulse);
                    outlines.method_23286(alpha << 24 | rgb);
                    matrices.method_22903();
                    try {
                        matrices.method_22904(0.5, 0.5, 0.5);
                        matrices.method_22905(expansion + 1.0f, expansion + 1.0f, expansion + 1.0f);
                        matrices.method_22904(-0.5, -0.5, -0.5);
                        class_918.method_62476((class_811)display, (class_4587)matrices, (class_4597)outlines, (int)light, (int)overlay, (int[])colors, quads, (class_1921)outlineLayer, (class_10444.class_10445)class_10444.class_10445.field_55341);
                        outlines.method_23285();
                        continue;
                    }
                    finally {
                        matrices.method_22909();
                    }
                }
                return;
            }
            catch (Throwable var28) {
                failed = true;
                System.err.println("Hook: held-item outline disabled after render error: " + String.valueOf(var28));
            }
            finally {
                renderingOutline = false;
            }
        }
    }

    private static ItemOutlineModule getModule() {
        try {
            ItemOutlineModule outline;
            if (Hook.INSTANCE == null || Hook.INSTANCE.getModuleManager() == null) {
                return null;
            }
            Module module = Hook.INSTANCE.getModuleManager().getModuleByClass(ItemOutlineModule.class);
            return module instanceof ItemOutlineModule ? (outline = (ItemOutlineModule)module) : null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean isFirstPersonHand(class_811 display) {
        return display == class_811.field_4321 || display == class_811.field_4322;
    }

    private static class_1921 getOutlineLayer(class_1921 original) {
        if (original == null) {
            return null;
        }
        class_1921 cached = OUTLINE_LAYERS.get(original);
        if (cached != null) {
            return cached;
        }
        try {
            Object sampler;
            class_12247 setup = ((RenderLayerAccessor)original).hook$getRenderSetup();
            Map<?, ?> textures = ((RenderSetupAccessor)setup).hook$getTextures();
            Object object = sampler = textures == null ? null : (Object)textures.get("Sampler0");
            if (sampler == null) {
                return null;
            }
            Method location = sampler.getClass().getDeclaredMethod("location", new Class[0]);
            location.setAccessible(true);
            class_2960 texture = (class_2960)location.invoke(sampler, new Object[0]);
            if (texture == null) {
                return null;
            }
            class_12247.class_12248 builder = class_12247.method_75927((RenderPipeline)ItemOutlineModule.getOutlinePipeline());
            builder.method_75934("Sampler0", texture);
            builder.method_75932(class_12247.class_4750.field_21854);
            class_1921 outline = RenderLayerAccessor.hook$create("hook_held_item_outline", builder.method_75938());
            OUTLINE_LAYERS.put(original, outline);
            return outline;
        }
        catch (Throwable failure) {
            failed = true;
            System.err.println("Hook: could not create held-item outline layer: " + String.valueOf(failure));
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static RenderPipeline getOutlinePipeline() {
        if (pipelineAttempted) {
            return outlinePipeline;
        }
        Class<ItemOutlineModule> clazz = ItemOutlineModule.class;
        synchronized (ItemOutlineModule.class) {
            if (pipelineAttempted) {
                // ** MonitorExit[var0] (shouldn't be in output)
                return outlinePipeline;
            }
            try {
                RenderPipeline.Snippet outlineSnippet = RenderPipelinesAccessor.hook$getOutlineSnippet();
                outlinePipeline = RenderPipelinesAccessor.hook$register(RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{outlineSnippet}).withLocation("pipeline/hook_held_item_outline").withFragmentShader("core/hook_held_item_outline").withBlend(BlendFunction.TRANSLUCENT).build());
            }
            catch (Throwable var7) {
                failed = true;
                System.err.println("Hook: could not register held-item outline pipeline: " + String.valueOf(var7));
            }
            finally {
                pipelineAttempted = true;
            }
            // ** MonitorExit[var0] (shouldn't be in output)
            return outlinePipeline;
        }
    }
}


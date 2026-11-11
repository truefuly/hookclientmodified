/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_10799
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  org.lwjgl.glfw.GLFW
 */
package hook.feature;

import hook.feature.ColorListEditorScreen;
import hook.module.setting.StorageBlocksSetting;
import hook.utils.RenderUtils;
import hook.utils.TextRenderer;
import java.awt.Color;
import java.util.Locale;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.lwjgl.glfw.GLFW;

public final class StorageColorPickerScreen
extends class_437 {
    private static final int WIDTH = 275;
    private static final int HEIGHT = 425;
    private static final int TEXTURE_SIZE = 128;
    private static final class_2960 SQUARE_TEXTURE_ID = class_2960.method_60655((String)"hook", (String)"runtime/storage_color_square");
    private static final class_2960 HUE_TEXTURE_ID = class_2960.method_60655((String)"hook", (String)"runtime/storage_color_hue");
    private static class_1043 squareTexture;
    private static class_1043 hueTexture;
    private static float uploadedHue;
    private final class_437 parent;
    private final StorageBlocksSetting.Entry entry;
    private final Color original;
    private float hue;
    private float saturation;
    private float brightness;
    private boolean draggingSquare;
    private boolean draggingHue;
    private boolean editingHex;
    private boolean selectAllHex;
    private String hexValue;

    public StorageColorPickerScreen(ColorListEditorScreen var1, StorageBlocksSetting.Entry var2) {
        this((class_437)var1, var2);
    }

    public StorageColorPickerScreen(class_437 var1, StorageBlocksSetting.Entry var2) {
        super((class_2561)class_2561.method_43473());
        this.parent = var1;
        this.entry = var2;
        this.original = var2.color();
        float[] var3 = Color.RGBtoHSB(this.original.getRed(), this.original.getGreen(), this.original.getBlue(), null);
        this.hue = var3[0];
        this.saturation = var3[1];
        this.brightness = var3[2];
        this.hexValue = StorageColorPickerScreen.toHex(this.original);
    }

    public void method_25394(class_332 var1, int var2, int var3, float var4) {
        RenderUtils.setContext(var1);
        RenderUtils.unscaledProjection();
        int var5 = class_310.method_1551().method_22683().method_4480();
        int var6 = class_310.method_1551().method_22683().method_4507();
        var1.method_25294(0, 0, var5, var6, new Color(0, 0, 0, 70).getRGB());
        int var7 = (var5 - 275) / 2;
        int var8 = (var6 - 425) / 2;
        RenderUtils.renderRoundedQuad(var1.method_51448(), new Color(31, 31, 38, 250), var7, var8, var7 + 275, var8 + 425, 9.0, 9.0, 9.0, 9.0, 20.0);
        TextRenderer.drawCenteredString("COLOR: " + this.entry.displayName().toUpperCase(Locale.ROOT), var1, var7 + 137, var8 + 10, Color.WHITE.getRGB());
        int var9 = var7 + 20;
        int var10 = var8 + 44;
        int var11 = 200;
        this.ensureTextures();
        var1.method_25293(class_10799.field_56883, SQUARE_TEXTURE_ID, var9, var10, 0.0f, 0.0f, var11, var11, 128, 128, 128, 128, -1);
        int var12 = var9 + Math.round(this.saturation * (float)var11);
        int var13 = var10 + Math.round((1.0f - this.brightness) * (float)var11);
        RenderUtils.renderCircle(var1.method_51448(), Color.BLACK, var12, var13, 6.0, 18);
        RenderUtils.renderCircle(var1.method_51448(), Color.WHITE, var12, var13, 4.0, 18);
        int var14 = var9 + var11 + 14;
        var1.method_25293(class_10799.field_56883, HUE_TEXTURE_ID, var14, var10, 0.0f, 0.0f, 20, var11, 1, 128, 1, 128, -1);
        int var15 = var10 + Math.round(this.hue * (float)var11);
        var1.method_25294(var14 - 2, var15 - 2, var14 + 22, var15 + 2, Color.WHITE.getRGB());
        var1.method_25294(var14 - 1, var15 - 1, var14 + 21, var15 + 1, Color.BLACK.getRGB());
        Color var16 = this.chosenColor();
        int var17 = var8 + 258;
        RenderUtils.renderRoundedQuad(var1.method_51448(), this.original, var9, var17, var9 + 110, var17 + 30, 4.0, 4.0, 4.0, 4.0, 20.0);
        RenderUtils.renderRoundedQuad(var1.method_51448(), var16, var9 + 125, var17, var9 + 235, var17 + 30, 4.0, 4.0, 4.0, 4.0, 20.0);
        TextRenderer.drawCenteredString("ORIGINAL", var1, var9 + 55, var17 + 38, new Color(160, 160, 165).getRGB());
        TextRenderer.drawCenteredString("NEW", var1, var9 + 180, var17 + 38, new Color(160, 160, 165).getRGB());
        int var18 = var8 + 318;
        int var19 = var7 + 65;
        boolean var20 = StorageColorPickerScreen.isValidHex(this.hexValue);
        TextRenderer.drawString("HEX", var1, var7 + 20, var18 + 8, new Color(190, 190, 198).getRGB());
        RenderUtils.renderRoundedQuad(var1.method_51448(), new Color(18, 18, 23), var19, var18, var7 + 275 - 20, var18 + 30, 5.0, 5.0, 5.0, 5.0, 20.0);
        RenderUtils.renderRoundedOutline(var1, !var20 ? new Color(255, 95, 95) : (this.editingHex ? new Color(120, 190, 255) : new Color(65, 65, 72)), var19, var18, var7 + 275 - 20, var18 + 30, 5.0, 5.0, 5.0, 5.0, 1.0, 20.0);
        int var21 = var20 ? new Color(235, 235, 240).getRGB() : new Color(255, 105, 105).getRGB();
        int var22 = TextRenderer.getWidth(this.hexValue);
        if (this.editingHex && this.selectAllHex && !this.hexValue.isEmpty()) {
            var1.method_25294(var19 + 8, var18 + 5, var19 + 12 + var22, var18 + 24, new Color(65, 125, 190, 120).getRGB());
        }
        TextRenderer.drawString(this.hexValue, var1, var19 + 10, var18 + 8, var21);
        if (this.editingHex && System.currentTimeMillis() % 1000L < 500L) {
            int var23 = var19 + 10 + var22;
            var1.method_25294(var23, var18 + 6, var23 + 1, var18 + 23, Color.WHITE.getRGB());
        }
        int var24 = var8 + 425 - 45;
        RenderUtils.renderRoundedQuad(var1.method_51448(), new Color(38, 38, 44), var7 + 107, var24, var7 + 177, var24 + 30, 5.0, 5.0, 5.0, 5.0, 20.0);
        TextRenderer.drawCenteredString("CANCEL", var1, var7 + 142, var24 + 8, new Color(255, 105, 105).getRGB());
        RenderUtils.renderRoundedQuad(var1.method_51448(), new Color(38, 38, 44), var7 + 186, var24, var7 + 256, var24 + 30, 5.0, 5.0, 5.0, 5.0, 20.0);
        TextRenderer.drawCenteredString("OK", var1, var7 + 221, var24 + 8, new Color(120, 190, 255).getRGB());
        RenderUtils.scaledProjection();
    }

    public boolean mouseClicked(double var1, double var3, int var5) {
        if (var5 != 0) {
            return false;
        }
        int var6 = StorageColorPickerScreen.physical(var1);
        int var7 = StorageColorPickerScreen.physical(var3);
        int var8 = class_310.method_1551().method_22683().method_4480();
        int var9 = class_310.method_1551().method_22683().method_4507();
        int var10 = (var8 - 275) / 2;
        int var11 = (var9 - 425) / 2;
        int var12 = var10 + 20;
        int var13 = var11 + 44;
        int var15 = var10 + 65;
        int var14 = var11 + 318;
        if (StorageColorPickerScreen.inside(var6, var7, var15, var14, 190, 30)) {
            this.editingHex = true;
            this.selectAllHex = true;
            return true;
        }
        this.editingHex = false;
        this.selectAllHex = false;
        if (StorageColorPickerScreen.inside(var6, var7, var12, var13, 200, 200)) {
            this.draggingSquare = true;
            this.updateSquare(var6, var7, var12, var13);
            return true;
        }
        if (StorageColorPickerScreen.inside(var6, var7, var12 + 214, var13, 20, 200)) {
            this.draggingHue = true;
            this.updateHue(var7, var13);
            return true;
        }
        int var16 = var11 + 425 - 45;
        if (StorageColorPickerScreen.inside(var6, var7, var10 + 107, var16, 70, 30)) {
            class_310.method_1551().method_1507(this.parent);
            return true;
        }
        if (StorageColorPickerScreen.inside(var6, var7, var10 + 186, var16, 70, 30)) {
            Color var17 = StorageColorPickerScreen.parseHex(this.hexValue);
            if (var17 == null) {
                this.editingHex = true;
                return true;
            }
            this.entry.setColor(var17);
            class_310.method_1551().method_1507(this.parent);
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double var1, double var3, int var5, double var6, double var8) {
        int var10 = StorageColorPickerScreen.physical(var1);
        int var11 = StorageColorPickerScreen.physical(var3);
        int var12 = (class_310.method_1551().method_22683().method_4480() - 275) / 2;
        int var13 = (class_310.method_1551().method_22683().method_4507() - 425) / 2;
        if (this.draggingSquare) {
            this.updateSquare(var10, var11, var12 + 20, var13 + 44);
            return true;
        }
        if (this.draggingHue) {
            this.updateHue(var11, var13 + 44);
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double var1, double var3, int var5) {
        this.draggingSquare = false;
        this.draggingHue = false;
        return true;
    }

    public boolean keyPressed(int var1, int var2, int var3) {
        if (var1 == 256) {
            class_310.method_1551().method_1507(this.parent);
            return true;
        }
        if (!this.editingHex) {
            return false;
        }
        if (var1 == 65 && StorageColorPickerScreen.hasControlDown()) {
            this.selectAllHex = true;
            return true;
        }
        if (var1 == 67 && StorageColorPickerScreen.hasControlDown()) {
            GLFW.glfwSetClipboardString((long)class_310.method_1551().method_22683().method_4490(), (CharSequence)this.hexValue);
            return true;
        }
        if (var1 == 88 && StorageColorPickerScreen.hasControlDown()) {
            GLFW.glfwSetClipboardString((long)class_310.method_1551().method_22683().method_4490(), (CharSequence)this.hexValue);
            this.hexValue = "";
            this.selectAllHex = false;
            return true;
        }
        if (var1 == 259 && this.selectAllHex) {
            this.hexValue = "";
            this.selectAllHex = false;
            return true;
        }
        if (var1 == 259 && !this.hexValue.isEmpty()) {
            this.hexValue = this.hexValue.substring(0, this.hexValue.length() - 1);
            this.applyHexIfValid();
            return true;
        }
        if (var1 == 86 && StorageColorPickerScreen.hasControlDown()) {
            String var4 = class_310.method_1551().field_1774.method_1460().trim();
            if (var4.matches("(?i)#?[0-9a-f]{6}")) {
                this.hexValue = ((String)(var4.startsWith("#") ? var4 : "#" + var4)).toUpperCase(Locale.ROOT);
                this.selectAllHex = false;
                this.applyHexIfValid();
            }
            return true;
        }
        if (var1 == 257) {
            this.applyHexIfValid();
            this.editingHex = false;
            this.selectAllHex = false;
            return true;
        }
        return false;
    }

    public boolean charTyped(char var1, int var2) {
        if (!this.editingHex) {
            return false;
        }
        if (this.selectAllHex) {
            this.hexValue = "";
            this.selectAllHex = false;
        }
        if (var1 == '#' && this.hexValue.isEmpty()) {
            this.hexValue = "#";
        } else if (Character.digit(var1, 16) >= 0 && this.hexValue.length() < 7) {
            if (this.hexValue.isEmpty()) {
                this.hexValue = "#";
            }
            this.hexValue = this.hexValue + Character.toUpperCase(var1);
        }
        this.applyHexIfValid();
        return true;
    }

    public boolean method_25402(class_11909 var1, boolean var2) {
        return this.mouseClicked(var1.comp_4798(), var1.comp_4799(), var1.method_74245());
    }

    public boolean method_25403(class_11909 var1, double var2, double var4) {
        return this.mouseDragged(var1.comp_4798(), var1.comp_4799(), var1.method_74245(), var2, var4);
    }

    public boolean method_25406(class_11909 var1) {
        return this.mouseReleased(var1.comp_4798(), var1.comp_4799(), var1.method_74245());
    }

    public boolean method_25404(class_11908 var1) {
        return this.keyPressed(var1.comp_4795(), var1.comp_4796(), var1.comp_4797());
    }

    public boolean method_25400(class_11905 var1) {
        return var1.method_74227() && this.charTyped((char)var1.comp_4793(), var1.comp_4794());
    }

    public void method_25420(class_332 var1, int var2, int var3, float var4) {
    }

    public boolean method_25422() {
        return false;
    }

    public boolean method_25421() {
        return false;
    }

    private void updateSquare(int var1, int var2, int var3, int var4) {
        this.saturation = StorageColorPickerScreen.clamp((float)(var1 - var3) / 200.0f);
        this.brightness = 1.0f - StorageColorPickerScreen.clamp((float)(var2 - var4) / 200.0f);
        this.selectAllHex = false;
        this.hexValue = StorageColorPickerScreen.toHex(this.chosenColor());
    }

    private void updateHue(int var1, int var2) {
        this.hue = StorageColorPickerScreen.clamp((float)(var1 - var2) / 200.0f);
        this.selectAllHex = false;
        this.hexValue = StorageColorPickerScreen.toHex(this.chosenColor());
    }

    private void ensureTextures() {
        class_310 var1 = class_310.method_1551();
        if (hueTexture == null) {
            class_1011 var2 = new class_1011(1, 128, false);
            for (int var3 = 0; var3 < 128; ++var3) {
                var2.method_61941(0, var3, 0xFF000000 | Color.HSBtoRGB((float)var3 / 127.0f, 1.0f, 1.0f) & 0xFFFFFF);
            }
            hueTexture = new class_1043(() -> "Hook storage hue", var2);
            var1.method_1531().method_4616(HUE_TEXTURE_ID, (class_1044)hueTexture);
        }
        if (squareTexture == null) {
            class_1011 var4 = new class_1011(128, 128, false);
            this.fillSquare(var4);
            squareTexture = new class_1043(() -> "Hook storage color square", var4);
            var1.method_1531().method_4616(SQUARE_TEXTURE_ID, (class_1044)squareTexture);
            uploadedHue = this.hue;
        } else if (Math.abs(uploadedHue - this.hue) > 1.0E-4f) {
            this.fillSquare(squareTexture.method_4525());
            squareTexture.method_4524();
            uploadedHue = this.hue;
        }
    }

    private void fillSquare(class_1011 var1) {
        for (int var2 = 0; var2 < 128; ++var2) {
            float var3 = 1.0f - (float)var2 / 127.0f;
            for (int var4 = 0; var4 < 128; ++var4) {
                float var5 = (float)var4 / 127.0f;
                var1.method_61941(var4, var2, 0xFF000000 | Color.HSBtoRGB(this.hue, var5, var3) & 0xFFFFFF);
            }
        }
    }

    private Color chosenColor() {
        return new Color(Color.HSBtoRGB(this.hue, this.saturation, this.brightness));
    }

    private void applyHexIfValid() {
        Color var1 = StorageColorPickerScreen.parseHex(this.hexValue);
        if (var1 != null) {
            float[] var2 = Color.RGBtoHSB(var1.getRed(), var1.getGreen(), var1.getBlue(), null);
            this.hue = var2[0];
            this.saturation = var2[1];
            this.brightness = var2[2];
        }
    }

    private static String toHex(Color var0) {
        return String.format(Locale.ROOT, "#%02X%02X%02X", var0.getRed(), var0.getGreen(), var0.getBlue());
    }

    private static boolean isValidHex(String var0) {
        return var0 != null && var0.matches("(?i)#[0-9a-f]{6}");
    }

    private static Color parseHex(String var0) {
        if (!StorageColorPickerScreen.isValidHex(var0)) {
            return null;
        }
        try {
            return new Color(Integer.parseInt(var0.substring(1), 16));
        }
        catch (NumberFormatException var2) {
            return null;
        }
    }

    private static boolean hasControlDown() {
        long var0 = class_310.method_1551().method_22683().method_4490();
        return GLFW.glfwGetKey((long)var0, (int)341) == 1 || GLFW.glfwGetKey((long)var0, (int)345) == 1;
    }

    private static float clamp(float var0) {
        return Math.max(0.0f, Math.min(1.0f, var0));
    }

    private static int physical(double var0) {
        return (int)Math.round(var0 * (double)class_310.method_1551().method_22683().method_4495());
    }

    private static boolean inside(double var0, double var2, int var4, int var5, int var6, int var7) {
        return var0 >= (double)var4 && var0 <= (double)(var4 + var6) && var2 >= (double)var5 && var2 <= (double)(var5 + var7);
    }

    static {
        uploadedHue = -1.0f;
    }
}


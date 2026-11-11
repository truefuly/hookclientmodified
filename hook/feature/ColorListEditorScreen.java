/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_1799
 *  net.minecraft.class_1935
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package hook.feature;

import hook.Hook;
import hook.feature.StorageColorPickerScreen;
import hook.module.setting.StorageBlocksSetting;
import hook.utils.RenderUtils;
import hook.utils.TextRenderer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1799;
import net.minecraft.class_1935;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

public final class ColorListEditorScreen
extends class_437 {
    private static final int WIDTH = 580;
    private static final int HEIGHT = 460;
    private static final int COLUMNS = 11;
    private static final int CELL = 42;
    private final StorageBlocksSetting setting;
    private final Map<String, ColorEntry> original = new LinkedHashMap<String, ColorEntry>();
    private String search = "";
    private boolean selectedOnly;
    private int scrollRow;

    public ColorListEditorScreen(StorageBlocksSetting var1) {
        super((class_2561)class_2561.method_43473());
        this.setting = var1;
        for (StorageBlocksSetting.Entry var3 : var1.entries()) {
            this.original.put(var3.id(), new ColorEntry(var3.enabled(), var3.color()));
        }
    }

    public void method_25394(class_332 var1, int var2, int var3, float var4) {
        RenderUtils.setContext(var1);
        RenderUtils.unscaledProjection();
        int var5 = ColorListEditorScreen.physical(var2);
        int var6 = ColorListEditorScreen.physical(var3);
        int var7 = class_310.method_1551().method_22683().method_4480();
        int var8 = class_310.method_1551().method_22683().method_4507();
        var1.method_25294(0, 0, var7, var8, new Color(0, 0, 0, 70).getRGB());
        int var9 = (var7 - 580) / 2;
        int var10 = (var8 - 460) / 2;
        RenderUtils.renderRoundedQuad(var1.method_51448(), new Color(30, 30, 35, 246), var9, var10, var9 + 580, var10 + 460, 8.0, 8.0, 8.0, 8.0, 20.0);
        RenderUtils.renderRoundedQuad(var1.method_51448(), new Color(40, 40, 45), var9, var10, var9 + 580, var10 + 34, 8.0, 8.0, 0.0, 0.0, 20.0);
        boolean var11 = this.setting.isPickerOnly();
        TextRenderer.drawCenteredString(var11 ? this.setting.getName().toString().toUpperCase(Locale.ROOT) + " (" + this.setting.selectedCount() + " SELECTED)" : "BLOCK COLORS: " + this.setting.getName().toString().toUpperCase(Locale.ROOT) + " (" + this.setting.selectedCount() + " BLOCKS)", var1, var9 + 290, var10 + 9, Color.WHITE.getRGB());
        int var12 = var10 + 42;
        this.button(var1, var9 + 20, var12, 150, 30, var11 ? "ALL ITEMS" : "ALL BLOCKS", !this.selectedOnly, new Color(120, 190, 255));
        this.button(var1, var9 + 180, var12, 150, 30, "SELECTED (" + this.setting.selectedCount() + ")", this.selectedOnly, new Color(120, 190, 255));
        int var13 = var10 + 82;
        RenderUtils.renderRoundedQuad(var1.method_51448(), new Color(18, 18, 23), var9 + 20, var13, var9 + 580 - 20, var13 + 31, 5.0, 5.0, 5.0, 5.0, 20.0);
        String var14 = System.currentTimeMillis() % 1000L > 500L ? "|" : "";
        TextRenderer.drawString((CharSequence)(this.search.isEmpty() ? "SEARCH..." : this.search + var14), var1, var9 + 30, var13 + 8, new Color(145, 145, 155).getRGB());
        int var15 = var9 + 20;
        int var16 = var10 + 124;
        int var17 = 270;
        RenderUtils.renderRoundedQuad(var1.method_51448(), new Color(20, 20, 25), var15, var16, var9 + 580 - 20, var16 + var17, 5.0, 5.0, 5.0, 5.0, 20.0);
        List<StorageBlocksSetting.Entry> var18 = this.visibleEntries();
        int var19 = Math.min(this.scrollRow * 11, Math.max(0, var18.size() - 1));
        int var20 = Math.min(var18.size(), var19 + 66);
        for (int var21 = var19; var21 < var20; ++var21) {
            StorageBlocksSetting.Entry var22 = var18.get(var21);
            int var23 = var21 - var19;
            int var24 = var15 + 6 + var23 % 11 * 42;
            int var25 = var16 + 6 + var23 / 11 * 42;
            Color var26 = var22.enabled() ? new Color(var22.color().getRed(), var22.color().getGreen(), var22.color().getBlue(), 85) : new Color(35, 35, 40);
            RenderUtils.renderRoundedQuad(var1.method_51448(), var26, var24, var25, var24 + 36, var25 + 36, 4.0, 4.0, 4.0, 4.0, 20.0);
            if (var22.enabled()) {
                RenderUtils.renderRoundedOutline(var1, var22.color(), var24, var25, var24 + 36, var25 + 36, 4.0, 4.0, 4.0, 4.0, 1.5, 20.0);
            }
            RenderUtils.drawItem(var1, new class_1799((class_1935)var22.item()), var24 + 2, var25 + 2, 32.0f, 0);
            var1.method_25294(var24 + 27, var25 + 3, var24 + 33, var25 + 9, var22.color().getRGB());
            if (!ColorListEditorScreen.inside(var5, var6, var24, var25, 36, 36)) continue;
            RenderUtils.renderRoundedOutline(var1, new Color(255, 255, 255, 210), var24, var25, var24 + 36, var25 + 36, 4.0, 4.0, 4.0, 4.0, 1.0, 20.0);
            TextRenderer.drawCenteredString(var22.displayName().toUpperCase(Locale.ROOT), var1, var9 + 290, var10 + 460 - 82, new Color(220, 220, 225).getRGB());
        }
        int var27 = var10 + 460 - 42;
        this.button(var1, var9 + 580 - 100, var27, 80, 30, "SAVE", true, new Color(90, 220, 135));
        this.button(var1, var9 + 580 - 210, var27, 100, 30, "CANCEL", false, new Color(255, 105, 105));
        this.button(var1, var9 + 580 - 320, var27, 100, 30, "CLEAR ALL", false, Color.WHITE);
        RenderUtils.scaledProjection();
    }

    private void button(class_332 var1, int var2, int var3, int var4, int var5, String var6, boolean var7, Color var8) {
        RenderUtils.renderRoundedQuad(var1.method_51448(), var7 ? new Color(75, 125, 185) : new Color(38, 38, 44), var2, var3, var2 + var4, var3 + var5, 5.0, 5.0, 5.0, 5.0, 20.0);
        TextRenderer.drawCenteredString(var6, var1, var2 + var4 / 2, var3 + 8, var8.getRGB());
    }

    private List<StorageBlocksSetting.Entry> visibleEntries() {
        ArrayList<StorageBlocksSetting.Entry> var1 = new ArrayList<StorageBlocksSetting.Entry>();
        String var2 = this.search.toLowerCase(Locale.ROOT);
        for (StorageBlocksSetting.Entry var4 : this.setting.entries()) {
            if (this.selectedOnly && !var4.enabled() || !var2.isEmpty() && !var4.displayName().toLowerCase(Locale.ROOT).contains(var2)) continue;
            var1.add(var4);
        }
        return var1;
    }

    public boolean mouseClicked(double var1, double var3, int var5) {
        int var9;
        int var11;
        int var8;
        int var10;
        int var7;
        int var6 = ColorListEditorScreen.physical(var1);
        if (ColorListEditorScreen.inside(var6, var7 = ColorListEditorScreen.physical(var3), (var10 = ((var8 = class_310.method_1551().method_22683().method_4480()) - 580) / 2) + 20, (var11 = ((var9 = class_310.method_1551().method_22683().method_4507()) - 460) / 2) + 42, 150, 30)) {
            this.selectedOnly = false;
            return true;
        }
        if (ColorListEditorScreen.inside(var6, var7, var10 + 180, var11 + 42, 150, 30)) {
            this.selectedOnly = true;
            return true;
        }
        int var12 = var10 + 20;
        int var13 = var11 + 124;
        List<StorageBlocksSetting.Entry> var14 = this.visibleEntries();
        int var15 = Math.min(this.scrollRow * 11, Math.max(0, var14.size() - 1));
        int var16 = Math.min(var14.size(), var15 + 66);
        for (int var17 = var15; var17 < var16; ++var17) {
            int var18 = var17 - var15;
            int var19 = var12 + 6 + var18 % 11 * 42;
            int var20 = var13 + 6 + var18 / 11 * 42;
            if (!ColorListEditorScreen.inside(var6, var7, var19, var20, 36, 36)) continue;
            StorageBlocksSetting.Entry var21 = var14.get(var17);
            if (!(var5 == 0 || var5 == 1 && this.setting.isPickerOnly())) {
                if (var5 == 1) {
                    class_310.method_1551().method_1507((class_437)new StorageColorPickerScreen(this, var21));
                }
            } else {
                var21.toggle();
            }
            return true;
        }
        int var22 = var11 + 460 - 42;
        if (ColorListEditorScreen.inside(var6, var7, var10 + 580 - 100, var22, 80, 30)) {
            Hook.INSTANCE.getSettingsManager().saveNow();
            this.returnToGui();
            return true;
        }
        if (ColorListEditorScreen.inside(var6, var7, var10 + 580 - 210, var22, 100, 30)) {
            this.restore();
            this.returnToGui();
            return true;
        }
        if (ColorListEditorScreen.inside(var6, var7, var10 + 580 - 320, var22, 100, 30)) {
            this.setting.clear();
            return true;
        }
        return false;
    }

    public boolean method_25401(double var1, double var3, double var5, double var7) {
        List<StorageBlocksSetting.Entry> var9 = this.visibleEntries();
        int var10 = Math.max(0, (var9.size() + 11 - 1) / 11 - 6);
        this.scrollRow = Math.max(0, Math.min(var10, this.scrollRow - (int)Math.signum(var7)));
        return true;
    }

    public boolean keyPressed(int var1, int var2, int var3) {
        if (var1 == 256) {
            this.restore();
            this.returnToGui();
            return true;
        }
        if (var1 == 259 && !this.search.isEmpty()) {
            this.search = this.search.substring(0, this.search.length() - 1);
            this.scrollRow = 0;
            return true;
        }
        return false;
    }

    public boolean charTyped(char var1, int var2) {
        if (!Character.isISOControl(var1) && this.search.length() < 28) {
            this.search = this.search + var1;
            this.scrollRow = 0;
        }
        return true;
    }

    public boolean method_25402(class_11909 var1, boolean var2) {
        return this.mouseClicked(var1.comp_4798(), var1.comp_4799(), var1.method_74245());
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

    private void restore() {
        for (StorageBlocksSetting.Entry var2 : this.setting.entries()) {
            ColorEntry var3 = this.original.get(var2.id());
            if (var3 == null) continue;
            var2.setEnabled(var3.enabled);
            var2.setColor(var3.color);
        }
    }

    private void returnToGui() {
        class_310.method_1551().method_1507((class_437)Hook.INSTANCE.GUI);
    }

    private static int physical(double var0) {
        return (int)Math.round(var0 * (double)class_310.method_1551().method_22683().method_4495());
    }

    private static boolean inside(double var0, double var2, int var4, int var5, int var6, int var7) {
        return var0 >= (double)var4 && var0 <= (double)(var4 + var6) && var2 >= (double)var5 && var2 <= (double)(var5 + var7);
    }

    private record ColorEntry(boolean enabled, Color color) {
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_1041
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_1792
 *  net.minecraft.class_1802
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_3419
 *  net.minecraft.class_408
 *  net.minecraft.class_437
 *  net.minecraft.class_640
 *  net.minecraft.class_7923
 */
package hook.feature;

import hook.Hook;
import hook.feature.ColorListEditorScreen;
import hook.feature.HookUiController;
import hook.feature.MinecraftUiRenderer;
import hook.feature.NameProtectModule;
import hook.feature.StorageColorPickerScreen;
import hook.feature.UiAudio;
import hook.halloween.Halloween;
import hook.halloween.HalloweenMusic;
import hook.module.Module;
import hook.module.setting.ItemSetting;
import hook.module.setting.StorageBlocksSetting;
import hook.utils.KeyUtils;
import hook.utils.RenderUtils;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1041;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3419;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_640;
import net.minecraft.class_7923;

public final class HookClickGuiScreen
extends class_437
implements HookUiController.UiDataSource {
    private final HookUiController ui = new HookUiController(this);
    private boolean freshOpen = true;
    private boolean inEditor;
    private boolean finishPending;
    private Runnable afterClose;
    private List<class_1792> items;

    public HookClickGuiScreen() {
        super((class_2561)class_2561.method_43473());
    }

    public void prepareOpen() {
        if (Hook.mc != null && Hook.mc.field_1755 == this) {
            this.ui.onOpen(false);
            this.freshOpen = false;
        } else {
            this.freshOpen = true;
        }
    }

    protected void method_25426() {
        this.ui.onOpen(this.freshOpen);
        this.freshOpen = false;
        this.inEditor = false;
        this.afterClose = null;
        this.syncAmbience();
    }

    private boolean ambienceWanted() {
        class_437 var1;
        class_437 class_4372 = var1 = Hook.mc == null ? null : Hook.mc.field_1755;
        return var1 == this ? !this.ui.isClosing() : this.inEditor && (var1 instanceof ColorListEditorScreen || var1 instanceof StorageColorPickerScreen);
    }

    public boolean showing() {
        return this.ambienceWanted();
    }

    @Override
    public float beat() {
        return HalloweenMusic.beat();
    }

    @Override
    public float shakeStrength() {
        return hook.module.modules.client.Hook.beatShake.getValue() ? (float)(hook.module.modules.client.Hook.shakeStrength.getValue() / 100.0) : 0.0f;
    }

    public void syncAmbience() {
        try {
            if (Hook.mc != null && Hook.mc.field_1690 != null) {
                UiAudio.setMaster(Hook.mc.field_1690.method_71978(class_3419.field_15250));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static int blurRadius(int var0) {
        try {
            HookClickGuiScreen var1 = Hook.INSTANCE.GUI;
            if (var1 != null && Hook.mc != null && Hook.mc.field_1755 == var1) {
                return Math.round((float)var0 * var1.ui.visibility());
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return var0;
    }

    public boolean isTyping() {
        return this.ui.isTyping();
    }

    public void requestClose() {
        this.ui.requestClose();
        UiAudio.setAmbience(false);
    }

    public void method_25419() {
        this.requestClose();
    }

    public boolean method_25421() {
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void method_25394(class_332 var1, int var2, int var3, float var4) {
        if (this.field_22787 != null && this.field_22787.method_22683() != null) {
            class_1041 var5 = this.field_22787.method_22683();
            int var6 = var5.method_4489();
            int var7 = var5.method_4506();
            if (var6 > 0 && var7 > 0) {
                RenderUtils.setContext(var1);
                if (Halloween.guiLocked() && !this.ui.isClosing()) {
                    this.requestClose();
                }
                this.syncAmbience();
                if (Hook.INSTANCE.screen != null && Hook.INSTANCE.screen != this) {
                    Hook.INSTANCE.screen.method_25394(var1, 0, 0, var4);
                }
                if (HookClickGuiScreen.blurRadius(this.field_22787.field_1690.method_57703()) > 0) {
                    try {
                        var1.method_71278();
                    }
                    catch (Throwable throwable) {
                        // empty catch block
                    }
                }
                float var8 = var5.method_4495();
                double var9 = this.field_22787.field_1729.method_1603() * (double)var6 / (double)Math.max(1, var5.method_4480());
                double var11 = this.field_22787.field_1729.method_1604() * (double)var7 / (double)Math.max(1, var5.method_4507());
                var1.method_51448().pushMatrix();
                var1.method_51448().scale(1.0f / var8, 1.0f / var8);
                try {
                    float var13 = (float)(hook.module.modules.client.Hook.guiSize.getValue() / 100.0);
                    this.ui.render(new MinecraftUiRenderer(var1, var8), var6, var7, var9, var11, var13);
                }
                finally {
                    var1.method_51448().popMatrix();
                }
                if (this.finishPending) {
                    this.finishPending = false;
                    this.finishClose();
                }
            }
        }
    }

    private void finishClose() {
        Module var1 = Hook.INSTANCE.getModuleManager().getModuleByClass(hook.module.modules.client.Hook.class);
        if (var1 != null) {
            var1.setEnabled(false);
        }
        try {
            Hook.INSTANCE.getSettingsManager().saveNow();
        }
        catch (Throwable var3) {
            var3.printStackTrace(System.err);
        }
        Runnable var2 = this.afterClose;
        this.afterClose = null;
        if (this.field_22787 != null && this.field_22787.field_1755 == this) {
            if (var2 != null) {
                var2.run();
            } else {
                this.field_22787.method_1507(Hook.INSTANCE.screen);
            }
        }
    }

    private double sx(double var1) {
        return var1 * (this.field_22787 == null ? 1.0 : (double)this.field_22787.method_22683().method_4495());
    }

    public boolean method_25402(class_11909 var1, boolean var2) {
        return this.ui.mouseClicked(this.sx(var1.comp_4798()), this.sx(var1.comp_4799()), var1.method_74245());
    }

    public boolean method_25406(class_11909 var1) {
        this.ui.mouseReleased(this.sx(var1.comp_4798()), this.sx(var1.comp_4799()), var1.method_74245());
        return true;
    }

    public boolean method_25403(class_11909 var1, double var2, double var4) {
        this.ui.mouseDragged(this.sx(var1.comp_4798()), this.sx(var1.comp_4799()));
        return true;
    }

    public boolean method_25401(double var1, double var3, double var5, double var7) {
        this.ui.mouseScrolled(this.sx(var1), this.sx(var3), var7);
        return true;
    }

    public boolean method_25404(class_11908 var1) {
        return this.ui.keyPressed(var1.comp_4795(), var1.comp_4797());
    }

    public boolean method_25400(class_11905 var1) {
        return var1.method_74227() && this.ui.charTyped((char)var1.comp_4793());
    }

    @Override
    public String playerName() {
        String var1;
        try {
            var1 = this.field_22787.method_1548().method_1676();
        }
        catch (Throwable var3) {
            return NameProtectModule.displayName("Player");
        }
        return NameProtectModule.displayName(var1);
    }

    @Override
    public String serverLine() {
        try {
            class_640 var2;
            String var1;
            if (this.field_22787.field_1687 == null) {
                return "Main menu";
            }
            if (this.field_22787.method_1542()) {
                return "Singleplayer";
            }
            String string = var1 = this.field_22787.method_1558() != null ? this.field_22787.method_1558().field_3761 : "Multiplayer";
            if (this.field_22787.field_1724 != null && this.field_22787.method_1562() != null && (var2 = this.field_22787.method_1562().method_2871(this.field_22787.field_1724.method_5667())) != null) {
                return var1 + "  \u00b7  " + var2.method_2959() + " ms";
            }
            return var1;
        }
        catch (Throwable var3) {
            return "";
        }
    }

    @Override
    public boolean inWorld() {
        return this.field_22787 != null && this.field_22787.field_1724 != null && this.field_22787.field_1687 != null;
    }

    @Override
    public String keyName(int var1) {
        try {
            CharSequence var2 = KeyUtils.getKey(var1);
            return var2 == null ? null : var2.toString();
        }
        catch (Throwable var3) {
            return null;
        }
    }

    @Override
    public void openStorage(StorageBlocksSetting var1) {
        this.inEditor = true;
        if (var1.isColorOnly() && !var1.entries().isEmpty()) {
            this.field_22787.method_1507((class_437)new StorageColorPickerScreen(this, var1.entries().get(0)));
        } else {
            this.field_22787.method_1507((class_437)new ColorListEditorScreen(var1));
        }
    }

    @Override
    public void openHudEditor() {
        if (this.inWorld()) {
            this.afterClose = () -> this.field_22787.method_1507((class_437)new class_408("", false));
            this.ui.requestClose();
        }
    }

    @Override
    public void closeNow() {
        this.finishPending = true;
    }

    @Override
    public List<?> items() {
        if (this.items == null) {
            this.items = new ArrayList<class_1792>();
            for (class_1792 var2 : class_7923.field_41178) {
                if (var2 == class_1802.field_8162) continue;
                this.items.add(var2);
            }
        }
        return this.items;
    }

    @Override
    public String itemName(Object var1) {
        try {
            String string;
            if (var1 instanceof class_1792) {
                class_1792 var2 = (class_1792)var1;
                string = var2.method_63680().getString();
            } else {
                string = "None";
            }
            return string;
        }
        catch (Throwable var3) {
            return "Item";
        }
    }

    @Override
    public Object itemOf(ItemSetting var1) {
        return var1.getItem();
    }

    @Override
    public void setItem(ItemSetting var1, Object var2) {
        if (var2 instanceof class_1792) {
            class_1792 var3 = (class_1792)var2;
            var1.setItem(var3);
        }
    }

    @Override
    public Path stateFile() {
        try {
            return FabricLoader.getInstance().getConfigDir().resolve("hook-gui-position.txt");
        }
        catch (Throwable var2) {
            return null;
        }
    }
}


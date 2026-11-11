/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_1041
 *  net.minecraft.class_1937
 *  net.minecraft.class_408
 *  net.minecraft.class_640
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.MouseButtonEvent;
import hook.event.events.Render2DEvent;
import hook.feature.HookClickGuiScreen;
import hook.feature.HudLayoutEditor;
import hook.feature.MinecraftUiRenderer;
import hook.feature.ModuleCatalog;
import hook.feature.NameProtectModule;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.StringSetting;
import hook.utils.EncryptedString;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1041;
import net.minecraft.class_1937;
import net.minecraft.class_408;
import net.minecraft.class_640;

public final class HudModule
extends Module {
    public static final BooleanSetting HIDE_POTION_HUD = new BooleanSetting(EncryptedString.of("Hide Potion HUD"), true).setDescription(EncryptedString.of("Hides the full vanilla status-effect overlay"));
    private final BooleanSetting showWatermark = new BooleanSetting(EncryptedString.of("Watermark"), true).setDescription(EncryptedString.of("HookVisuals watermark in the corner"));
    private final BooleanSetting showTime = new BooleanSetting(EncryptedString.of("Time"), true).setDescription(EncryptedString.of("Clock next to the watermark"));
    private final BooleanSetting showInfo = new BooleanSetting(EncryptedString.of("Info"), true).setDescription(EncryptedString.of("FPS, ping and server"));
    private final BooleanSetting showPlayer = new BooleanSetting(EncryptedString.of("Player"), false).setDescription(EncryptedString.of("Your head, name and health"));
    private final BooleanSetting showCoordinates = new BooleanSetting(EncryptedString.of("Show Coordinates"), true).setDescription(EncryptedString.of("Position with a mini compass"));
    private final BooleanSetting showDirection = new BooleanSetting(EncryptedString.of("Direction"), true).setDescription(EncryptedString.of("Facing direction next to the coordinates"));
    private final BooleanSetting showNether = new BooleanSetting(EncryptedString.of("Nether Coords"), false).setDescription(EncryptedString.of("Matching overworld / nether position"));
    private final BooleanSetting showModules = new BooleanSetting(EncryptedString.of("Modules"), true).setDescription(EncryptedString.of("Enabled modules, longest name first"));
    private final BooleanSetting rainbow = new BooleanSetting(EncryptedString.of("Rainbow HUD"), false).setDescription(EncryptedString.of("Soft flowing rainbow on the module list and accents"));
    private final NumberSetting rainbowSpeed = new NumberSetting(EncryptedString.of("Rainbow Speed"), 0.1, 5.0, 1.0, 0.1).getValue(EncryptedString.of("How fast the rainbow flows"));
    private final NumberSetting hudScale = new NumberSetting(EncryptedString.of("HUD Scale"), 60.0, 160.0, 100.0, 5.0).getValue(EncryptedString.of("Size of the HUD in percent"));
    private final NumberSetting opacity = new NumberSetting(EncryptedString.of("Opacity"), 0.0, 1.0, 0.62, 0.05).getValue(EncryptedString.of("Strength of the glass behind HUD elements"));
    private final StringSetting customIp = new StringSetting(EncryptedString.of("Custom IP"), "").setDescription(EncryptedString.of("Server name shown in the HUD; blank uses the connected server"));
    private final BooleanSetting spoofPing = new BooleanSetting(EncryptedString.of("Ping Spoof"), false).setDescription(EncryptedString.of("Display a custom ping value in this HUD only"));
    private final NumberSetting spoofedPing = new NumberSetting(EncryptedString.of("Spoofed Ping"), 0.0, 9999.0, 50.0, 1.0).getValue(EncryptedString.of("Ping value shown in the HUD while Ping Spoof is enabled"));
    private final HudLayoutEditor hud = new HudLayoutEditor();
    private final HudLayoutEditor.adhvjlahyjurfh options = new HudLayoutEditor.adhvjlahyjurfh();
    private final HudDataSourceAdapter data = new HudDataSourceAdapter();
    private float shown;
    private long last;

    public HudModule() {
        super(EncryptedString.of("HUD"), EncryptedString.of("Glass HUD; open chat to move elements, resize/hide boss bar and scoreboard, or reset"), -1, Category.CLIENT);
        this.addSettings(this.showWatermark, this.showTime, this.showInfo, this.showPlayer, this.showCoordinates, this.showDirection, this.showNether, this.showModules, this.rainbow, this.rainbowSpeed, this.hudScale, this.opacity, HIDE_POTION_HUD, this.customIp, this.spoofPing, this.spoofedPing);
        try {
            this.hud.setFile(FabricLoader.getInstance().getConfigDir().resolve("hook-hud-layout.txt"));
            this.hud.load(); // boss bar / scoreboard layout applies even while the HUD module is off
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventListener
    public void onRender2D(Render2DEvent var1) {
        if (!this.mc.field_1690.field_1842 && this.mc.method_22683() != null) {
            long var2 = System.nanoTime();
            float var4 = this.last == 0L ? 0.016f : Math.min(0.1f, (float)(var2 - this.last) / 1.0E9f);
            this.last = var2;
            boolean var5 = this.mc.field_1755 instanceof HookClickGuiScreen;
            float var6 = var5 ? 0.0f : 1.0f;
            this.shown += (var6 - this.shown) * (1.0f - (float)Math.exp(-(var5 ? 14.0f : 7.0f) * var4));
            if (!(this.shown < 0.01f)) {
                this.options.watermark = this.showWatermark.getValue();
                this.options.time = this.showTime.getValue();
                this.options.info = this.showInfo.getValue();
                this.options.player = this.showPlayer.getValue();
                this.options.coords = this.showCoordinates.getValue();
                this.options.direction = this.showDirection.getValue();
                this.options.netherCoords = this.showNether.getValue();
                this.options.modules = this.showModules.getValue();
                this.options.scale = (float)(this.hudScale.getValue() / 100.0);
                this.options.opacity = (float)this.opacity.getValue();
                this.options.rainbow = this.rainbow.getValue();
                this.options.rainbowSpeed = (float)this.rainbowSpeed.getValue();
                class_1041 var7 = this.mc.method_22683();
                int var8 = var7.method_4489();
                int var9 = var7.method_4506();
                float var10 = var7.method_4495();
                double var11 = this.mc.field_1729.method_1603() * (double)var8 / (double)Math.max(1, var7.method_4480());
                double var13 = this.mc.field_1729.method_1604() * (double)var9 / (double)Math.max(1, var7.method_4507());
                boolean var15 = this.mc.field_1755 instanceof class_408;
                var1.context.method_51448().pushMatrix();
                var1.context.method_51448().scale(1.0f / var10, 1.0f / var10);
                try {
                    this.hud.render(new MinecraftUiRenderer(var1.context, var10), var8, var9, this.data, this.options, this.shown, var15, var11, var13);
                }
                finally {
                    var1.context.method_51448().popMatrix();
                }
            }
        }
    }

    @EventListener
    public void onMouseScrolled(hook.event.events.MouseScrolledEvent event) {
        if (this.mc.field_1755 instanceof class_408) {
            class_1041 w = this.mc.method_22683();
            double x = this.mc.field_1729.method_1603() * (double)w.method_4489() / (double)Math.max(1, w.method_4480());
            double y = this.mc.field_1729.method_1604() * (double)w.method_4506() / (double)Math.max(1, w.method_4507());
            if (this.hud.scroll(x, y, event.amount)) event.cancel();
        }
    }

    @EventListener
    public void onMouseButton(MouseButtonEvent var1) {
        if (var1.button == 1 && var1.actions == 1 && this.mc.field_1755 instanceof class_408) {
            class_1041 w = this.mc.method_22683();
            double x = this.mc.field_1729.method_1603() * (double)w.method_4489() / (double)Math.max(1, w.method_4480());
            double y = this.mc.field_1729.method_1604() * (double)w.method_4506() / (double)Math.max(1, w.method_4507());
            this.hud.rightClick(x, y);
            return;
        }
        if (var1.button == 0) {
            if (var1.actions == 1 && this.mc.field_1755 instanceof class_408) {
                class_1041 var2 = this.mc.method_22683();
                double var3 = this.mc.field_1729.method_1603() * (double)var2.method_4489() / (double)Math.max(1, var2.method_4480());
                double var5 = this.mc.field_1729.method_1604() * (double)var2.method_4506() / (double)Math.max(1, var2.method_4507());
                this.hud.mousePressed(var3, var5);
            } else if (var1.actions == 0) {
                this.hud.mouseReleased();
            }
        }
    }

    private final class HudDataSourceAdapter
    implements HudLayoutEditor.HudDataSource {
        private HudDataSourceAdapter() {
        }

        @Override
        public int fps() {
            return HudModule.this.mc.method_47599();
        }

        @Override
        public String ping() {
            if (HudModule.this.spoofPing.getValue()) {
                return String.valueOf(HudModule.this.spoofedPing.getIntValue());
            }
            if (((HudModule)HudModule.this).mc.field_1724 != null && HudModule.this.mc.method_1562() != null) {
                class_640 var1 = HudModule.this.mc.method_1562().method_2871(((HudModule)HudModule.this).mc.field_1724.method_5667());
                return var1 == null ? "0" : String.valueOf(var1.method_2959());
            }
            return "0";
        }

        @Override
        public String server() {
            String var1 = HudModule.this.customIp.getValue();
            if (var1 != null && !var1.isBlank()) {
                return var1.trim();
            }
            if (HudModule.this.mc.method_1558() != null) {
                return ((HudModule)HudModule.this).mc.method_1558().field_3761;
            }
            return HudModule.this.mc.method_1542() ? "Singleplayer" : "";
        }

        @Override
        public boolean inWorld() {
            return ((HudModule)HudModule.this).mc.field_1724 != null && ((HudModule)HudModule.this).mc.field_1687 != null;
        }

        @Override
        public double x() {
            return ((HudModule)HudModule.this).mc.field_1724.method_23317();
        }

        @Override
        public double y() {
            return ((HudModule)HudModule.this).mc.field_1724.method_23318();
        }

        @Override
        public double z() {
            return ((HudModule)HudModule.this).mc.field_1724.method_23321();
        }

        @Override
        public float yaw() {
            return ((HudModule)HudModule.this).mc.field_1724.method_36454();
        }

        @Override
        public boolean nether() {
            return ((HudModule)HudModule.this).mc.field_1687 != null && ((HudModule)HudModule.this).mc.field_1687.method_27983() == class_1937.field_25180;
        }

        @Override
        public String playerName() {
            return NameProtectModule.displayName(((HudModule)HudModule.this).mc.field_1724 == null ? "" : ((HudModule)HudModule.this).mc.field_1724.method_5477().getString());
        }

        @Override
        public float health() {
            return ((HudModule)HudModule.this).mc.field_1724 == null ? 0.0f : ((HudModule)HudModule.this).mc.field_1724.method_6032();
        }

        @Override
        public List<String> modules() {
            return ModuleCatalog.enabled(HudModule.this);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_408
 *  net.minecraft.class_437
 */
package hook.module;

import hook.event.EventListener;
import hook.event.events.KeyEvent;
import hook.event.events.MouseButtonEvent;
import hook.event.events.TickEvent;
import hook.feature.AimAssistModule;
import hook.feature.AnchorMacroModule;
import hook.feature.AntiTrapModule;
import hook.feature.AntiVanishModule;
import hook.feature.AuctionSniperModule;
import hook.feature.AutoCrystalModule;
import hook.feature.AutoEatModule;
import hook.feature.AutoFireworkModule;
import hook.feature.AutoInvTotemModule;
import hook.feature.AutoMineModule;
import hook.feature.AutoRtpModule;
import hook.feature.AutoSellModule;
import hook.feature.SwordcoreModuleSupport;
import hook.feature.MovementSyncModule;
import hook.feature.SmartSwordModule;
import hook.feature.ShieldDisableModule;
import hook.feature.MovementResistModule;
import hook.feature.STappingModule;
import hook.feature.JumpFollowingModule;
import hook.feature.SprintHitFollowModule;
import hook.feature.TrueJumpResetModule;
import hook.feature.AutoSpawnerSellModule;
import hook.feature.AutoToolModule;
import hook.feature.AutoTotemModule;
import hook.feature.AutoTpaModule;
import hook.feature.AxeSwapModule;
import hook.feature.XbowCartModule;
import hook.feature.InstaCartModule;
import hook.feature.BedrockEspModule;
import hook.feature.BlockEspModule;
import hook.feature.TunnelEspModule;
import hook.feature.DeepslateRotationEspModule;
import hook.feature.BlockNotifierModule;
import hook.feature.BlockOverlayModule;
import hook.feature.Character3dModule;
import hook.feature.ChunkFinderBetterModule;
import hook.feature.ChunkFinderModule;
import hook.feature.CordSnapperModule;
import hook.feature.CustomSkysModule;
import hook.feature.CustomToolsModule;
import hook.feature.CustomTotemModule;
import hook.feature.DoubleAnchorModule;
import hook.feature.ElytraGlideModule;
import hook.feature.ElytraSwapModule;
import hook.feature.FakePayModule;
import hook.feature.FakeScoreboardModule;
import hook.feature.FastPlaceModule;
import hook.feature.FlyModule;
import hook.feature.FreecamModule;
import hook.feature.GlazedRtpBaseFinderModule;
import hook.feature.GlazedRtpEndBaseFinderModule;
import hook.feature.GlazedRtpNetherBaseFinderModule;
import hook.feature.GlazedAmethystEspModule;
import hook.feature.GlazedRotatedDeepslateEspModule;
import hook.feature.GlazedChunkFinderModule;
import hook.feature.FreelookModule;
import hook.feature.FullbrightModule;
import hook.feature.HalloweenModule;
import hook.feature.HandAuraModule;
import hook.feature.HitboxModule;
import hook.feature.HookClickGuiScreen;
import hook.feature.HoverTotemModule;
import hook.feature.HudModule;
import hook.feature.ItemOutlineModule;
import hook.feature.LuckyFinderModule;
import hook.feature.MaceSwapModule;
import hook.feature.NameProtectModule;
import hook.feature.NetheriteFinderModule;
import hook.feature.OgScoreboardModule;
import hook.feature.PearlCatchModule;
import hook.feature.PanicButtonModule;
import hook.feature.PlayerBypassModule;
import hook.feature.PlayerDetectionModule;
import hook.feature.PlayerEspModule;
import hook.feature.PrimeChunkFinderModule;
import hook.feature.RegionMapModule;
import hook.feature.RtpBaseFinderModule;
import hook.feature.ShulkerDropperModule;
import hook.feature.SkinProtectModule;
import hook.feature.SpawnerDropperModule;
import hook.feature.SpawnerNotifyModule;
import hook.feature.SpawnerOrderModule;
import hook.feature.SpawnerProtectModule;
import hook.feature.SpearSwapModule;
import hook.feature.StaffListModule;
import hook.feature.StaticHitboxesModule;
import hook.feature.StorageEspModule;
import hook.feature.SusChunkFinderModule;
import hook.feature.SusChunkV2Module;
import hook.feature.SwingSpeedModule;
import hook.feature.TargetHudModule;
import hook.feature.TriggerBotModule;
import hook.feature.VerseFallModuleBridge;
import hook.feature.VerseFallBackendSettingsModule;
import hook.feature.TunnelBaseFinderModule;
import hook.feature.YouTubeModule;
import hook.module.Category;
import hook.module.Module;
import hook.module.modules.client.Hook;
import hook.module.setting.BindSetting;
import hook.utils.EncryptedString;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.class_408;
import net.minecraft.class_437;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<Module>();
    private final Map<Class<?>, Module> modulesByClass = new HashMap();
    private final Map<Category, List<Module>> modulesByCategory = new EnumMap<Category, List<Module>>(Category.class);

    public ModuleManager() {
        this.a();
        this.d();
    }

    public void a() {
        this.a(new TriggerBotModule());
        this.a(new MovementSyncModule());
        this.a(new TrueJumpResetModule());
        this.a(new XbowCartModule());
        this.a(new InstaCartModule());
        this.a(new PearlCatchModule());
        this.a(new HitboxModule());
        this.a(new AutoTotemModule());
        this.a(new HoverTotemModule());
        this.a(new AnchorMacroModule());
        this.a(new AutoCrystalModule());
        this.a(new DoubleAnchorModule());
        this.a(new FastPlaceModule());
        this.a(new FreecamModule());
        this.a(new FreelookModule());
        this.a(new FlyModule());
        this.a(new AutoToolModule());
        this.a(new SwingSpeedModule());
        this.a(new AutoEatModule());
        this.a(new AutoMineModule());
        this.a(new NameProtectModule());
        this.a(new SkinProtectModule());
        this.macro(new AutoTpaModule());
        this.a(new PlayerEspModule());
        this.a(new StorageEspModule());
        this.a(new BlockEspModule());
        this.a(new TunnelEspModule());
        this.a(new DeepslateRotationEspModule());
        this.a(new BlockNotifierModule());
        this.a(new BedrockEspModule());
        this.a(new SpawnerNotifyModule());
        this.a(new TargetHudModule());
        this.a(new VerseFallModuleBridge("HUD Lyrics", "Musiktext im HUD.", "HUD Lyrics"));
        this.safe("3D Character", Character3dModule::new);
        this.safe("Custom Skys", CustomSkysModule::new);
        this.safe("Block Overlay", BlockOverlayModule::new);
        this.safe("Fullbright", FullbrightModule::new);
        this.a(new Hook());
        this.a(new PanicButtonModule());
        this.a(new VerseFallBackendSettingsModule());
        this.a(new HudModule());
        try {
            this.a(new ItemOutlineModule());
        }
        catch (Throwable var2) {
            System.err.println("Hook: Item Outline unavailable: " + var2.getClass().getSimpleName());
        }
    }

    public List<Module> b() {
        return this.modules.stream().filter(Module::isEnabled).toList();
    }

    public List<Module> c() {
        return this.modules;
    }

    public void d() {
        hook.Hook.INSTANCE.getEventBus().register(this);
        for (Module var2 : this.modules) {
            var2.addSetting(new BindSetting(EncryptedString.of("Keybind"), var2.getKeybind(), true).setDescription(EncryptedString.of("Key to enabled the module")));
        }
    }

    @EventListener
    public void syncMediaBackend(TickEvent event) {
        VerseFallBackendSettingsModule settings = (VerseFallBackendSettingsModule)this.getModuleByClass(VerseFallBackendSettingsModule.class);
        if (settings != null) {
            com.spotifyclient.media.MediaBackend.setMode(settings.backend.getMode());
        }
        for (Module module : this.modules) {
            if (module instanceof VerseFallModuleBridge bridge) bridge.syncSettings();
        }
        SwordcoreModuleSupport.sync(this);
    }

    public List<Module> a(Category var1) {
        return this.modulesByCategory.getOrDefault((Object)var1, List.of());
    }

    public Module getModuleByClass(Class<? extends Module> var1) {
        Module var2 = this.modulesByClass.get(var1);
        if (var2 != null) {
            return var2;
        }
        for (Module var4 : this.modules) {
            if (!var1.isInstance(var4)) continue;
            return var4;
        }
        return null;
    }

    private void safe(String var1, Supplier<? extends Module> var2) {
        try {
            this.a(var2.get());
        }
        catch (Throwable var4) {
            System.err.println("Hook: " + var1 + " unavailable: " + String.valueOf(var4));
            var4.printStackTrace(System.err);
        }
    }

    private void macro(Module var1) {
        var1.setCategory(Category.MACROS);
        this.a(var1);
    }

    public void a(Module var1) {
        hook.Hook.INSTANCE.getEventBus().register(var1);
        this.modules.add(var1);
        this.modulesByClass.put(var1.getClass(), var1);
        this.modulesByCategory.computeIfAbsent(var1.getCategory(), var0 -> new ArrayList()).add(var1);
    }

    @EventListener
    public void a(KeyEvent var1) {
        HookClickGuiScreen var2;
        class_437 class_4372;
        if (!(var1.mode != 1 || hook.Hook.mc != null && (class_4372 = hook.Hook.mc.field_1755) instanceof HookClickGuiScreen && (var2 = (HookClickGuiScreen)class_4372).isTyping())) {
            this.modules.forEach(var1x -> {
                boolean var2x;
                // The menu key only works in-game (no screen) or to close the menu itself - never in chat,
                // inventories, the pause menu or any other screen.
                if (var1x.getKeybind() == var1.key && ((var2x = var1x instanceof Hook) ? menuKeyAllowed() : hook.Hook.mc.field_1724 != null && hook.Hook.mc.field_1690 != null && !(hook.Hook.mc.field_1755 instanceof class_408))) {
                    var1x.toggle();
                }
            });
        }
    }

    @EventListener
    public void onMouseButton(MouseButtonEvent var1) {
        if (var1.actions == 1 && var1.button >= 0 && var1.button <= 7 && !(hook.Hook.mc.field_1755 instanceof HookClickGuiScreen) && !(hook.Hook.mc.field_1755 instanceof class_408)) {
            this.modules.forEach(var1x -> {
                boolean var2;
                if (var1x.getKeybind() == var1.button && ((var2 = var1x instanceof Hook) ? menuKeyAllowed() : hook.Hook.mc.field_1724 != null && hook.Hook.mc.field_1690 != null)) {
                    var1x.toggle();
                }
            });
        }
    }

    private static boolean menuKeyAllowed() {
        if (hook.Hook.mc == null) return false;
        class_437 screen = hook.Hook.mc.field_1755;
        return screen == null || screen instanceof HookClickGuiScreen || screen instanceof net.minecraft.class_442;
    }
}


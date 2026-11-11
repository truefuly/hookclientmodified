/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_12076
 *  net.minecraft.class_1937
 *  net.minecraft.class_1959
 *  net.minecraft.class_2338
 *  net.minecraft.class_2374
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_638
 *  net.minecraft.class_7833
 *  net.minecraft.class_9848
 *  org.joml.Quaternionfc
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 */
package hook.feature;

import hook.feature.AuroraMeshRenderer;
import hook.feature.SkyDomeRenderer;
import hook.feature.StarfieldRenderer;
import hook.mixin.CustomSkysClientWorldAccessor;
import hook.module.ext.ClientModule;
import hook.module.ext.ModuleCategory;
import hook.module.ext.ModuleRegistry;
import hook.module.ext.setting.BooleanSetting;
import hook.module.ext.setting.ColorSetting;
import hook.module.ext.setting.ModeSetting;
import hook.module.ext.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12076;
import net.minecraft.class_1937;
import net.minecraft.class_1959;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_638;
import net.minecraft.class_7833;
import net.minecraft.class_9848;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.joml.Vector4f;

@Environment(value=EnvType.CLIENT)
public final class CustomSkysModule
extends ClientModule {
    private static final String CUSTOM = "Custom";
    private static final String AURORA_ALWAYS = "Always";
    private static final String AURORA_COLD = "Cold Biomes";
    private static final String AURORA_OFF = "Off";
    private static final float MOON_SCALE = 1.4f;
    private static final SkyPalette[] THEMES;
    private final ModeSetting theme;
    private final ColorSetting nightColor;
    private final ColorSetting sunsetColor;
    private final ColorSetting auroraColor;
    private final ColorSetting auroraTopColor;
    private final BooleanSetting gradientSky;
    private final BooleanSetting keepInRain;
    private final BooleanSetting celestialGlow;
    private final BooleanSetting stars;
    private final NumberSetting starBrightness;
    private final BooleanSetting milkyWay;
    private final NumberSetting milkyWayBrightness;
    private final BooleanSetting shootingStars;
    private final ModeSetting aurora;
    private final NumberSetting auroraBrightness;
    private final NumberSetting auroraSpeed;
    private final BooleanSetting cloudColors;
    private final BooleanSetting endSky;
    private final BooleanSetting netherColors;
    private final SkyDomeRenderer sky;
    private final StarfieldRenderer star;
    private final AuroraMeshRenderer auroraMesh;
    private final class_4587 world;
    private float night;
    private float rainBrightness;
    private float auroraHere;
    private long lastFrame;
    private boolean moonScaled;

    public CustomSkysModule() {
        super("custom-skys", "Custom Skys", "A custom sky with stars and an aurora.", ModuleCategory.VISUALS);
        String var10002 = "theme";
        String var10005 = "Theme";
        String var10006 = "Borealis";
        String[] var10007 = new String[]{"Borealis", "Nebula", "HookVisuals", "Frost", "Crimson", CUSTOM};
        this.theme = this.addSetting(var10002, new ModeSetting(var10005, var10006, var10007));
        this.nightColor = this.addSetting("night-color", new ColorSetting("Night Sky Color", -16047568));
        this.sunsetColor = this.addSetting("sunset-color", new ColorSetting("Sunset Color", -3703914));
        this.auroraColor = this.addSetting("aurora-color", new ColorSetting("Aurora Color", -13697126));
        this.auroraTopColor = this.addSetting("aurora-top-color", new ColorSetting("Aurora Top Color", -7713281));
        this.gradientSky = this.addSetting("gradient-sky", new BooleanSetting("Gradient Sky", true));
        this.keepInRain = this.addSetting("keep-in-rain", new BooleanSetting("Keep In Rain", true));
        this.celestialGlow = this.addSetting("sun-moon-glow", new BooleanSetting("Sun & Moon Glow", true));
        this.stars = this.addSetting("stars", new BooleanSetting("Stars", true));
        this.starBrightness = this.addSetting("star-brightness", new NumberSetting("Star Brightness", 1.3f, 0.5f, 2.0f).step(0.1f));
        this.milkyWay = this.addSetting("milky-way", new BooleanSetting("Milky Way", true));
        this.milkyWayBrightness = this.addSetting("milky-way-brightness", new NumberSetting("Milky Way Brightness", 1.0f, 0.2f, 2.0f).step(0.1f));
        this.shootingStars = this.addSetting("shooting-stars", new BooleanSetting("Shooting Stars", true));
        var10002 = "aurora";
        var10005 = "Aurora";
        var10006 = AURORA_ALWAYS;
        var10007 = new String[]{AURORA_ALWAYS, AURORA_COLD, AURORA_OFF};
        this.aurora = this.addSetting(var10002, new ModeSetting(var10005, var10006, var10007));
        this.auroraBrightness = this.addSetting("aurora-brightness", new NumberSetting("Aurora Brightness", 1.0f, 0.2f, 2.0f).step(0.1f));
        this.auroraSpeed = this.addSetting("aurora-speed", new NumberSetting("Aurora Speed", 1.0f, 0.2f, 3.0f).step(0.1f));
        this.cloudColors = this.addSetting("cloud-colors", new BooleanSetting("Cloud Colors", true));
        this.endSky = this.addSetting("end-sky", new BooleanSetting("End Sky", true));
        this.netherColors = this.addSetting("nether-colors", new BooleanSetting("Nether Colors", true));
        this.sky = new SkyDomeRenderer();
        this.star = new StarfieldRenderer();
        this.auroraMesh = new AuroraMeshRenderer();
        this.world = new class_4587();
        this.rainBrightness = 1.0f;
        this.nightColor.visibleWhen(() -> this.theme.is(CUSTOM));
        this.sunsetColor.visibleWhen(() -> this.theme.is(CUSTOM));
        this.auroraColor.visibleWhen(() -> this.theme.is(CUSTOM));
        this.auroraTopColor.visibleWhen(() -> this.theme.is(CUSTOM));
        this.starBrightness.visibleWhen(this.stars::get);
        this.milkyWayBrightness.visibleWhen(this.milkyWay::get);
        this.auroraBrightness.visibleWhen(() -> !this.aurora.is(AURORA_OFF));
        this.auroraSpeed.visibleWhen(() -> !this.aurora.is(AURORA_OFF));
    }

    public static CustomSkysModule get() {
        CustomSkysModule var0 = ModuleRegistry.get(CustomSkysModule.class);
        return var0 != null && var0.isEnabled() ? var0 : null;
    }

    public String selectedThemeName() {
        return this.theme.get();
    }

    @Override
    public void onDisable() {
        this.sky.close();
        this.star.close();
        this.auroraMesh.close();
    }

    public void extract(class_638 var1, float var2, class_12076 var3) {
        this.night = class_3532.method_15363((float)(var3.field_63094 * 2.0f), (float)0.0f, (float)1.0f);
        float var4 = 1.0f - var3.field_63093;
        float var5 = var1.method_8478(var2);
        if (this.keepInRain.get()) {
            this.night = Math.max(this.night, CustomSkysModule.clearNight(var3.field_63091) * var4);
            var3.field_63094 = Math.max(var3.field_63094, this.night * 0.5f);
            var3.field_63093 = 1.0f;
            var4 = 0.0f;
            var5 = 0.0f;
        }
        this.rainBrightness = 1.0f - var4;
        SkyPalette var6 = this.currentTheme();
        int var7 = ((CustomSkysClientWorldAccessor)var1).hook$atmosphereSkyFlash();
        this.sky.update(var3.field_63091, var3.field_64465, var4, var5, var7 > 0 ? Math.min(1.0f, ((float)var7 - var2) / 2.0f) : 0.0f, var6.nightZenith(), var6.nightHorizon(), var6.twilight());
    }

    private static float clearNight(float var0) {
        float var1 = class_3532.method_15363((float)(0.75f - 2.0f * class_3532.method_15362((double)var0)), (float)0.0f, (float)1.0f);
        return var1 * var1;
    }

    public boolean skyDisc() {
        if (this.gradientSky.get() && this.sky.ready()) {
            this.sky.renderDome(this.world);
            return true;
        }
        return false;
    }

    public boolean replacesSunrise() {
        return this.gradientSky.get() && this.sky.ready();
    }

    public boolean stars(class_4587 var1) {
        float var2 = this.night * this.rainBrightness;
        if (this.milkyWay.get()) {
            this.star.renderBand(var1, var2 * this.milkyWayBrightness.get(), this.currentTheme().milkyWay());
        }
        if (!this.stars.get()) {
            return false;
        }
        this.star.renderStars(var1, var2, this.starBrightness.get());
        return true;
    }

    public void beforeMoon(class_4587 var1) {
        this.moonScaled = this.celestialGlow.get();
        if (this.moonScaled) {
            var1.method_22903();
            var1.method_22905(1.4f, 1.0f, 1.4f);
        }
    }

    public void afterMoon(class_4587 var1) {
        if (this.moonScaled) {
            var1.method_22909();
        }
        this.moonScaled = false;
    }

    public void afterCelestials() {
        long var1 = System.nanoTime();
        float var3 = this.lastFrame == 0L ? 0.0f : Math.min((float)(var1 - this.lastFrame) / 1.0E9f, 0.1f);
        this.lastFrame = var1;
        if (this.sky.ready()) {
            if (this.celestialGlow.get()) {
                this.sky.renderHalos(this.world, this.rainBrightness, this.night);
            }
            if (this.shootingStars.get()) {
                this.sky.renderMeteors(this.world, this.night, this.rainBrightness);
            }
            this.auroraHere += (this.auroraTarget() - this.auroraHere) * Math.min(1.0f, var3 * 0.5f);
            if (this.auroraHere > 0.01f && this.night > 0.01f) {
                SkyPalette var4 = this.currentTheme();
                float var5 = (float)(var1 % 3600000000000L) / 1.0E9f * this.auroraSpeed.get();
                float var6 = this.auroraHere * this.night * (0.35f + 0.65f * this.rainBrightness) * this.auroraBrightness.get();
                this.auroraMesh.render(this.world, var6, var5, var4.auroraLow(), var4.auroraHigh());
            }
        }
    }

    private float auroraTarget() {
        if (this.aurora.is(AURORA_OFF)) {
            return 0.0f;
        }
        if (this.aurora.is(AURORA_ALWAYS)) {
            return 1.0f;
        }
        class_310 var1 = class_310.method_1551();
        if (var1.field_1687 == null) {
            return 0.0f;
        }
        class_2338 var2 = class_2338.method_49638((class_2374)var1.field_1773.method_19418().method_71156());
        return ((class_1959)var1.field_1687.method_23753(var2).comp_349()).method_33599(var2, var1.field_1687.method_8615()) ? 1.0f : 0.0f;
    }

    public void fog(class_4184 var1, class_638 var2, Vector4f var3) {
        if (var2.method_27983() == class_1937.field_25179) {
            if (!this.gradientSky.get() || !this.sky.ready()) {
                return;
            }
            Vector3fc var4 = var1.method_19335();
            int var5 = this.sky.horizonToward(var4.x(), var4.z());
            var3.x = (float)(var5 >> 16 & 0xFF) / 255.0f;
            var3.y = (float)(var5 >> 8 & 0xFF) / 255.0f;
            var3.z = (float)(var5 & 0xFF) / 255.0f;
        } else if (var2.method_27983() == class_1937.field_25180 && this.netherColors.get()) {
            float var6 = 0.2126f * var3.x + 0.7152f * var3.y + 0.0722f * var3.z;
            var3.x = class_3532.method_15363((float)((var6 + (var3.x - var6) * 1.45f) * 1.12f), (float)0.0f, (float)1.0f);
            var3.y = class_3532.method_15363((float)((var6 + (var3.y - var6) * 1.45f) * 1.12f), (float)0.0f, (float)1.0f);
            var3.z = class_3532.method_15363((float)((var6 + (var3.z - var6) * 1.45f) * 1.12f), (float)0.0f, (float)1.0f);
        }
    }

    public int clouds(int var1) {
        class_310 var2 = class_310.method_1551();
        if (this.cloudColors.get() && this.sky.ready() && var2.field_1687 != null && var2.field_1687.method_27983() == class_1937.field_25179) {
            float var3 = this.sky.golden();
            float var4 = this.sky.nightness();
            int var5 = class_9848.method_61319((float)var3, (int)class_9848.method_61319((float)var4, (int)0xFFFFFF, (int)this.currentTheme().nightHorizon()), (int)this.sky.twilightGlow());
            float var6 = Math.max(var3 * 0.75f, var4 * 0.55f);
            return CustomSkysModule.keepBrightness(var1, var5, var6);
        }
        return var1;
    }

    public boolean endSky() {
        if (!this.endSky.get()) {
            return false;
        }
        long var1 = System.nanoTime();
        this.sky.renderEndDome(this.world);
        class_4587 var3 = new class_4587();
        var3.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees((float)(var1 % 1200000000000L) / 1.0E9f * 0.3f));
        this.star.renderNebulae(var3, 1.0f);
        this.star.renderStars(var3, 1.0f, 1.2f);
        return true;
    }

    private SkyPalette currentTheme() {
        for (SkyPalette var4 : THEMES) {
            if (!this.theme.is(var4.name())) continue;
            return var4;
        }
        int var5 = this.nightColor.rgb();
        return new SkyPalette(CUSTOM, SkyDomeRenderer.scale(var5, 0.3f), var5, this.sunsetColor.rgb(), this.auroraColor.rgb(), this.auroraTopColor.rgb(), 0xE8E8FF);
    }

    private static int keepBrightness(int var0, int var1, float var2) {
        if (var2 <= 0.0f) {
            return var0;
        }
        float var3 = (float)(var0 >> 16 & 0xFF) / 255.0f;
        float var4 = (float)(var0 >> 8 & 0xFF) / 255.0f;
        float var5 = (float)(var0 & 0xFF) / 255.0f;
        float var6 = (float)(var1 >> 16 & 0xFF) / 255.0f;
        float var7 = (float)(var1 >> 8 & 0xFF) / 255.0f;
        float var8 = (float)(var1 & 0xFF) / 255.0f;
        float var9 = CustomSkysModule.luminance(var3, var4, var5) / Math.max(CustomSkysModule.luminance(var6, var7, var8), 0.001f);
        var3 = class_3532.method_16439((float)var2, (float)var3, (float)Math.min(1.0f, var6 * var9));
        var4 = class_3532.method_16439((float)var2, (float)var4, (float)Math.min(1.0f, var7 * var9));
        var5 = class_3532.method_16439((float)var2, (float)var5, (float)Math.min(1.0f, var8 * var9));
        return var0 & 0xFF000000 | CustomSkysModule.channel(var3) << 16 | CustomSkysModule.channel(var4) << 8 | CustomSkysModule.channel(var5);
    }

    private static float luminance(float var0, float var1, float var2) {
        return 0.2126f * var0 + 0.7152f * var1 + 0.0722f * var2;
    }

    private static int channel(float var0) {
        return class_3532.method_15340((int)Math.round(var0 * 255.0f), (int)0, (int)255);
    }

    static {
        SkyPalette[] var10000 = new SkyPalette[]{new SkyPalette("Borealis", 198414, 729648, 13073302, 3080090, 9063935, 13163263), new SkyPalette("Nebula", 459791, 2233916, 14053304, 16732120, 5991679, 15124735), new SkyPalette("HookVisuals", 657157, 2890512, 15239498, 16757306, 16730670, 16769200), new SkyPalette("Frost", 264724, 1454154, 11057384, 8382719, 15135999, 0xDDEEFF), new SkyPalette("Crimson", 721926, 3018260, 15227490, 0xFF3355, 16751164, 16765640)};
        THEMES = var10000;
    }

    @Environment(value=EnvType.CLIENT)
    private record SkyPalette(String name, int nightZenith, int nightHorizon, int twilight, int auroraLow, int auroraHigh, int milkyWay) {
    }
}


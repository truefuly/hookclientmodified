package hook.feature;

import hook.module.Category;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ModeSetting;
import hook.module.setting.SettingVisibility;
import hook.module.setting.StorageBlocksSetting;
import hook.utils.EncryptedString;
import java.awt.Color;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_1826;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

/**
 * Mobs through walls, sorted into groups (hostile, neutral, passive, water, villagers, pets, bosses, other things like
 * end crystals and minecarts) - or only the mobs you pick. Mobs that are after someone can be marked.
 */
public final class MobEspModule extends EntityEspBase {
    public enum Filter { Groups, Selected }
    public enum Colors { Group, Health, Distance }

    enum Group {
        BOSS(0xB45AFF), HOSTILE(0xFF7A3C), NEUTRAL(0xFFD24D), PET(0xFF8FD8), VILLAGER(0x5AA8FF), WATER(0x4DE3E3), PASSIVE(0x6EE38B), OTHER(0xC8C8D2);
        final int rgb;

        Group(int rgb) {
            this.rgb = rgb;
        }
    }

    private final ModeSetting<Filter> filter = new ModeSetting<Filter>(EncryptedString.of("Mobs"), Filter.Groups, Filter.class)
            .setDescription(EncryptedString.of("Groups: the mob groups ticked below. Selected: only the mobs picked in the list"));
    private final BooleanSetting hostile = group("Hostile", true, "Zombies, skeletons, creepers, spiders, slimes, phantoms, piglins...");
    private final BooleanSetting neutral = group("Neutral", true, "Attack only when provoked: endermen, zombified piglins, wolves, bees, iron golems, polar bears...");
    private final BooleanSetting passive = group("Passive", false, "Animals: cows, sheep, pigs, chickens, horses, bats...");
    private final BooleanSetting water = group("Water", false, "Squids, fish, dolphins");
    private final BooleanSetting villagers = group("Villagers", false, "Villagers, wandering traders, snow golems");
    private final BooleanSetting pets = group("Pets", true, "Tamed wolves, cats, parrots, horses... (someone lives near)");
    private final BooleanSetting bosses = group("Bosses", true, "Ender dragon, wither, warden, elder guardians");
    private final BooleanSetting others = group("Other Things", false, "End crystals, minecarts, boats, armor stands");
    private final StorageBlocksSetting list = createList();
    private final BooleanSetting aggro = new BooleanSetting(EncryptedString.of("Mark Attacking"), true)
            .setDescription(EncryptedString.of("Mobs that are attacking someone right now show bright red"));
    private final BooleanSetting onlyAggro = new BooleanSetting(EncryptedString.of("Only Attacking"), false)
            .setDescription(EncryptedString.of("Hostile and neutral mobs only while they are attacking someone"));
    private final ModeSetting<Colors> colors = new ModeSetting<Colors>(EncryptedString.of("Default Colors"), Colors.Group, Colors.class)
            .setDescription(EncryptedString.of("Colours in the Default colour mode. Group: hostile orange, neutral yellow, passive green, water cyan, villagers blue, pets pink, bosses purple. Health: green to red. Distance: red close, green far"));

    public MobEspModule() {
        super("Mob ESP", "Shows mobs through walls by group - hostile, neutral, passive, pets, bosses... - or only the ones you pick", Category.RENDER, 35, 0xFFFF9A3C, false);
        this.list.setDescription(EncryptedString.of("Mobs shown in Selected mode"));
        for (BooleanSetting s : new BooleanSetting[]{this.hostile, this.neutral, this.passive, this.water, this.villagers, this.pets, this.bosses, this.others})
            SettingVisibility.set(s, () -> this.filter.getValue() == Filter.Groups);
        SettingVisibility.set(this.list, () -> this.filter.getValue() == Filter.Selected);
        this.addSettings(this.filter, this.hostile, this.neutral, this.passive, this.water, this.villagers, this.pets, this.bosses, this.others, this.list);
        this.addSettings(this.aggro, this.onlyAggro, this.colors, this.healthBar, this.invisible, this.expand);
        this.addSettings(this.style.settings());
    }

    private static BooleanSetting group(String name, boolean on, String desc) {
        return new BooleanSetting(EncryptedString.of(name), on).setDescription(EncryptedString.of(desc));
    }

    static Group groupOf(class_1297 e) {
        if (e instanceof net.minecraft.class_1510 || e instanceof net.minecraft.class_1528 || e instanceof net.minecraft.class_7260 || e instanceof net.minecraft.class_1550) return Group.BOSS;
        if (e instanceof net.minecraft.class_1321 t && t.method_6181() || e instanceof net.minecraft.class_1496 h && h.method_6727()) return Group.PET;
        if (e instanceof net.minecraft.class_5354 && e instanceof class_1308) return Group.NEUTRAL;
        if (e instanceof net.minecraft.class_1569) return Group.HOSTILE;
        if (e instanceof net.minecraft.class_3988 || e instanceof net.minecraft.class_1427) return Group.VILLAGER;
        if (e instanceof net.minecraft.class_9866 || e instanceof net.minecraft.class_1480) return Group.WATER;
        if (e instanceof class_1308) return Group.PASSIVE;
        if (e instanceof class_1309 || e instanceof net.minecraft.class_1511 || e instanceof net.minecraft.class_1688 || e instanceof net.minecraft.class_10255) return Group.OTHER;
        return null;
    }

    private boolean groupOn(Group g) {
        return switch (g) {
            case BOSS -> this.bosses.getValue();
            case HOSTILE -> this.hostile.getValue();
            case NEUTRAL -> this.neutral.getValue();
            case PET -> this.pets.getValue();
            case VILLAGER -> this.villagers.getValue();
            case WATER -> this.water.getValue();
            case PASSIVE -> this.passive.getValue();
            case OTHER -> this.others.getValue();
        };
    }

    private static boolean attacking(class_1297 e) {
        return e instanceof class_1308 m && m.method_6510();
    }

    @Override
    protected boolean wants(class_1297 e) {
        if (e instanceof class_1657) return false;
        Group g = groupOf(e);
        if (g == null) return false;
        if (this.filter.getValue() == Filter.Selected) {
            class_2960 id = class_7923.field_41177.method_10221(e.method_5864());
            StorageBlocksSetting.Entry en = id == null ? null : this.list.get(id.toString());
            if (en == null || !en.enabled()) return false;
        } else if (!this.groupOn(g)) return false;
        return !this.onlyAggro.getValue() || g != Group.HOSTILE && g != Group.NEUTRAL || attacking(e);
    }

    @Override
    protected Color color(class_1297 e, class_243 pos) {
        if (this.aggro.getValue() && attacking(e)) return new Color(0xFF2A2A);
        switch ((Colors) this.colors.getValue()) {
            case Health:
                return health(e);
            case Distance:
                return this.distance(pos);
            default: {
                Group g = groupOf(e);
                return new Color(g == null ? 0xFFFFFF : g.rgb);
            }
        }
    }

    private static StorageBlocksSetting createList() {
        StorageBlocksSetting list = new StorageBlocksSetting(EncryptedString.of("Mob List")).pickerOnly();
        for (class_1299<?> type : class_7923.field_41177) {
            class_2960 id = class_7923.field_41177.method_10221(type);
            if (id == null || id.method_12832().equals("player")) continue;
            class_1826 egg = class_1826.method_8019(type);
            if (egg == null) continue; // only real mobs (they all have a spawn egg)
            list.addEntity(id.toString(), type.method_5897().getString(), (class_1792) egg, type, false, Color.WHITE);
        }
        return list;
    }
}

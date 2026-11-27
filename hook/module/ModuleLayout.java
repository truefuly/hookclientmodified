package hook.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Where every module sits in the click GUI: its tab and the section inside the tab. Combat is split by the way you
 * fight (Sword, Mace, Crystal, Cart...), the other tabs by what the module is for. Modules not listed here keep the
 * tab they were registered in and land in "Other".
 */
public final class ModuleLayout {
    private record Spot(Category category, String sub) {
    }

    private static final Map<String, Spot> SPOTS = new HashMap<>();
    private static final Map<Category, List<String>> ORDER = new HashMap<>();

    static {
        tab(Category.COMBAT, "Sword", "Trigger Bot", "Auto Clicker", "HitBox", "Shield Breaker", "Movement Sync", "True Jump Reset");
        tab(Category.COMBAT, "Mace", "Mace Swap");
        tab(Category.COMBAT, "Spear", "Spear Macro");
        tab(Category.COMBAT, "Crystal", "Auto Crystal", "Anchor Macro", "Double Anchor");
        tab(Category.COMBAT, "Cart", "Xbow Cart", "Bow Cart");
        tab(Category.COMBAT, "Totem", "Auto Totem", "Hover Totem");
        tab(Category.COMBAT, "Pearl", "Pearl Catch");

        tab(Category.RENDER, "Entities", "Player ESP", "Mob ESP", "Item ESP", "Clean Hitboxes", "Hit Glow");
        tab(Category.RENDER, "Info", "Nametags", "Target HUD", "Trajectories", "Effect Timer");
        tab(Category.RENDER, "Blocks", "Block ESP", "Block Notifier", "Spawner Notify");
        tab(Category.RENDER, "Cleanup", "No Fog", "No Particles", "No Render", "Force Render");
        tab(Category.RENDER, "Camera", "Freecam");
        tab(Category.RENDER, "World", "Chunk Borders", "Slime Chunks");

        tab(Category.DONUT, "Bases", "Base ESP", "Storage ESP", "Storage", "Sign ESP", "Portal ESP", "Light ESP", "Entity Clues");
        tab(Category.DONUT, "Chunks", "New Chunks", "Growth Detector", "Kelp ESP", "Amethyst ESP");
        tab(Category.DONUT, "Underground", "Tunnel Finder", "Tunnel ESP", "Deepslate Rotation ESP", "Bedrock ESP");
        tab(Category.DONUT, "Seed", "Seed Cracker");

        tab(Category.MISC, "Movement", "Fly", "Creative");
        tab(Category.MISC, "Mining", "Auto Miner", "Auto Tool", "Anti Break", "Fast Use");
        tab(Category.MISC, "Survival", "Auto Eat");
        tab(Category.MISC, "Privacy", "Name Protect", "Skin Protect", "Anti Screenshare", "Resource Blocker");
        tab(Category.MISC, "Tools", "Quick Utilities");

        tab(Category.AUTOMATION, "Building", "Schematic Builder");
        tab(Category.AUTOMATION, "Farming", "Auto Breed");
        tab(Category.AUTOMATION, "Server", "Auto Tpa", "XP Shop", "Crate Opener");

        tab(Category.VISUALS, "Camera", "Freelook", "Fullbright");
        tab(Category.VISUALS, "World", "Custom Skys", "custom-skys", "Block Overlay", "block-overlay");
        tab(Category.VISUALS, "Player", "3D Character", "character-3d", "Halloween");
        tab(Category.VISUALS, "Hands", "Item Outline", "Swing Speed", "Custom Tools", "Custom Totem");
        tab(Category.VISUALS, "Music", "HUD Lyrics");

        tab(Category.CLIENT, "General", "HookVisuals", "Panic Button");
        tab(Category.CLIENT, "HUD", "HUD");
        tab(Category.CLIENT, "Music", "Music");
    }

    private static void tab(Category category, String sub, String... names) {
        for (String n : names) SPOTS.put(n.toLowerCase(java.util.Locale.ROOT), new Spot(category, sub));
        List<String> order = ORDER.computeIfAbsent(category, c -> new java.util.ArrayList<>());
        if (!order.contains(sub)) order.add(sub);
    }

    private ModuleLayout() {
    }

    /** Moves the module into its tab and section. Macros from the old Macros tab go to Automation › Server. */
    public static void apply(Module module) {
        Spot spot = SPOTS.get(module.getName().toString().toLowerCase(java.util.Locale.ROOT));
        if (spot != null) {
            module.setCategory(spot.category());
            module.setSub(spot.sub());
        } else if (module.getCategory() == Category.MACROS || module.getCategory() == Category.GLAZED) {
            module.setCategory(Category.AUTOMATION);
            module.setSub("Server");
        } else {
            module.setSub("Other");
        }
    }

    /** Position of the section inside its tab ("Other" last). */
    public static int subOrder(Module module) {
        List<String> order = ORDER.get(module.getCategory());
        int i = order == null ? -1 : order.indexOf(module.getSub());
        return i < 0 ? 999 : i;
    }
}

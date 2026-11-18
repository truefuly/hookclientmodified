package hook.feature;

/** Lobby themes: the menu background (and custom buttons) before you join a world. */
public enum LobbyTheme {
    Standard("Standard", null, false, "Vanilla Minecraft menus and buttons"),
    OverworldCastle("Overworld Castle", "castle", false, "Night lake, castle and a mint aurora"),
    OverworldCaves("Overworld Caves", "caves", false, "Lava falls, crystals and glow berries"),
    Nether("Nether", "nether", false, "Lava lakes, fortress and fungi"),
    End("End", "end", false, "End city, crystals and floating islands"),
    // takes the old Halloween slot: settings store the index, so the other themes keep theirs
    HookVisualsDark("HookVisuals Dark", "hookdark", false, "Dark mode with the glowing HK icon"),
    Winter("Winter Edition", "winter", true, "Snowy village, ice lake and snowfall"),
    HookVisuals("HookVisuals Special", "hook", false, "The HK logo, beacons and a neon grid"),
    HookVisualsLight("HookVisuals Light", "hooklight", false, "Light mode with the glowing HK icon");

    /** Order of the tiles in the theme picker (the enum order is fixed by saved settings). */
    public static final LobbyTheme[] DISPLAY = {Standard, HookVisuals, HookVisualsDark, HookVisualsLight, OverworldCastle, OverworldCaves, Nether, End, Winter};

    public final String title;
    public final String folder;
    public final boolean limited;
    public final String description;

    LobbyTheme(String title, String folder, boolean limited, String description) {
        this.title = title;
        this.folder = folder;
        this.limited = limited;
        this.description = description;
    }

    @Override
    public String toString() {
        return this.title;
    }

    public static LobbyTheme selected() {
        try {
            Enum<?> value = hook.module.modules.client.Hook.lobbyTheme.getValue();
            return value instanceof LobbyTheme theme ? theme : OverworldCastle;
        } catch (Throwable t) {
            return OverworldCastle;
        }
    }
}

package hook.feature;

/** Lobby themes: the menu background (and custom buttons) before you join a world. */
public enum LobbyTheme {
    Standard("Standard", null, false, "Vanilla Minecraft menus and buttons"),
    OverworldCastle("Overworld Castle", "castle", false, "Night lake, castle and a mint aurora"),
    OverworldCaves("Overworld Caves", "caves", false, "Lava falls, crystals and glow berries"),
    Nether("Nether", "nether", false, "Lava lakes, fortress and fungi"),
    End("End", "end", false, "End city, crystals and floating islands"),
    Halloween("Halloween Edition", "halloween", true, "Orange moon, haunted house and bats"),
    Winter("Winter Edition", "winter", true, "Snowy village, ice lake and snowfall"),
    HookVisuals("HookVisuals Special", "hook", false, "The HK logo, beacons and a neon grid");

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

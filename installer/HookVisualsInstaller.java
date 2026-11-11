import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/** Double-click installer for the Hook Visuals 1.21.11 Fabric mod. */
public final class HookVisualsInstaller {
    private static final String MOD_FILE = "HookVisuals1.0.35-custom.1.21.11.jar";
    private static final String RESOURCE = "/HookVisuals.jar";

    private HookVisualsInstaller() {}

    public static void main(String[] args) {
        if (Runtime.version().feature() < 21) {
            show("Java 21 oder neuer wird benötigt. Installiere Java 21 und öffne diese Datei erneut.", true);
            return;
        }
        try {
            List<Path> targets = findProfiles();
            if (targets.isEmpty()) {
                show("Kein Modrinth-Fabric-Profil für Minecraft 1.21.11 gefunden.\n"
                        + "Installiere Fabric API 1.21.11 im Modrinth-Profil und starte den Installer erneut.", true);
                return;
            }
            byte[] mod;
            try (InputStream in = HookVisualsInstaller.class.getResourceAsStream(RESOURCE)) {
                if (in == null) throw new IOException("Die eingebettete Mod-Datei fehlt.");
                mod = in.readAllBytes();
            }
            List<String> installed = new ArrayList<>();
            for (Path mods : targets) {
                Files.createDirectories(mods);
                disableOlderCopies(mods);
                Path target = mods.resolve(MOD_FILE);
                Path temp = Files.createTempFile(mods, "HookVisuals-", ".tmp");
                try {
                    Files.write(temp, mod);
                    Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException atomicMoveUnavailable) {
                    Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
                } finally {
                    Files.deleteIfExists(temp);
                }
                installed.add(mods.getParent().getFileName().toString());
            }
            show("Hook Visuals 1.0.35 wurde installiert.\n\nProfil(e): "
                    + String.join(", ", installed)
                    + "\n\nStarte das Profil in Modrinth mit Minecraft 1.21.11.", false);
        } catch (Exception e) {
            show("Installation fehlgeschlagen: " + e.getMessage(), true);
        }
    }

    private static List<Path> findProfiles() throws IOException {
        List<Path> roots = new ArrayList<>();
        String home = System.getProperty("user.home");
        roots.add(Path.of(home, ".local", "share", "ModrinthApp", "profiles"));
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) roots.add(Path.of(appData, "ModrinthApp", "profiles"));
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) roots.add(Path.of(localAppData, "ModrinthApp", "profiles"));
        roots.add(Path.of(home, "Library", "Application Support", "ModrinthApp", "profiles"));

        List<Path> result = new ArrayList<>();
        for (Path root : roots) {
            if (!Files.isDirectory(root)) continue;
            try (DirectoryStream<Path> profiles = Files.newDirectoryStream(root)) {
                for (Path profile : profiles) {
                    Path mods = profile.resolve("mods");
                    if (!Files.isDirectory(mods)) continue;
                    boolean compatible = false;
                    try (DirectoryStream<Path> jars = Files.newDirectoryStream(mods, "fabric-api*1.21.11*.jar")) {
                        compatible = jars.iterator().hasNext();
                    }
                    if (compatible) result.add(mods);
                }
            }
        }
        return result;
    }

    private static void disableOlderCopies(Path mods) throws IOException {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(mods, "HookVisuals*.jar")) {
            for (Path file : files) {
                if (file.getFileName().toString().equals(MOD_FILE)) continue;
                Files.move(file, file.resolveSibling(file.getFileName() + ".disabled"), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static void show(String message, boolean error) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println(message);
            return;
        }
        JOptionPane.showMessageDialog(null, message, "Hook Visuals Installer",
                error ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }
}

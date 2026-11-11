package hook.feature;

import com.spotifyclient.module.ClientModule;
import com.spotifyclient.module.ModuleRegistry;
import com.spotifyclient.ModSettings;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BindSetting;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ButtonSetting;
import hook.module.setting.ChoiceSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.RgbSetting;
import hook.module.setting.Setting;
import hook.module.setting.SettingVisibility;
import hook.module.setting.StringSetting;
import hook.utils.EncryptedString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Mirrors a VerseFall visual module and its settings into Hook's Visuals category. */
public final class VerseFallModuleBridge extends Module {
    private final String verseFallModuleName;
    private final List<SettingBinding> settingBindings = new ArrayList<>();

    public VerseFallModuleBridge(String name, String description, String verseFallModuleName) {
        super(EncryptedString.of(name), EncryptedString.of(description), -1, Category.VISUALS);
        this.verseFallModuleName = verseFallModuleName;
        ClientModule target = this.target();
        if (target != null) {
            this.setEnabled(target.isOn());
            if (target.settings() != null) {
                for (com.spotifyclient.module.Setting source : target.settings().get()) addMirror(source, List.of());
            }
        }
    }

    @Override
    public void onEnable() {
        setVerseFallEnabled(true);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        setVerseFallEnabled(false);
        super.onDisable();
    }

    /** Called by ModuleManager, including while this bridge module is disabled. */
    public void syncSettings() {
        for (SettingBinding binding : this.settingBindings) binding.sync();
        if ("HUD Lyrics".equals(this.verseFallModuleName)) ModSettings.lyricsBarLogo = false;
    }

    private ClientModule target() {
        try {
            return ModuleRegistry.byName(this.verseFallModuleName);
        } catch (Throwable error) {
            System.err.println("Hook: VerseFall module unavailable: " + this.verseFallModuleName + " (" + error + ")");
            return null;
        }
    }

    private void setVerseFallEnabled(boolean enabled) {
        ClientModule module = this.target();
        if (module != null && module.hasToggle()) module.setOn(enabled);
    }

    private void addMirror(com.spotifyclient.module.Setting original, List<BooleanSupplier> visibility) {
        if (original instanceof com.spotifyclient.module.Setting.Shown shown) {
            ArrayList<BooleanSupplier> conditions = new ArrayList<>(visibility);
            conditions.add(shown.when());
            this.addMirror(shown.setting(), conditions);
            return;
        }

        com.spotifyclient.module.Setting source = com.spotifyclient.module.Setting.unwrap(original);
        if ("HUD Lyrics".equals(this.verseFallModuleName)
                && source instanceof com.spotifyclient.module.Setting.Toggle toggle
                && "Client logo".equals(toggle.label())) {
            toggle.set().accept(false);
            return;
        }
        if (source instanceof com.spotifyclient.module.Setting.Header header) {
            this.addSetting(new VerseFallDisplaySetting(header.title(), null));
        } else if (source instanceof com.spotifyclient.module.Setting.Info info) {
            this.addSetting(new VerseFallDisplaySetting("Info", info.text()));
        } else if (source instanceof com.spotifyclient.module.Setting.Toggle toggle) {
            BooleanSetting mirror = new BooleanSetting(EncryptedString.of(toggle.label()), toggle.get().getAsBoolean());
            this.bind(mirror, () -> toggle.get().getAsBoolean(), value -> toggle.set().accept((Boolean)value),
                () -> mirror.getValue(), value -> mirror.setValue((Boolean)value), visibility);
        } else if (source instanceof com.spotifyclient.module.Setting.Slide slide) {
            NumberSetting mirror = new NumberSetting(EncryptedString.of(slide.label()), slide.min(), slide.max(), slide.get().getAsDouble(), Math.max(0.001, slide.step()));
            this.bind(mirror, () -> slide.get().getAsDouble(), value -> slide.set().accept((Double)value),
                () -> mirror.getValue(), value -> mirror.getValue((Double)value), visibility);
        } else if (source instanceof com.spotifyclient.module.Setting.Choice choice) {
            List<String> options = Arrays.asList(choice.options());
            int index = choice.get().getAsInt();
            String initial = options.isEmpty() ? "" : options.get(Math.max(0, Math.min(index, options.size() - 1)));
            ChoiceSetting mirror = new ChoiceSetting(EncryptedString.of(choice.label()), initial, options);
            this.bind(mirror, () -> choice.get().getAsInt(), value -> choice.set().accept((Integer)value),
                () -> mirror.getIndex(), value -> mirror.setIndex((Integer)value), visibility);
        } else if (source instanceof com.spotifyclient.module.Setting.ColorPick color) {
            RgbSetting mirror = new RgbSetting(EncryptedString.of(color.label()), color.get().getAsInt());
            this.bind(mirror, () -> color.get().getAsInt(), value -> color.set().accept((Integer)value),
                () -> mirror.getArgb(), value -> mirror.setArgb((Integer)value), visibility);
        } else if (source instanceof com.spotifyclient.module.Setting.KeyPick key) {
            BindSetting mirror = new BindSetting(EncryptedString.of(key.label()), key.get().getAsInt(), false);
            this.bind(mirror, () -> key.get().getAsInt(), value -> key.set().accept((Integer)value),
                () -> mirror.getValue(), value -> mirror.setValue((Integer)value), visibility);
        } else if (source instanceof com.spotifyclient.module.Setting.Multi multi) {
            String[] options = multi.options();
            for (int i = 0; i < options.length && i < 31; i++) {
                int bit = 1 << i;
                BooleanSetting mirror = new BooleanSetting(EncryptedString.of(multi.label() + " / " + options[i]), (multi.mask().getAsInt() & bit) != 0);
                this.bind(mirror, () -> (multi.mask().getAsInt() & bit) != 0,
                    value -> {
                        int mask = multi.mask().getAsInt();
                        multi.set().accept((Boolean)value ? mask | bit : mask & ~bit);
                    }, () -> mirror.getValue(), value -> mirror.setValue((Boolean)value), visibility);
            }
        } else if (source instanceof com.spotifyclient.module.Setting.Action action) {
            ButtonSetting mirror = new ButtonSetting(EncryptedString.of(action.label()), action.run());
            this.addVisibleSetting(mirror, visibility);
        } else if (source instanceof com.spotifyclient.module.Setting.Input input) {
            StringSetting mirror = new StringSetting(EncryptedString.of(input.placeholder()), "");
            this.bind(mirror, () -> "", value -> input.submit().accept((String)value),
                () -> mirror.getValue(), value -> mirror.setValue((String)value), visibility);
        } else if (source instanceof com.spotifyclient.module.Setting.ListRow row) {
            ButtonSetting mirror = new ButtonSetting(EncryptedString.of(row.label()), row.click());
            this.addVisibleSetting(mirror, visibility);
        }
    }

    private void bind(Setting mirror, Supplier<Object> sourceGet, Consumer<Object> sourceSet,
                      Supplier<Object> mirrorGet, Consumer<Object> mirrorSet, List<BooleanSupplier> visibility) {
        this.addVisibleSetting(mirror, visibility);
        this.settingBindings.add(new SettingBinding(mirror, sourceGet, sourceSet, mirrorGet, mirrorSet));
    }

    private void addVisibleSetting(Setting setting, List<BooleanSupplier> visibility) {
        this.addSetting(setting);
        if (!visibility.isEmpty()) {
            SettingVisibility.set(setting, () -> visibility.stream().allMatch(condition -> {
                try { return condition.getAsBoolean(); }
                catch (Throwable ignored) { return true; }
            }));
        }
    }

    private static final class SettingBinding {
        private final Supplier<Object> sourceGet;
        private final Consumer<Object> sourceSet;
        private final Supplier<Object> mirrorGet;
        private final Consumer<Object> mirrorSet;
        private Object lastSource;
        private Object lastMirror;

        private SettingBinding(Setting mirror, Supplier<Object> sourceGet, Consumer<Object> sourceSet,
                              Supplier<Object> mirrorGet, Consumer<Object> mirrorSet) {
            this.sourceGet = sourceGet;
            this.sourceSet = sourceSet;
            this.mirrorGet = mirrorGet;
            this.mirrorSet = mirrorSet;
            this.lastSource = sourceGet.get();
            this.lastMirror = mirrorGet.get();
        }

        private void sync() {
            Object source = this.sourceGet.get();
            Object mirror = this.mirrorGet.get();
            if (!Objects.equals(mirror, this.lastMirror)) {
                this.sourceSet.accept(mirror);
                source = this.sourceGet.get();
            } else if (!Objects.equals(source, this.lastSource)) {
                this.mirrorSet.accept(source);
                mirror = this.mirrorGet.get();
            }
            this.lastSource = source;
            this.lastMirror = mirror;
        }
    }

    private static final class VerseFallDisplaySetting extends Setting {
        private final Supplier<String> info;

        private VerseFallDisplaySetting(String title, Supplier<String> info) {
            super(EncryptedString.of(title));
            this.info = info;
        }

        @Override
        public CharSequence getDescription() {
            if (this.info == null) return null;
            try {
                String text = this.info.get();
                return text == null ? null : EncryptedString.of(text);
            } catch (Throwable ignored) {
                return null;
            }
        }
    }
}

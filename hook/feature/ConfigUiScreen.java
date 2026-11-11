/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 */
package hook.feature;

import hook.Hook;
import hook.feature.UiAudio;
import hook.feature.UiEasing;
import hook.feature.UiIcon;
import hook.feature.UiPalette;
import hook.feature.UiRenderer;
import hook.manager.ConfigLibrary;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.class_310;

public final class ConfigUiScreen {
    private static final int TEXT = UiRenderer.white(0.96f);
    private static final int SUB = UiRenderer.white(0.55f);
    private final Supplier<ConfigLibrary> library;
    private final Supplier<String> clipboard;
    private final Consumer<String> copy;
    private final List<ActionButton> hits = new ArrayList<ActionButton>();
    private final Map<String, Float> hover = new HashMap<String, Float>();
    private boolean opened;
    private boolean selectAll;
    private float progress;
    private float unit;
    private float delta;
    private float x;
    private float y;
    private float width;
    private float height;
    private float caret;
    private double mx;
    private double my;
    private PendingAction dialog = PendingAction.NONE;
    private String target;
    private String input = "";
    private String status = "";
    private String selection;
    private boolean error;

    public ConfigUiScreen() {
        this(() -> Hook.INSTANCE.getSettingsManager().configs(), () -> class_310.method_1551().field_1774.method_1460(), var0 -> class_310.method_1551().field_1774.method_1455(var0));
    }

    public ConfigUiScreen(Supplier<ConfigLibrary> var1, Supplier<String> var2, Consumer<String> var3) {
        this.library = var1;
        this.clipboard = var2;
        this.copy = var3;
    }

    public void open() {
        this.opened = true;
        this.dialog = PendingAction.NONE;
        this.status = "";
        this.error = false;
        try {
            this.selection = this.library.get().selected();
        }
        catch (RuntimeException var2) {
            this.fail(var2);
        }
        UiAudio.play(UiAudio.Cue.OPEN);
    }

    public void close() {
        this.opened = false;
        this.dialog = PendingAction.NONE;
        UiAudio.play(UiAudio.Cue.BACK);
    }

    public boolean active() {
        return this.opened || this.progress > 0.005f;
    }

    public boolean isOpen() {
        return this.opened;
    }

    public float visibility() {
        return this.progress;
    }

    private float d(float var1) {
        return var1 * this.unit;
    }

    private void fail(RuntimeException var1) {
        this.status = var1.getMessage() == null ? "Couldn't complete this action." : var1.getMessage();
        this.error = true;
    }

    private void run(Runnable var1, String var2) {
        try {
            var1.run();
            this.status = var2;
            this.error = false;
            UiAudio.play(UiAudio.Cue.CONFIRM);
        }
        catch (RuntimeException var4) {
            this.fail(var4);
        }
    }

    public void render(UiRenderer var1, float var2, float var3, float var4, float var5, float var6, float var7, double var8, double var10, boolean var12) {
        this.unit = var6;
        this.delta = var7;
        this.mx = var8;
        this.my = var10;
        this.caret += var7;
        this.progress += ((this.opened ? 1.0f : 0.0f) - this.progress) * (1.0f - (float)Math.exp(-14.0f * var7));
        if (this.progress < 0.005f && !this.opened) {
            this.progress = 0.0f;
            this.hits.clear();
        } else {
            List<ConfigLibrary.Profile> var15;
            this.x = var2;
            this.y = var3;
            this.width = var4;
            this.height = var5;
            this.hits.clear();
            var1.pushAlpha(this.progress);
            float var13 = this.d(6.0f) * (1.0f - UiEasing.out(this.progress));
            this.y += var13;
            boolean var14 = this.opened && var12 && this.progress > 0.6f;
            try {
                var15 = this.library.get().profiles();
            }
            catch (RuntimeException var27) {
                this.fail(var27);
                var15 = List.of();
            }
            var1.round(this.x + this.d(22.0f), this.y + this.d(18.0f), this.d(44.0f), this.d(44.0f), this.d(14.0f), UiRenderer.white(0.07f));
            var1.outline(this.x + this.d(22.0f), this.y + this.d(18.0f), this.d(44.0f), this.d(44.0f), this.d(14.0f), Math.max(1.0f, this.unit * 0.9f), UiRenderer.white(0.09f));
            var1.icon(UiIcon.FOLDER, this.x + this.d(33.5f), this.y + this.d(29.5f), this.d(21.0f), TEXT);
            var1.text("Configs", this.x + this.d(82.0f), this.y + this.d(19.0f), this.d(21.0f), 1, TEXT);
            var1.text(var15.size() + " / 5 saved", this.x + this.d(83.0f), this.y + this.d(45.0f), this.d(12.0f), 0, SUB);
            this.iconButton(var1, "close", UiIcon.CLOSE, "Close", this.x + this.width - this.d(54.0f), this.y + this.d(24.0f), this.d(32.0f), var14, this::close);
            var1.rect(this.x + this.d(20.0f), this.y + this.d(78.0f), this.width - this.d(40.0f), Math.max(1.0f, this.unit), UiRenderer.white(0.055f));
            boolean var16 = var15.size() >= 5;
            this.button(var1, "create", "Create new", UiIcon.PLUS, this.x + this.d(22.0f), this.y + this.d(94.0f), this.d(132.0f), this.d(34.0f), var14 && !var16, true, () -> this.begin(PendingAction.CREATE, null, ""));
            this.button(var1, "paste", "Paste config", UiIcon.COPY, this.x + this.d(164.0f), this.y + this.d(94.0f), this.d(132.0f), this.d(34.0f), var14 && !var16, false, () -> this.run(() -> {
                this.selection = this.library.get().pasteCode(this.clipboard.get());
            }, "Imported. Click Load to apply."));
            this.button(var1, "reset", "Reset all", UiIcon.RESET, this.x + this.width - this.d(136.0f), this.y + this.d(94.0f), this.d(114.0f), this.d(34.0f), var14, false, () -> this.begin(PendingAction.RESET, null, ""));
            float var17 = this.y + this.d(144.0f);
            float var18 = this.width - this.d(44.0f);
            if (var15.isEmpty()) {
                var1.textCentered("No saved configs", this.x + this.width / 2.0f, this.y + this.d(281.0f), this.d(14.0f), 1, SUB);
                var1.textCentered("Create new or paste a config.", this.x + this.width / 2.0f, this.y + this.d(308.0f), this.d(11.5f), 0, UiRenderer.white(0.34f));
            }
            Iterator<ConfigLibrary.Profile> iterator = var15.iterator();
            while (iterator.hasNext()) {
                ConfigLibrary.Profile var20Raw;
                ConfigLibrary.Profile var20 = var20Raw = iterator.next();
                String var21 = var20.id();
                boolean var22 = Objects.equals(this.selection, var21);
                float var23 = this.x + this.d(22.0f);
                float var24 = this.d(58.0f);
                float var25 = this.animate("row:" + var21, this.hot(var23, var17, var18, var24) && var14 ? 1.0f : 0.0f);
                var1.round(var23, var17, var18, var24, this.d(16.0f), UiEasing.mix(UiRenderer.white((var22 ? 0.065f : 0.03f) + 0.025f * var25), UiPalette.accent(0.065f + 0.025f * var25), var22 ? 0.25f : 0.0f));
                var1.roundGradient(var23, var17, var18, var24, this.d(16.0f), UiRenderer.white(0.025f), UiRenderer.white(0.0f));
                var1.outline(var23, var17, var18, var24, this.d(16.0f), Math.max(1.0f, this.unit * 0.9f), UiRenderer.white((var22 ? 0.12f : 0.055f) + 0.05f * var25));
                var1.icon(var22 ? UiIcon.CHECK : UiIcon.FOLDER, var23 + this.d(15.0f), var17 + this.d(17.0f), this.d(19.0f), var22 ? UiPalette.accent(0.9f) : SUB);
                var1.text(var1.ellipsize(var20.name(), var18 - this.d(265.0f), this.d(14.0f), 1), var23 + this.d(47.0f), var17 + this.d(10.0f), this.d(14.0f), 1, TEXT);
                var1.text(var20.enabledCount() + " enabled", var23 + this.d(47.0f), var17 + this.d(33.0f), this.d(10.5f), 0, SUB);
                if (var14 && this.dialog == PendingAction.NONE) {
                    this.hits.add(new ActionButton(var23, var17, var18, var24, () -> this.run(() -> {
                        this.library.get().select(var21);
                        this.selection = var21;
                    }, "")));
                }
                float var26 = var23 + var18 - this.d(211.0f);
                this.iconButton(var1, "rename:" + var21, UiIcon.EDIT, "Rename", var26, var17 + this.d(11.0f), this.d(32.0f), var14, () -> this.begin(PendingAction.RENAME, var21, var20.name()));
                this.iconButton(var1, "copy:" + var21, UiIcon.COPY, "Copy code", var26 + this.d(38.0f), var17 + this.d(11.0f), this.d(32.0f), var14, () -> this.run(() -> this.copy.accept(this.library.get().copyCode(var21)), "Code copied."));
                this.iconButton(var1, "delete:" + var21, UiIcon.TRASH, "Delete", var26 + this.d(76.0f), var17 + this.d(11.0f), this.d(32.0f), var14, () -> this.begin(PendingAction.DELETE, var21, var20.name()));
                this.button(var1, "load:" + var21, "Load", null, var26 + this.d(120.0f), var17 + this.d(10.0f), this.d(76.0f), this.d(35.0f), var14, var22, () -> this.run(() -> {
                    this.library.get().loadConfig(var21);
                    this.selection = var21;
                }, "Loaded " + var20.name() + "."));
                var17 += this.d(68.0f);
            }
            float var28 = this.y + this.height - this.d(54.0f);
            var1.rect(this.x + this.d(20.0f), var28 - this.d(13.0f), this.width - this.d(40.0f), Math.max(1.0f, this.unit), UiRenderer.white(0.055f));
            this.button(var1, "save", "Save current", UiIcon.CHECK, this.x + this.d(22.0f), var28, this.d(145.0f), this.d(36.0f), var14 && this.selection != null, true, () -> this.run(() -> this.library.get().saveCurrent(this.selection), "Saved."));
            var1.text(var1.ellipsize(this.status, this.width - this.d(210.0f), this.d(10.7f), 0), this.x + this.d(185.0f), var28 + this.d(11.0f), this.d(10.7f), 0, this.error ? UiRenderer.white(0.92f) : SUB);
            if (this.dialog != PendingAction.NONE) {
                this.drawDialog(var1, var14);
            }
            var1.popAlpha();
        }
    }

    private float animate(String var1, float var2) {
        float var3 = this.hover.getOrDefault(var1, Float.valueOf(0.0f)).floatValue();
        var3 += (var2 - var3) * (1.0f - (float)Math.exp(-14.0f * this.delta));
        this.hover.put(var1, Float.valueOf(var3));
        return var3;
    }

    private boolean hot(float var1, float var2, float var3, float var4) {
        return this.mx >= (double)var1 && this.my >= (double)var2 && this.mx < (double)(var1 + var3) && this.my < (double)(var2 + var4);
    }

    private void button(UiRenderer var1, String var2, String var3, UiIcon var4, float var5, float var6, float var7, float var8, boolean var9, boolean var10, Runnable var11) {
        boolean var12;
        boolean bl = var12 = var9 && this.dialog == PendingAction.NONE && this.hot(var5, var6, var7, var8);
        if (var2.startsWith("dialog:")) {
            var12 = var9 && this.hot(var5, var6, var7, var8);
        }
        float var13 = this.animate(var2, var12 ? 1.0f : 0.0f);
        var1.round(var5, var6, var7, var8, this.d(12.0f), UiEasing.mix(UiRenderer.white((var10 && var9 ? 0.07f : 0.035f) + 0.04f * var13), UiPalette.accent(0.09f + 0.04f * var13), var10 && var9 ? 0.25f : 0.0f));
        var1.roundGradient(var5, var6, var7, var8, this.d(12.0f), UiRenderer.white(0.025f), UiRenderer.white(0.0f));
        var1.outline(var5, var6, var7, var8, this.d(12.0f), Math.max(1.0f, this.unit * 0.9f), UiRenderer.white(var9 ? 0.08f + 0.075f * var13 : 0.035f));
        int var14 = UiRenderer.white(var9 ? 0.84f + 0.12f * var13 : 0.26f);
        if (var4 != null) {
            var1.icon(var4, var5 + this.d(13.0f), var6 + (var8 - this.d(15.0f)) / 2.0f, this.d(15.0f), var14);
        }
        var1.textCentered(var3, var5 + var7 / 2.0f + (var4 == null ? 0.0f : this.d(9.0f)), var6 + (var8 - this.d(11.5f)) / 2.0f - this.d(0.5f), this.d(11.5f), 1, var14);
        if (var9 && (this.dialog == PendingAction.NONE || var2.startsWith("dialog:"))) {
            this.hits.add(new ActionButton(var5, var6, var7, var8, var11));
        }
    }

    private void iconButton(UiRenderer var1, String var2, UiIcon var3, String var4, float var5, float var6, float var7, boolean var8, Runnable var9) {
        float var10 = this.animate(var2, var8 && this.dialog == PendingAction.NONE && this.hot(var5, var6, var7, var7) ? 1.0f : 0.0f);
        var1.round(var5, var6, var7, var7, this.d(10.0f), UiRenderer.white(0.02f + 0.075f * var10));
        var1.icon(var3, var5 + (var7 - this.d(16.0f)) / 2.0f, var6 + (var7 - this.d(16.0f)) / 2.0f, this.d(16.0f), UiRenderer.white(var8 ? 0.55f + 0.4f * var10 : 0.25f));
        if (var10 > 0.35f) {
            float var11 = var1.textWidth(var4, this.d(10.0f), 0) + this.d(16.0f);
            float var12 = var5 + var7 / 2.0f - var11 / 2.0f;
            var1.round(var12, var6 - this.d(29.0f), var11, this.d(24.0f), this.d(8.0f), UiPalette.glass(0.96f));
            var1.textCentered(var4, var5 + var7 / 2.0f, var6 - this.d(23.0f), this.d(10.0f), 0, TEXT);
        }
        if (var8 && this.dialog == PendingAction.NONE) {
            this.hits.add(new ActionButton(var5, var6, var7, var7, var9));
        }
    }

    private void begin(PendingAction var1, String var2, String var3) {
        this.dialog = var1;
        this.target = var2;
        this.input = var3;
        this.selectAll = var1 == PendingAction.RENAME;
        this.status = "";
        this.error = false;
        this.caret = 0.0f;
        UiAudio.play(UiAudio.Cue.OPEN);
    }

    private void drawDialog(UiRenderer var1, boolean var2) {
        this.hits.clear();
        var1.round(this.x, this.y, this.width, this.height, this.d(22.0f), UiRenderer.argb(0.42f, 5, 6, 9));
        float var3 = this.d(444.0f);
        float var4 = this.d(222.0f);
        float var5 = this.x + (this.width - var3) / 2.0f;
        float var6 = this.y + (this.height - var4) / 2.0f;
        var1.shadow(var5, var6, var3, var4, this.d(20.0f), this.d(30.0f), UiRenderer.argb(0.55f, 0, 0, 0));
        var1.round(var5, var6, var3, var4, this.d(20.0f), UiPalette.glass(0.94f));
        var1.roundGradient(var5, var6, var3, var4, this.d(20.0f), UiRenderer.white(0.05f), UiRenderer.white(0.0f));
        var1.outline(var5, var6, var3, var4, this.d(20.0f), Math.max(1.0f, this.unit * 0.9f), UiRenderer.white(0.1f));
        boolean var7 = this.dialog == PendingAction.CREATE || this.dialog == PendingAction.RENAME;
        String var8 = switch (this.dialog.ordinal()) {
            case 1 -> "New config";
            case 2 -> "Rename config";
            case 3 -> "Delete config?";
            default -> "Reset all?";
        };
        var1.text(var8, var5 + this.d(24.0f), var6 + this.d(22.0f), this.d(19.0f), 1, TEXT);
        String var9 = var7 ? "Name" : (this.dialog == PendingAction.DELETE ? "Remove this saved config." : "Reset modules, settings, binds and layout.");
        var1.text(var1.ellipsize(var9, var3 - this.d(48.0f), this.d(11.0f), 0), var5 + this.d(24.0f), var6 + this.d(54.0f), this.d(11.0f), 0, SUB);
        if (var7) {
            float var10 = var5 + this.d(24.0f);
            float var11 = var6 + this.d(83.0f);
            float var12 = var3 - this.d(48.0f);
            float var13 = this.d(43.0f);
            var1.round(var10, var11, var12, var13, this.d(12.0f), UiRenderer.white(0.055f));
            var1.outline(var10, var11, var12, var13, this.d(12.0f), Math.max(1.0f, this.unit), UiPalette.accent(0.35f));
            if (this.selectAll && !this.input.isEmpty()) {
                var1.round(var10 + this.d(12.0f), var11 + this.d(10.0f), Math.min(var12 - this.d(24.0f), var1.textWidth(this.input, this.d(13.0f), 0)), this.d(22.0f), this.d(4.0f), UiPalette.accent(0.18f));
            }
            var1.pushClip(var10 + this.d(12.0f), var11, var12 - this.d(24.0f), var13);
            float var14 = var1.textWidth(this.input, this.d(13.0f), 0);
            float var15 = Math.max(0.0f, var14 - var12 + this.d(28.0f));
            var1.text(this.input.isEmpty() ? "Config name" : this.input, var10 + this.d(13.0f) - var15, var11 + this.d(13.0f), this.d(13.0f), 0, this.input.isEmpty() ? SUB : TEXT);
            if (this.caret % 1.2f < 0.6f) {
                var1.rect(var10 + this.d(14.0f) + var14 - var15, var11 + this.d(11.0f), Math.max(1.0f, this.unit), this.d(21.0f), TEXT);
            }
            var1.popClip();
        } else {
            var1.text(this.dialog == PendingAction.DELETE ? this.input : "Your saved configs will remain available.", var5 + this.d(24.0f), var6 + this.d(99.0f), this.d(13.0f), 1, TEXT);
        }
        if (this.error) {
            var1.text(var1.ellipsize(this.status, var3 - this.d(48.0f), this.d(10.5f), 0), var5 + this.d(24.0f), var6 + this.d(140.0f), this.d(10.5f), 0, TEXT);
        }
        this.button(var1, "dialog:cancel", "Cancel", null, var5 + this.d(24.0f), var6 + var4 - this.d(55.0f), this.d(116.0f), this.d(34.0f), var2, false, () -> {
            this.dialog = PendingAction.NONE;
            this.status = "";
            this.error = false;
        });
        String var16 = switch (this.dialog.ordinal()) {
            case 1 -> "Create";
            case 2 -> "Rename";
            case 3 -> "Delete";
            default -> "Reset All";
        };
        this.button(var1, "dialog:confirm", var16, null, var5 + var3 - this.d(146.0f), var6 + var4 - this.d(55.0f), this.d(122.0f), this.d(34.0f), var2, true, this::confirm);
    }

    private void confirm() {
        try {
            switch (this.dialog.ordinal()) {
                case 1: {
                    this.selection = this.library.get().create(this.input);
                    this.status = "Config created.";
                    break;
                }
                case 2: {
                    this.library.get().rename(this.target, this.input);
                    this.status = "Config renamed.";
                    break;
                }
                case 3: {
                    this.library.get().delete(this.target);
                    this.selection = this.library.get().selected();
                    this.status = "Config deleted.";
                    break;
                }
                case 4: {
                    this.library.get().resetCurrent();
                    this.status = "Reset complete.";
                    break;
                }
                default: {
                    return;
                }
            }
            this.dialog = PendingAction.NONE;
            this.error = false;
            UiAudio.play(UiAudio.Cue.CONFIRM);
        }
        catch (RuntimeException var2) {
            this.fail(var2);
        }
    }

    public boolean mouseClicked(double var1, double var3, int var5) {
        if (!this.active()) {
            return false;
        }
        if (this.opened && !(this.progress < 0.6f) && var5 == 0) {
            for (int var6 = this.hits.size() - 1; var6 >= 0; --var6) {
                ActionButton var7 = this.hits.get(var6);
                if (!(var1 >= (double)var7.x) || !(var3 >= (double)var7.y) || !(var1 < (double)(var7.x + var7.w)) || !(var3 < (double)(var7.y + var7.h))) continue;
                var7.action.run();
                return true;
            }
            if (!(this.dialog != PendingAction.NONE || var1 >= (double)this.x && var3 >= (double)this.y && var1 < (double)(this.x + this.width) && var3 < (double)(this.y + this.height))) {
                this.close();
            }
            return true;
        }
        return true;
    }

    public boolean keyPressed(int var1, int var2) {
        boolean var4;
        if (!this.active()) {
            return false;
        }
        if (var1 == 256) {
            if (this.dialog != PendingAction.NONE) {
                this.dialog = PendingAction.NONE;
                this.error = false;
                this.status = "";
            } else {
                this.close();
            }
            return true;
        }
        boolean var3 = this.dialog == PendingAction.CREATE || this.dialog == PendingAction.RENAME;
        boolean bl = var4 = (var2 & 2) != 0;
        if (var1 != 257 && var1 != 335) {
            if (var3) {
                if (var4 && var1 == 65) {
                    this.selectAll = true;
                } else if (var4 && var1 == 67) {
                    this.copy.accept(this.input);
                } else if (var4 && var1 == 86) {
                    String var5 = this.clipboard.get().replaceAll("[\\p{Cntrl}]", "");
                    this.input = (this.selectAll ? "" : this.input) + var5;
                    this.input = this.input.substring(0, Math.min(32, this.input.length()));
                    this.selectAll = false;
                } else if (var1 == 259 || var1 == 261) {
                    this.input = !this.selectAll && !var4 ? this.input.substring(0, Math.max(0, this.input.length() - 1)) : "";
                    this.selectAll = false;
                }
                this.caret = 0.0f;
            }
            return true;
        }
        if (this.dialog != PendingAction.NONE) {
            this.confirm();
        }
        return true;
    }

    public boolean charTyped(char var1) {
        if (!this.active()) {
            return false;
        }
        if (!(Character.isISOControl(var1) || this.dialog != PendingAction.CREATE && this.dialog != PendingAction.RENAME)) {
            if (this.selectAll) {
                this.input = "";
            }
            if (this.input.length() < 32) {
                this.input = this.input + var1;
            }
            this.selectAll = false;
            this.caret = 0.0f;
        }
        return true;
    }

    private static enum PendingAction {
        NONE,
        CREATE,
        RENAME,
        DELETE,
        RESET;

    }

    private record ActionButton(float x, float y, float w, float h, Runnable action) {
    }
}


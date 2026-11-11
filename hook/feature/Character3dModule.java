/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10017
 *  net.minecraft.class_10055
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_156
 *  net.minecraft.class_310
 *  net.minecraft.class_3887
 *  net.minecraft.class_4507
 *  net.minecraft.class_638
 *  net.minecraft.class_746
 *  net.minecraft.class_970
 *  net.minecraft.class_972
 *  net.minecraft.class_976
 *  net.minecraft.class_978
 *  net.minecraft.class_979
 *  net.minecraft.class_983
 *  net.minecraft.class_989
 */
package hook.feature;

import hook.feature.BakedCharacterData;
import hook.feature.Character3dHelper;
import hook.feature.CharacterDefinition;
import hook.feature.CharacterDetailLevel;
import hook.feature.CharacterModelCache;
import hook.feature.CharacterMotionAnimator;
import hook.feature.CharacterRenderStateAccess;
import hook.feature.FreecamModule;
import hook.feature.LocalCharacterAssetLibrary;
import hook.feature.WornCharacterModel;
import hook.module.ext.ClientModule;
import hook.module.ext.ModuleCategory;
import hook.module.ext.ModuleRegistry;
import hook.module.ext.Stage;
import hook.module.ext.event.TickEvent;
import hook.module.ext.setting.ActionSetting;
import hook.module.ext.setting.BooleanSetting;
import hook.module.ext.setting.ModeSetting;
import hook.utils.ext.ModuleChat;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10017;
import net.minecraft.class_10055;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_156;
import net.minecraft.class_310;
import net.minecraft.class_3887;
import net.minecraft.class_4507;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_970;
import net.minecraft.class_972;
import net.minecraft.class_976;
import net.minecraft.class_978;
import net.minecraft.class_979;
import net.minecraft.class_983;
import net.minecraft.class_989;

@Environment(value=EnvType.CLIENT)
public final class Character3dModule
extends ClientModule {
    private static final long FRIEND_REFRESH_NANOS = 1000000000L;
    private static final float MODEL_PIXEL = 0.05859375f;
    private static volatile Character3dModule active;
    private final ModeSetting character = this.addSetting("character", Character3dModule.characterSetting());
    private final ModeSetting detail;
    private final BooleanSetting heldItems;
    private final Int2BooleanOpenHashMap friendCache;
    private long friendsCheckedAt;
    private int scanTicks;
    private WeakReference<class_638> level;

    public Character3dModule() {
        super("character-3d", "3D Character", "Replaces your player model with a 3D character.", ModuleCategory.VISUALS);
        String var10002 = "detail";
        String var10005 = "Detail";
        String var10006 = CharacterDetailLevel.MEDIUM.label;
        String[] var10007 = new String[]{CharacterDetailLevel.LOW.label, CharacterDetailLevel.MEDIUM.label, CharacterDetailLevel.HIGH.label};
        this.detail = this.addSetting(var10002, new ModeSetting(var10005, var10006, var10007));
        this.heldItems = this.addSetting("held-items", new BooleanSetting("Held Items", false));
        this.friendCache = new Int2BooleanOpenHashMap();
        this.level = new WeakReference<Object>(null);
        this.addSetting("characters-folder", new ActionSetting("Open Characters Folder", this::openFolder));
        this.character.onChange(this::prepare);
        this.detail.onChange(this::prepare);
    }

    private static ModeSetting characterSetting() {
        Character3dHelper.refresh();
        List<String> var0 = Character3dHelper.names();
        return new ModeSetting("Character", var0.get(0), var0.toArray(new String[0]));
    }

    private void refreshCharacters() {
        if (Character3dHelper.refresh()) {
            this.character.setModes(Character3dHelper.names());
        }
    }

    private void openFolder() {
        Path var1 = LocalCharacterAssetLibrary.folder();
        try {
            Files.createDirectories(var1, new FileAttribute[0]);
        }
        catch (IOException var3) {
            ModuleChat.send((ClientModule)this, ModuleChat.body().danger("Couldn't create ").text(var1.toString()));
            return;
        }
        this.refreshCharacters();
        class_156.method_668().method_60932(var1);
    }

    private void prepare() {
        if (this.isEnabled()) {
            CharacterModelCache.prepare(Character3dHelper.byName(this.character.get()), CharacterDetailLevel.byLabel(this.detail.get()));
        }
    }

    @Override
    public void onEnable() {
        CharacterModelCache.listen();
        this.reset();
        this.refreshCharacters();
        this.prepare();
        active = this;
        this.on(TickEvent.TYPE, Stage.POST, var1 -> {
            this.watchLevel();
            if (++this.scanTicks % 40 == 0) {
                this.refreshCharacters();
            }
        });
    }

    @Override
    public void onDisable() {
        if (active == this) {
            active = null;
        }
        this.reset();
        CharacterModelCache.clear();
    }

    private void reset() {
        this.friendCache.clear();
        CharacterMotionAnimator.clear();
        this.level = new WeakReference<class_638>(class_310.method_1551().field_1687);
    }

    private void watchLevel() {
        class_638 var1 = class_310.method_1551().field_1687;
        if (this.level.get() != var1) {
            this.reset();
            CharacterModelCache.releaseTextures();
        }
    }

    public static void extract(class_1297 var0, class_10055 var1) {
        Character3dModule var2 = active;
        WornCharacterModel var3 = var2 == null ? null : var2.resolve(var1);
        CharacterRenderStateAccess var4 = (CharacterRenderStateAccess)var1;
        var4.hook$setCharacter(var3);
        if (var3 != null) {
            class_1297 var5 = var0.method_5854();
            var4.hook$setSeat(var5 == null ? 0.0f : (var5 instanceof class_1309 ? 1.35f : 0.3f));
            if (var1.field_53338 != null) {
                BakedCharacterData var6 = var3.model;
                float var7 = var6.maxY * var6.scale - 32.0f;
                if (var7 > 0.0f) {
                    var1.field_53338 = var1.field_53338.method_1031(0.0, (double)(var7 * 0.05859375f * var1.field_53453), 0.0);
                }
            }
        }
    }

    static void baking(String var0) {
        Character3dModule var1 = active;
        if (var1 != null) {
            ModuleChat.send((ClientModule)var1, ModuleChat.body().text("Preparing ").name(var0).text(", big models take a few seconds the first time"));
        }
    }

    static void notice(String var0, List<String> var1) {
        Character3dModule var2 = active;
        if (var2 != null) {
            ModuleChat.Body var3 = ModuleChat.body().name(var0);
            if (var1.isEmpty()) {
                var3.text(" has no skeleton the client recognises, so it moves as one piece");
            } else {
                var3.text(" only moves its ").value(String.join((CharSequence)" and ", var1)).text(", the rest of its skeleton wasn't recognised");
            }
            ModuleChat.send((ClientModule)var2, var3);
        }
    }

    static void failed(CharacterDefinition var0, Throwable var1) {
        Character3dModule var2 = active;
        if (var2 != null) {
            Throwable var3 = var1 instanceof UncheckedIOException && var1.getCause() != null ? var1.getCause() : var1;
            String var4 = !(var3 instanceof IOException) && !(var3 instanceof IllegalStateException) ? "the file couldn't be read, see the log" : var3.getMessage();
            ModuleChat.send((ClientModule)var2, ModuleChat.body().text("Couldn't load ").name(var0.name()).text(": ").danger(var4));
        }
    }

    static WornCharacterModel worn(class_10017 var0) {
        WornCharacterModel wornCharacterModel;
        if (var0 instanceof CharacterRenderStateAccess) {
            CharacterRenderStateAccess var1 = (CharacterRenderStateAccess)var0;
            wornCharacterModel = var1.hook$character();
        } else {
            wornCharacterModel = null;
        }
        return wornCharacterModel;
    }

    static float seat(class_10017 var0) {
        float f;
        if (var0 instanceof CharacterRenderStateAccess) {
            CharacterRenderStateAccess var1 = (CharacterRenderStateAccess)var0;
            f = var1.hook$seat();
        } else {
            f = 0.0f;
        }
        return f;
    }

    public static boolean hidesModel(class_10017 var0) {
        return Character3dModule.worn(var0) != null;
    }

    public static boolean hidesLayer(class_3887<?, ?> var0, class_10017 var1) {
        return Character3dModule.worn(var1) == null ? false : var0 instanceof class_970 || var0 instanceof class_972 || var0 instanceof class_979 || var0 instanceof class_4507 || var0 instanceof class_976 || var0 instanceof class_983 || var0 instanceof class_978 || var0 instanceof class_989 && !Character3dModule.showsHeldItems();
    }

    private static boolean showsHeldItems() {
        Character3dModule var0 = active;
        return var0 != null && var0.heldItems.get();
    }

    static WornCharacterModel firstPerson() {
        Character3dModule var0 = active;
        if (var0 == null) {
            return null;
        }
        class_746 var1 = class_310.method_1551().field_1724;
        if (var1 != null && !var1.method_7325()) {
            WornCharacterModel var2 = CharacterModelCache.ready(Character3dHelper.byName(var0.character.get()), CharacterDetailLevel.byLabel(var0.detail.get()));
            return var2 != null && var2.model.firstPersonArms ? var2 : null;
        }
        return null;
    }

    private WornCharacterModel resolve(class_10055 var1) {
        if (var1.field_53542) {
            return null;
        }
        class_310 var2 = class_310.method_1551();
        return !this.wears(var2, var1) ? null : CharacterModelCache.ready(Character3dHelper.byName(this.character.get()), CharacterDetailLevel.byLabel(this.detail.get()));
    }

    private boolean wears(class_310 var1, class_10055 var2) {
        class_746 var3 = var1.field_1724;
        if (var3 != null && var1.field_1687 != null) {
            if (var2.field_53528 != var3.method_5628()) {
                return false;
            }
            FreecamModule var4 = ModuleRegistry.get(FreecamModule.class);
            return (var4 == null || !var4.isEnabled()) && var1.field_1690.method_31044().method_31034() ? var1.field_1755 != null && !var3.method_6113() : true;
        }
        return false;
    }

    private boolean isFriend(class_310 var1, int var2) {
        long var3 = System.nanoTime();
        if (var3 - this.friendsCheckedAt > 1000000000L) {
            this.friendCache.clear();
            this.friendsCheckedAt = var3;
        }
        if (this.friendCache.containsKey(var2)) {
            return this.friendCache.get(var2);
        }
        boolean var5 = false;
        this.friendCache.put(var2, var5);
        return var5;
    }
}


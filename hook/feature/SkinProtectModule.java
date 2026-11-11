/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.class_1068
 *  net.minecraft.class_640
 *  net.minecraft.class_8685
 */
package hook.feature;

import com.mojang.authlib.GameProfile;
import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.Setting;
import hook.module.setting.StringSetting;
import hook.utils.EncryptedString;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import net.minecraft.class_1068;
import net.minecraft.class_640;
import net.minecraft.class_8685;

public final class SkinProtectModule
extends Module {
    private static final long CONSTANT_0332 = TimeUnit.MILLISECONDS.toNanos(350L);
    private static final long CONSTANT_0103 = TimeUnit.SECONDS.toNanos(30L);
    private final StringSetting skinName = new StringSetting("Skin Name", "").setDescription(EncryptedString.of("Minecraft username to use for the skin and cape. Blank uses the default skin"));
    private final BooleanSetting protectEverybody = new BooleanSetting(EncryptedString.of("Protect Everybody"), false).setDescription(EncryptedString.of("Show the selected skin and cape on every player instead of only yourself"));
    private volatile class_8685 selectedTextures = class_1068.method_62620();
    private String requestedKey = "";
    private String requestedName = "";
    private CompletableFuture<Optional<class_8685>> pendingLookup;
    private long nextLookupNanos;
    private int lookupVersion;

    public SkinProtectModule() {
        super(EncryptedString.of("Skin Protect"), EncryptedString.of("Locally replaces player skins, capes and elytra textures"), -1, Category.MISC);
        Setting[] var10001 = new Setting[]{this.skinName, this.protectEverybody};
        this.addSettings(var10001);
    }

    @Override
    public void onEnable() {
        this.clearResolution();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.clearResolution();
        super.onDisable();
    }

    private void clearResolution() {
        ++this.lookupVersion;
        this.pendingLookup = null;
        this.selectedTextures = class_1068.method_62620();
        this.requestedKey = "";
        this.requestedName = "";
        this.nextLookupNanos = 0L;
    }

    @EventListener
    public void onTick(TickEvent var1) {
        long var2 = System.nanoTime();
        String var4 = this.skinName.getValue() == null ? "" : this.skinName.getValue().trim();
        String var5 = var4.toLowerCase(Locale.ROOT);
        if (!var5.equals(this.requestedKey)) {
            this.requestedKey = var5;
            this.requestedName = var4;
            ++this.lookupVersion;
            this.pendingLookup = null;
            this.selectedTextures = class_1068.method_62620();
            this.nextLookupNanos = var2 + CONSTANT_0332;
        }
        if (this.pendingLookup == null && var2 >= this.nextLookupNanos && this.requestedName.matches("[A-Za-z0-9_]{3,16}")) {
            this.resolveTextures(this.requestedName);
        }
    }

    private void resolveTextures(String var1) {
        class_640 var4;
        int var2 = this.lookupVersion;
        GameProfile var3 = null;
        if (this.mc.method_1562() != null && (var4 = this.mc.method_1562().method_2874(var1)) != null) {
            var3 = var4.method_2966();
        }
        CompletableFuture<Optional<GameProfile>> var5 = var3 == null
            ? CompletableFuture.supplyAsync(() -> this.mc.method_73361().comp_4624().method_73289(var1))
            : CompletableFuture.completedFuture(Optional.of(var3));
        CompletableFuture<Optional<class_8685>> var6 = var5.thenCompose(var1x -> var1x
            .map(var1xx -> CompletableFuture.supplyAsync(() -> this.mc.method_1582().method_52863(var1xx)).thenCompose(var0 -> var0))
            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty())));
        this.pendingLookup = var6;
        var6.whenComplete((var2x, var3x) -> this.mc.execute(() -> {
            if (var2 == this.lookupVersion && this.isEnabled()) {
                this.pendingLookup = null;
                if (var3x == null && var2x != null && var2x.isPresent()) {
                    this.selectedTextures = var2x.get();
                    this.nextLookupNanos = Long.MAX_VALUE;
                } else {
                    this.selectedTextures = class_1068.method_62620();
                    this.nextLookupNanos = System.nanoTime() + CONSTANT_0103;
                }
            }
        }));
    }

    public class_8685 replacementFor(UUID var1, String var2, class_8685 var3) {
        if (!this.isEnabled()) {
            return var3;
        }
        return !this.protectEverybody.getValue() && !this.isOwnPlayer(var1, var2) ? var3 : this.selectedTextures;
    }

    private boolean isOwnPlayer(UUID var1, String var2) {
        UUID var3 = this.mc.method_1548().method_44717();
        return var3 != null && var3.equals(var1) || var2 != null && var2.equalsIgnoreCase(this.mc.method_1548().method_1676());
    }
}


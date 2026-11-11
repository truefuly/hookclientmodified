/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1511
 *  net.minecraft.class_1621
 *  net.minecraft.class_1657
 *  net.minecraft.class_1802
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_239
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_3965
 *  net.minecraft.class_3966
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.PreItemUseEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BindSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.Setting;
import hook.utils.BlockUtil;
import hook.utils.EncryptedString;
import hook.utils.KeyUtils;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_1621;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_3965;
import net.minecraft.class_3966;

public final class AutoCrystalModule
extends Module {
    private final BindSetting activateKey = new BindSetting(EncryptedString.of("Activate Key"), 1, false).setDescription(EncryptedString.of("Key that does the crystalling"));
    private final NumberSetting placeDelay = new NumberSetting(EncryptedString.of("Place Delay"), 0.0, 20.0, 0.0, 1.0);
    private final NumberSetting breakDelay = new NumberSetting(EncryptedString.of("Break Delay"), 0.0, 20.0, 0.0, 1.0);
    private final hook.module.setting.BooleanSetting smartPlace = new hook.module.setting.BooleanSetting(EncryptedString.of("Smart Place"), true).setDescription(EncryptedString.of("Never places twice on the same block before the server shows the crystal (ping-aware, like Crystal Optimizer)"));
    private int placeDelayCounter;
    private int breakDelayCounter;
    public boolean isActive;
    private final java.util.Map<Long, Long> pendingBases = new java.util.HashMap<>();

    public AutoCrystalModule() {
        super(EncryptedString.of("Auto Crystal"), EncryptedString.of("Places and breaks end crystals for you while you hold the key"), -1, Category.COMBAT);
        Setting[] var10001 = new Setting[]{this.activateKey, this.placeDelay, this.breakDelay, this.smartPlace};
        this.addSettings(var10001);
    }

    @Override
    public void onEnable() {
        this.resetCounters();
        this.pendingBases.clear();
        this.isActive = false;
        super.onEnable();
    }

    private int ping() {
        if (this.mc.method_1562() == null || this.mc.field_1724 == null) {
            return 0;
        }
        net.minecraft.class_640 info = this.mc.method_1562().method_2871(this.mc.field_1724.method_5667());
        return info == null ? 0 : info.method_2959();
    }

    /** A base stays blocked after a placement until a crystal shows up on it or the ping-aware timeout ends. */
    private boolean baseBlocked(class_2338 base) {
        if (!this.smartPlace.getValue()) {
            return false;
        }
        long now = System.currentTimeMillis();
        this.pendingBases.entrySet().removeIf(e -> now - e.getValue() > 5000L);
        Long sent = this.pendingBases.get(base.method_10063());
        if (sent == null) {
            return false;
        }
        class_2338 up = base.method_10084();
        class_238 space = new class_238(up.method_10263(), up.method_10264(), up.method_10260(), up.method_10263() + 1.0, up.method_10264() + 2.0, up.method_10260() + 1.0);
        if (!this.mc.field_1687.method_8390(class_1511.class, space, e -> true).isEmpty()) {
            this.pendingBases.remove(base.method_10063());
            return false;
        }
        long timeout = Math.max(150L, Math.min(2000L, Math.max(0L, (long) this.ping()) * 2L + 100L));
        return now - sent < timeout;
    }

    private void resetCounters() {
        this.placeDelayCounter = 0;
        this.breakDelayCounter = 0;
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (this.mc.field_1755 == null) {
            this.updateCounters();
            if (!this.mc.field_1724.method_6115() && this.isKeyActive() && this.mc.field_1724.method_6047().method_7909() == class_1802.field_8301) {
                this.handleInteraction();
            }
        }
    }

    private void updateCounters() {
        if (this.placeDelayCounter > 0) {
            --this.placeDelayCounter;
        }
        if (this.breakDelayCounter > 0) {
            --this.breakDelayCounter;
        }
    }

    private boolean isKeyActive() {
        int var1 = this.activateKey.getValue();
        if (var1 != -1 && !KeyUtils.isKeyPressed(var1)) {
            this.resetCounters();
            this.isActive = false;
            return false;
        }
        this.isActive = true;
        return true;
    }

    private void handleInteraction() {
        class_239 var1 = this.mc.field_1765;
        if (this.mc.field_1765 instanceof class_3965) {
            this.handleBlockInteraction((class_3965)var1);
        } else {
            class_239 class_2392 = this.mc.field_1765;
            if (class_2392 instanceof class_3966) {
                class_3966 var2 = (class_3966)class_2392;
                this.handleEntityInteraction(var2);
            }
        }
    }

    private void handleBlockInteraction(class_3965 var1) {
        class_2338 var2;
        if (var1.method_17783() == class_239.class_240.field_1332 && this.placeDelayCounter <= 0 && (BlockUtil.isBlockAtPosition(var2 = var1.method_17777(), class_2246.field_10540) || BlockUtil.isBlockAtPosition(var2, class_2246.field_9987)) && this.isValidCrystalPlacement(var2) && !this.baseBlocked(var2)) {
            BlockUtil.interactWithBlock(var1, true);
            this.pendingBases.put(var2.method_10063(), System.currentTimeMillis());
            this.placeDelayCounter = this.placeDelay.getIntValue();
        }
    }

    private void handleEntityInteraction(class_3966 var1) {
        class_1297 var2;
        if (this.breakDelayCounter <= 0 && ((var2 = var1.method_17782()) instanceof class_1511 || var2 instanceof class_1621)) {
            this.mc.field_1761.method_2918((class_1657)this.mc.field_1724, var2);
            this.mc.field_1724.method_6104(class_1268.field_5808);
            this.breakDelayCounter = this.breakDelay.getIntValue();
        }
    }

    @EventListener
    public void onPreItemUse(PreItemUseEvent var1) {
        class_239 class_2392;
        if (this.mc.field_1724.method_6047().method_7909() == class_1802.field_8301 && (class_2392 = this.mc.field_1765) instanceof class_3965) {
            class_2338 var4;
            class_3965 var2 = (class_3965)class_2392;
            if (this.mc.field_1765.method_17783() == class_239.class_240.field_1332 && (BlockUtil.isBlockAtPosition(var4 = var2.method_17777(), class_2246.field_10540) || BlockUtil.isBlockAtPosition(var4, class_2246.field_9987))) {
                var1.cancel();
            }
        }
    }

    private boolean isValidCrystalPlacement(class_2338 var1) {
        class_2338 var2 = var1.method_10084();
        if (!this.mc.field_1687.method_22347(var2)) {
            return false;
        }
        int var3 = var2.method_10263();
        int var4 = var2.method_10264();
        int var5 = var2.method_10260();
        return this.mc.field_1687.method_8335(null, new class_238((double)var3, (double)var4, (double)var5, (double)var3 + 1.0, (double)var4 + 2.0, (double)var5 + 1.0)).isEmpty();
    }
}


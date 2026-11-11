/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1297$class_5529
 *  net.minecraft.class_1299
 *  net.minecraft.class_238
 *  net.minecraft.class_638
 *  net.minecraft.class_746
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.utils.EncryptedString;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_238;
import net.minecraft.class_638;
import net.minecraft.class_746;

public final class AntiTrapModule
extends Module {
    public AntiTrapModule() {
        super(EncryptedString.of("Anti Trap"), EncryptedString.of("Clears nearby armor stands and chest minecarts blocking your movement"), -1, Category.DONUT);
    }

    @Override
    public void onEnable() {
        this.clearBlockingEntities();
        super.onEnable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        this.clearBlockingEntities();
    }

    private void clearBlockingEntities() {
        class_746 player = this.mc.field_1724;
        class_638 world = this.mc.field_1687;
        if (player == null || world == null) {
            return;
        }
        class_238 nearby = player.method_5829().method_1014(0.35);
        for (class_1297 entity : world.method_8333((class_1297)player, nearby, candidate -> AntiTrapModule.isTrapEntity(candidate) && candidate.method_5829().method_994(nearby))) {
            if (entity.method_31481()) continue;
            entity.method_5650(class_1297.class_5529.field_26999);
        }
    }

    private static boolean isTrapEntity(class_1297 entity) {
        return entity != null && (entity.method_5864() == class_1299.field_6131 || entity.method_5864() == class_1299.field_6126);
    }
}


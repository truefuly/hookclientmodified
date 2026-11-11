/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_490
 */
package hook.utils;

import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_490;

public class FakeInvScreen
extends class_490 {
    public FakeInvScreen(class_1657 playerEntity) {
        super(playerEntity);
    }

    protected void method_2383(class_1735 slot, int slotId, int button, class_1713 actionType) {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}


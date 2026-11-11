/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1937
 *  net.minecraft.class_310
 *  net.minecraft.class_634
 *  net.minecraft.class_638
 *  net.minecraft.class_746
 */
package hook.utils.ext;

import hook.utils.ext.DonutRegionGrid;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_638;
import net.minecraft.class_746;

@Environment(value=EnvType.CLIENT)
public final class DonutRegionTracker {
    private static final int OFF_MAP = Integer.MIN_VALUE;
    private class_638 level;
    private class_746 self;
    private String brand;
    private int goliath = Integer.MIN_VALUE;

    public boolean moved(class_310 client) {
        class_638 currentLevel = client.field_1687;
        class_746 currentPlayer = client.field_1724;
        if (currentLevel == null || currentPlayer == null) {
            this.clear();
            return false;
        }
        class_634 network = client.method_1562();
        String currentBrand = network == null ? null : network.method_52790();
        int currentRegion = DonutRegionTracker.region(currentLevel, currentPlayer);
        boolean changed = this.level != null && (currentLevel != this.level || currentPlayer != this.self && !this.self.method_29504() || currentRegion != this.goliath || currentBrand != null && this.brand != null && !currentBrand.equals(this.brand));
        this.level = currentLevel;
        this.self = currentPlayer;
        this.brand = currentBrand;
        this.goliath = currentRegion;
        return changed;
    }

    public void clear() {
        this.level = null;
        this.self = null;
        this.brand = null;
        this.goliath = Integer.MIN_VALUE;
    }

    private static int region(class_638 level, class_746 player) {
        return level.method_27983() == class_1937.field_25179 ? DonutRegionGrid.goliathAt(player.method_23317(), player.method_23321()) : Integer.MIN_VALUE;
    }
}


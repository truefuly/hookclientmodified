/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_2400
 *  net.minecraft.class_3940
 *  net.minecraft.class_3940$class_11941
 *  net.minecraft.class_4002
 *  net.minecraft.class_5819
 *  net.minecraft.class_638
 *  net.minecraft.class_703
 *  net.minecraft.class_707
 */
package thetruetrident.swordcore;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2400;
import net.minecraft.class_3940;
import net.minecraft.class_4002;
import net.minecraft.class_5819;
import net.minecraft.class_638;
import net.minecraft.class_703;
import net.minecraft.class_707;

@Environment(value=EnvType.CLIENT)
public class MovementSyncInnerParticle
extends class_3940 {
    private final float initialScale;

    private MovementSyncInnerParticle(class_638 world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, class_4002 spriteProvider, class_5819 random) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider.method_18139(random));
        this.field_3852 = velocityX;
        this.field_3869 = velocityY;
        this.field_3850 = velocityZ;
        this.field_3847 = 12;
        this.initialScale = this.field_17867 = 0.08f;
        this.field_3844 = 0.0f;
        this.field_62636 = 1.0f;
    }

    protected class_3940.class_11941 method_74255() {
        return class_3940.class_11941.field_62641;
    }

    public void method_3070() {
        super.method_3070();
        float progress = Math.min(1.0f, (float)this.field_3866 / (float)this.field_3847);
        this.field_17867 = this.initialScale * (1.0f - progress);
        this.field_62638 = this.field_62637;
        this.field_62637 += 0.35f;
        this.field_62636 = 1.0f;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Factory
    implements class_707<class_2400> {
        private final class_4002 spriteProvider;

        public Factory(class_4002 spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public class_703 createParticle(class_2400 type, class_638 world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, class_5819 random) {
            return new MovementSyncInnerParticle(world, x, y, z, velocityX, velocityY, velocityZ, this.spriteProvider, random);
        }
    }
}


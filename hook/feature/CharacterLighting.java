/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_2874$class_12325
 *  net.minecraft.class_308$class_11274
 *  net.minecraft.class_310
 *  net.minecraft.class_638
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package hook.feature;

import hook.feature.DirectionalLighting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2874;
import net.minecraft.class_308;
import net.minecraft.class_310;
import net.minecraft.class_638;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public final class CharacterLighting {
    private static final Vector3f LIGHT_0 = new Vector3f(0.2f, 1.0f, -0.7f).normalize();
    private static final Vector3f LIGHT_1 = new Vector3f(-0.2f, 1.0f, 0.7f).normalize();
    private static final DirectionalLighting NETHER = new DirectionalLighting(0.2f, 1.0f, -0.7f, -0.2f, -1.0f, 0.7f);
    private static final DirectionalLighting INVENTORY = new DirectionalLighting(0.2f, -1.0f, 1.0f, -0.2f, -1.0f, 0.0f);
    private static final DirectionalLighting ITEMS_FLAT = CharacterLighting.transformed(new Matrix4f().rotationY(-0.3926991f).rotateX(2.3561945f));
    private static final DirectionalLighting ITEMS_3D = CharacterLighting.transformed(new Matrix4f().scaling(1.0f, -1.0f, 1.0f).rotateYXZ(1.0821041f, 3.2375858f, 0.0f).rotateYXZ(-0.3926991f, 2.3561945f, 0.0f));
    private static class_308.class_11274 entry = class_308.class_11274.field_60025;

    private CharacterLighting() {
    }

    public static void setup(class_308.class_11274 type) {
        entry = type;
    }

    static DirectionalLighting current() {
        return switch (entry) {
            case class_308.class_11274.field_60025 -> {
                if (CharacterLighting.nether()) {
                    yield NETHER;
                }
                yield DirectionalLighting.WORLD;
            }
            case class_308.class_11274.field_60026 -> ITEMS_FLAT;
            case class_308.class_11274.field_60027 -> ITEMS_3D;
            case class_308.class_11274.field_60028, class_308.class_11274.field_60029 -> INVENTORY;
            default -> throw new MatchException(null, null);
        };
    }

    private static boolean nether() {
        class_638 world = class_310.method_1551().field_1687;
        return world != null && world.method_8597().comp_5221() == class_2874.class_12325.field_64381;
    }

    private static DirectionalLighting transformed(Matrix4f transform) {
        Vector3f a = transform.transformDirection((Vector3fc)LIGHT_0, new Vector3f());
        Vector3f b = transform.transformDirection((Vector3fc)LIGHT_1, new Vector3f());
        return new DirectionalLighting(a.x, a.y, a.z, b.x, b.y, b.z);
    }
}


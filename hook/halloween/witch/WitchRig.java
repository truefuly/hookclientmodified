/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package hook.halloween.witch;

import hook.halloween.witch.WitchAsset;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class WitchRig {
    public final WitchAsset asset;
    public final int count;
    private final Matrix4f[] restLocal;
    private final Matrix4f[] invBind;
    private final Quaternionf[] bindRot;
    private final Quaternionf[] bindRotInv;
    public final Matrix4f[] world;
    public final Matrix4f[] skin;
    public final Quaternionf[] rot;
    public final Vector3f rootOffset = new Vector3f();
    private final Quaternionf tmpQ = new Quaternionf();

    public WitchRig(WitchAsset var1) {
        this.asset = var1;
        this.count = var1.jointNames.length;
        this.restLocal = new Matrix4f[this.count];
        this.invBind = new Matrix4f[this.count];
        this.bindRot = new Quaternionf[this.count];
        this.bindRotInv = new Quaternionf[this.count];
        this.world = new Matrix4f[this.count];
        this.skin = new Matrix4f[this.count];
        this.rot = new Quaternionf[this.count];
        for (int var2 = 0; var2 < this.count; ++var2) {
            Matrix4f var3 = var1.bind[var2];
            this.invBind[var2] = new Matrix4f((Matrix4fc)var3).invert();
            int var4 = var1.parent[var2];
            this.restLocal[var2] = var4 < 0 ? new Matrix4f((Matrix4fc)var3) : new Matrix4f((Matrix4fc)var1.bind[var4]).invert().mul((Matrix4fc)var3);
            this.bindRot[var2] = var3.getNormalizedRotation(new Quaternionf());
            this.bindRotInv[var2] = new Quaternionf((Quaternionfc)this.bindRot[var2]).invert();
            this.world[var2] = new Matrix4f((Matrix4fc)var3);
            this.skin[var2] = new Matrix4f();
            this.rot[var2] = new Quaternionf();
        }
    }

    public void reset() {
        for (Quaternionf var4 : this.rot) {
            var4.identity();
        }
        this.rootOffset.zero();
    }

    public void solve() {
        for (int var1 = 0; var1 < this.count; ++var1) {
            Matrix4f var2 = this.world[var1];
            int var3 = this.asset.parent[var1];
            if (var3 < 0) {
                var2.translation((Vector3fc)this.rootOffset).mul((Matrix4fc)this.restLocal[var1]);
            } else {
                var2.set((Matrix4fc)this.world[var3]).mul((Matrix4fc)this.restLocal[var1]);
            }
            Quaternionf var4 = this.rot[var1];
            if (var4.x != 0.0f || var4.y != 0.0f || var4.z != 0.0f) {
                this.tmpQ.set((Quaternionfc)this.bindRotInv[var1]).mul((Quaternionfc)var4).mul((Quaternionfc)this.bindRot[var1]);
                var2.rotate((Quaternionfc)this.tmpQ);
            }
            this.skin[var1].set((Matrix4fc)var2).mul((Matrix4fc)this.invBind[var1]);
        }
    }

    public Vector3f position(int var1, Vector3f var2) {
        return this.world[var1].getTranslation(var2);
    }

    public Vector3f bindPosition(int var1, Vector3f var2) {
        return this.asset.bind[var1].getTranslation(var2);
    }
}


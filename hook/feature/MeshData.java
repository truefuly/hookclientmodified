/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
final class MeshData {
    final float[] pos;
    final int vertexCount;
    final int[] tris;
    final int triCount;

    MeshData(float[] positions, int vertexCount, int[] triangles, int triangleCount) {
        this.pos = positions;
        this.vertexCount = vertexCount;
        this.tris = triangles;
        this.triCount = triangleCount;
    }
}


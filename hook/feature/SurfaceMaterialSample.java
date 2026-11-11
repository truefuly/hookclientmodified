/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import java.nio.charset.StandardCharsets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class SurfaceMaterialSample {
    public float r;
    public float g;
    public float b;
    public float glow;
    public float flat;
    public float flatWeight;
    public float occlusion;
    public float occlusionWeight;

    public void set(int rgb) {
        this.r = (float)(rgb >> 16 & 0xFF) / 255.0f;
        this.g = (float)(rgb >> 8 & 0xFF) / 255.0f;
        this.b = (float)(rgb & 0xFF) / 255.0f;
        this.glow = 0.0f;
        this.flat = 0.0f;
        this.flatWeight = 0.0f;
        this.occlusion = 0.0f;
        this.occlusionWeight = 0.0f;
    }

    public void set(SurfaceMaterialSample other) {
        this.r = other.r;
        this.g = other.g;
        this.b = other.b;
        this.glow = other.glow;
        this.flat = other.flat;
        this.flatWeight = other.flatWeight;
        this.occlusion = other.occlusion;
        this.occlusionWeight = other.occlusionWeight;
    }

    public void mix(SurfaceMaterialSample other, float amount) {
        float remaining = 1.0f - amount;
        this.r = this.r * remaining + other.r * amount;
        this.g = this.g * remaining + other.g * amount;
        this.b = this.b * remaining + other.b * amount;
        this.glow = this.glow * remaining + other.glow * amount;
        this.flat = this.flat * remaining + other.flat * amount;
        this.flatWeight = this.flatWeight * remaining + other.flatWeight * amount;
        this.occlusion = this.occlusion * remaining + other.occlusion * amount;
        this.occlusionWeight = this.occlusionWeight * remaining + other.occlusionWeight * amount;
    }

    public void toward(int rgb, float amount) {
        float remaining = 1.0f - amount;
        this.r = this.r * remaining + (float)(rgb >> 16 & 0xFF) / 255.0f * amount;
        this.g = this.g * remaining + (float)(rgb >> 8 & 0xFF) / 255.0f * amount;
        this.b = this.b * remaining + (float)(rgb & 0xFF) / 255.0f * amount;
    }

    public int argb() {
        return 0xFF000000 | SurfaceMaterialSample.channel(this.r) << 16 | SurfaceMaterialSample.channel(this.g) << 8 | SurfaceMaterialSample.channel(this.b);
    }

    float flat(float base) {
        return SurfaceMaterialSample.blend(base, this.flat, this.flatWeight);
    }

    float occlusion(float base) {
        return SurfaceMaterialSample.blend(base, this.occlusion, this.occlusionWeight);
    }

    private static float blend(float base, float target, float weight) {
        if (weight <= 1.0E-4f) {
            return base;
        }
        float clampedWeight = Math.min(1.0f, weight);
        return Math.max(0.0f, Math.min(1.0f, base * (1.0f - clampedWeight) + target * (clampedWeight / weight)));
    }

    static int channel(float value) {
        int rounded = Math.round(value * 255.0f);
        return rounded >= 0 ? Math.min(rounded, 255) : 0;
    }

    public static String decodeString(byte[] data, byte[] key, int seed) {
        byte[] seedBytes = Integer.toString(seed).getBytes();
        for (int i = 0; i < data.length; ++i) {
            data[i] = (byte)(data[i] ^ seedBytes[i % seedBytes.length]);
            data[i] = (byte)(data[i] ^ key[i % key.length]);
        }
        return new String(data, StandardCharsets.UTF_16);
    }
}


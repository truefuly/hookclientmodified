/*
 * Decompiled with CFR 0.152.
 */
package hook.itemfx.mesh;

import hook.itemfx.mesh.Sd;

public final class Mat {
    public static final int MATTE = 0;
    public static final int METAL = 1;
    public static final int CHROME = 2;
    public static final int EMISSIVE = 3;
    public static final int GEM = 4;
    public static final int CHARRED = 5;
    public static final int CARVE = 6;
    public static final int MEMBRANE = 7;
    public static final int PUMPKIN = 8;
    public static final int WOOD = 9;
    public static final int LEATHER = 10;
    public static final int STONE = 11;
    public static final int CRYSTAL = 12;
    public static final int HORN = 13;
    public static final int BLADE = 14;
    public static final int R_STATIC = 0;
    public static final int R_WING_L = 1;
    public static final int R_WING_R = 2;
    public static final int R_HEM = 3;
    public static final int R_HEAD = 4;
    public static final int R_FLOW = 5;
    public static final int S_SPOOKY = 20;
    public static final int S_CHROME = 21;
    public static final int S_GHOST = 22;
    public static final int S_DECOR = 23;
    public static final int S_HALO = 24;
    public static final int R_HAT = 11;
    public static final int R_SPIDER = 12;
    public static final int R_THREAD = 13;
    public static final int R_BAT1 = 14;
    public static final int R_BAT2 = 15;
    public static final int ATLAS = 128;
    public static final int[] SPOOKY_CELL = new int[]{0, 0, 32, 32};
    public static final int[] CHROME_CELL = new int[]{64, 0, 32, 32};
    public static final int[] PICK_CELL = new int[]{0, 32, 32, 32};
    public static final int[] HAT_CELL = new int[]{64, 32, 16, 16};
    public static final int[] WEB_CELL = new int[]{80, 32, 16, 16};
    public static final int[] SPIDER_CELL = new int[]{0, 64, 7, 6};
    public static final int[] BAT_A_CELL = new int[]{16, 64, 12, 8};
    public static final int[] BAT_B_CELL = new int[]{64, 64, 12, 8};
    public static final int[] SKULL_A_CELL = new int[]{80, 64, 12, 12};
    public static final int[] SKULL_B_CELL = new int[]{0, 96, 12, 12};

    private Mat() {
    }

    public static int rgb(float var0, float var1, float var2) {
        return Math.round(Sd.clamp(var0, 0.0f, 1.0f) * 255.0f) << 16 | Math.round(Sd.clamp(var1, 0.0f, 1.0f) * 255.0f) << 8 | Math.round(Sd.clamp(var2, 0.0f, 1.0f) * 255.0f);
    }
}


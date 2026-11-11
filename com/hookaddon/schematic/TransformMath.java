/*
 * Decompiled with CFR 0.152.
 */
package com.hookaddon.schematic;

public final class TransformMath {
    private TransformMath() {
    }

    public static int[] transform(int x, int y, int z, Mirror mirror, Rotation rotation) {
        int[] nArray;
        boolean mirrored = mirror != Mirror.NONE;
        switch (mirror.ordinal()) {
            case 1: {
                z = -z;
                break;
            }
            case 2: {
                x = -x;
                break;
            }
            default: {
                mirrored = false;
            }
        }
        switch (rotation.ordinal()) {
            case 1: {
                int[] nArray2 = new int[3];
                nArray2[0] = -z;
                nArray2[1] = y;
                nArray = nArray2;
                nArray2[2] = x;
                break;
            }
            case 3: {
                int[] nArray3 = new int[3];
                nArray3[0] = z;
                nArray3[1] = y;
                nArray = nArray3;
                nArray3[2] = -x;
                break;
            }
            case 2: {
                int[] nArray4 = new int[3];
                nArray4[0] = -x;
                nArray4[1] = y;
                nArray = nArray4;
                nArray4[2] = -z;
                break;
            }
            default: {
                int[] nArray5 = new int[3];
                nArray5[0] = x;
                nArray5[1] = y;
                nArray = nArray5;
                nArray5[2] = z;
            }
        }
        return nArray;
    }

    public static boolean changesBounds(Mirror mirror, Rotation rotation) {
        return mirror != Mirror.NONE || rotation != Rotation.NONE;
    }

    public static int[] transformedBounds(int sizeX, int sizeY, int sizeZ, Mirror mirror, Rotation rotation) {
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
            return new int[]{0, 0, 0, 0, 0, 0};
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (int corner = 0; corner < 8; ++corner) {
            int x = (corner & 1) == 0 ? 0 : sizeX - 1;
            int y = (corner & 2) == 0 ? 0 : sizeY - 1;
            int z = (corner & 4) == 0 ? 0 : sizeZ - 1;
            int[] t = TransformMath.transform(x, y, z, mirror, rotation);
            minX = Math.min(minX, t[0]);
            minY = Math.min(minY, t[1]);
            minZ = Math.min(minZ, t[2]);
            maxX = Math.max(maxX, t[0]);
            maxY = Math.max(maxY, t[1]);
            maxZ = Math.max(maxZ, t[2]);
        }
        return new int[]{minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1};
    }

    public static int[] toWorldRelative(int x, int y, int z, int originX, int originY, int originZ) {
        return new int[]{x + originX, y + originY, z + originZ};
    }

    public static enum Mirror {
        NONE("None"),
        LEFT_RIGHT("Front to back"),
        FRONT_BACK("Left to right");

        public final String title;

        private Mirror(String title) {
            this.title = title;
        }
    }

    public static enum Rotation {
        NONE("None"),
        CLOCKWISE_90("90\u00b0 right"),
        CLOCKWISE_180("180\u00b0"),
        COUNTERCLOCKWISE_90("90\u00b0 left");

        public final String title;

        private Rotation(String title) {
            this.title = title;
        }
    }
}


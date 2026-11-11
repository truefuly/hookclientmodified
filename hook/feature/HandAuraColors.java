/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.feature.UiPalette;
import java.util.Random;

final class HandAuraColors {
    private static int MixM4EWLFr;
    private transient int tNA3Xza2UU;

    private HandAuraColors() {
        int var4 = 1077125420;
        var4 = 0x62932048 ^ var4;
        var4 = 580944228;
        this.tNA3Xza2UU = 0x233A5164 ^ MixM4EWLFr;
        var4 = 0x69900387 ^ var4;
        var4 = 0x66D500DE ^ var4;
    }

    static int argb() {
        int var6 = 1571153687;
        var6 = 0x3C5FA234 ^ var6;
        float var0 = 1.0f;
        int var2 = UiPalette.accent(var0);
        int var1 = -16777216;
        return var2 | var1;
    }

    static {
        int var3 = new Random(7385252709338376032L).nextInt();
        MixM4EWLFr = 0xC074C67E ^ var3;
    }
}


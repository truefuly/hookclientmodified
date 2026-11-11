/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1109
 *  net.minecraft.class_1113
 *  net.minecraft.class_310
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 */
package hook.utils;

import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;

public final class FlagAlarm {
    private static int remainingBeats;
    private static int ticksUntilNext;

    private FlagAlarm() {
    }

    public static void trigger(class_310 client) {
        FlagAlarm.play(client, 0);
        remainingBeats = 5;
        ticksUntilNext = 7;
    }

    public static void tick(class_310 client) {
        if (remainingBeats > 0 && --ticksUntilNext <= 0) {
            int beat = 5 - remainingBeats + 1;
            FlagAlarm.play(client, beat);
            --remainingBeats;
            ticksUntilNext = 7;
        }
    }

    private static void play(class_310 client, int beat) {
        float pitch = beat % 2 == 0 ? 0.72f : 0.92f;
        client.method_1483().method_4873((class_1113)class_1109.method_4757((class_3414)((class_3414)class_3417.field_14793.comp_349()), (float)pitch, (float)1.0f));
    }
}


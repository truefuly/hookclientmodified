/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 */
package hook.compat.meteorclient.pathing;

import hook.compat.meteorclient.pathing.IPathManager;
import net.minecraft.class_2338;

public final class PathManagers {
    private static final IPathManager NONE = new IPathManager(){

        @Override
        public String getName() {
            return "none";
        }

        @Override
        public void moveTo(class_2338 target) {
        }

        @Override
        public boolean isPathing() {
            return false;
        }

        @Override
        public void stop() {
        }
    };

    private PathManagers() {
    }

    public static IPathManager get() {
        return NONE;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package hook.module.ext;

import hook.module.Category;

public final class ModuleCategory {
    public static final ModuleCategory COMBAT = new ModuleCategory(Category.COMBAT);
    public static final ModuleCategory MISC = new ModuleCategory(Category.MISC);
    public static final ModuleCategory DONUT = new ModuleCategory(Category.DONUT);
    public static final ModuleCategory GLAZED = new ModuleCategory(Category.GLAZED);
    public static final ModuleCategory RENDER = new ModuleCategory(Category.RENDER);
    public static final ModuleCategory VISUALS = new ModuleCategory(Category.VISUALS);
    public static final ModuleCategory CLIENT = new ModuleCategory(Category.CLIENT);
    public static final ModuleCategory MOVEMENT = MISC;
    public static final ModuleCategory BASE_RAID = DONUT;
    private final Category hook;

    private ModuleCategory(Category var1) {
        this.hook = var1;
    }

    public Category hook() {
        return this.hook;
    }
}


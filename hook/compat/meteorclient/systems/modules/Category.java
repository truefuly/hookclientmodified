/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.systems.modules;

public class Category {
    public final String name;
    private final hook.module.Category delegate;

    public Category(String name) {
        this.name = name;
        this.delegate = Category.map(name);
    }

    public hook.module.Category hook() {
        return this.delegate;
    }

    private static hook.module.Category map(String name) {
        if (name == null) {
            return hook.module.Category.AUTOMATION;
        }
        switch (name.toLowerCase()) {
            case "combat": {
                return hook.module.Category.COMBAT;
            }
            case "render": {
                return hook.module.Category.RENDER;
            }
            case "misc": {
                return hook.module.Category.MISC;
            }
            case "macros": {
                return hook.module.Category.MACROS;
            }
        }
        return hook.module.Category.AUTOMATION;
    }
}


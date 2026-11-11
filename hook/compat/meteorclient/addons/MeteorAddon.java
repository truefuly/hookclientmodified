/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.addons;

public abstract class MeteorAddon {
    public void onRegisterCategories() {
    }

    public abstract void onInitialize();

    public String getPackage() {
        return "";
    }
}


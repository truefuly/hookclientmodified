/*
 * Decompiled with CFR 0.152.
 */
package hook.module;

import hook.utils.EncryptedString;

public enum Category {
    COMBAT(EncryptedString.of("Combat")),
    MACROS(EncryptedString.of("Macros")),
    MISC(EncryptedString.of("Utility")),
    DONUT(EncryptedString.of("Base Finder")),
    GLAZED(EncryptedString.of("Glazed")),
    RENDER(EncryptedString.of("Render")),
    VISUALS(EncryptedString.of("Visuals")),
    AUTOMATION(EncryptedString.of("Automation")),
    CLIENT(EncryptedString.of("Client"));

    public final CharSequence name;

    private Category(CharSequence nullxx) {
        this.name = nullxx;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package hook.module;

import hook.utils.EncryptedString;

public enum Category {
    COMBAT(EncryptedString.of("Combat")),
    RENDER(EncryptedString.of("Vision")),
    DONUT(EncryptedString.of("Base Hunt")),
    MISC(EncryptedString.of("Player")),
    AUTOMATION(EncryptedString.of("Automation")),
    MACROS(EncryptedString.of("Macros")),
    GLAZED(EncryptedString.of("Glazed")),
    VISUALS(EncryptedString.of("Cosmetics")),
    CLIENT(EncryptedString.of("Client"));

    public final CharSequence name;

    private Category(CharSequence nullxx) {
        this.name = nullxx;
    }
}


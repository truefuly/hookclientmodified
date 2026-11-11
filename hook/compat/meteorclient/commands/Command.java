/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  net.minecraft.class_2172
 *  net.minecraft.class_2561
 */
package hook.compat.meteorclient.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import hook.compat.meteorclient.commands.CommandChat;
import net.minecraft.class_2172;
import net.minecraft.class_2561;

public abstract class Command {
    public static final int SINGLE_SUCCESS = 1;
    public final String name;
    public final String description;
    public final String alias;

    protected Command(String name, String description, String alias) {
        this.name = name;
        this.description = description;
        this.alias = alias;
    }

    public abstract void build(LiteralArgumentBuilder<class_2172> var1);

    protected LiteralArgumentBuilder<class_2172> literal(String name) {
        return LiteralArgumentBuilder.literal((String)name);
    }

    protected <T> RequiredArgumentBuilder<class_2172, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument((String)name, type);
    }

    protected void info(String format, Object ... args) {
        this.chat("\u00a7f", format, args);
    }

    protected void warning(String format, Object ... args) {
        this.chat("\u00a7e", format, args);
    }

    protected void error(String format, Object ... args) {
        this.chat("\u00a7c", format, args);
    }

    private void chat(String color, String format, Object ... args) {
        String message;
        try {
            message = args.length == 0 ? format : String.format(format, args);
        }
        catch (RuntimeException e) {
            message = format;
        }
        CommandChat.send((class_2561)class_2561.method_43470((String)("\u00a77[\u00a7bHook\u00a77] " + color + message)));
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  net.minecraft.class_2172
 *  net.minecraft.class_2561
 */
package hook.compat.meteorclient.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import hook.compat.meteorclient.commands.Command;
import hook.compat.meteorclient.commands.CommandChat;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2172;
import net.minecraft.class_2561;

public final class Commands {
    private static final List<Command> COMMANDS = new ArrayList<Command>();

    private Commands() {
    }

    public static void add(Command command) {
        COMMANDS.add(command);
    }

    public static boolean dispatch(String message) {
        if (message == null || !message.startsWith(".")) {
            return false;
        }
        String body = message.substring(1).trim();
        if (body.isEmpty()) {
            return false;
        }
        String[] parts = body.split("\\s+", 2);
        Command command = Commands.find(parts[0]);
        if (command == null) {
            return false;
        }
        try {
            LiteralArgumentBuilder root = LiteralArgumentBuilder.literal((String)command.name);
            command.build((LiteralArgumentBuilder<class_2172>)root);
            CommandDispatcher dispatcher = new CommandDispatcher();
            dispatcher.register(root);
            String normalized = command.name + (String)(parts.length > 1 ? " " + parts[1] : "");
            dispatcher.execute(normalized, (Object)Commands.noopSource());
        }
        catch (CommandSyntaxException e) {
            CommandChat.send((class_2561)class_2561.method_43470((String)("\u00a77[\u00a7bHook\u00a77] \u00a7c" + e.getRawMessage().getString())));
        }
        catch (Throwable t) {
            CommandChat.send((class_2561)class_2561.method_43470((String)("\u00a77[\u00a7bHook\u00a77] \u00a7cCommand failed: " + String.valueOf(t))));
            t.printStackTrace(System.err);
        }
        return true;
    }

    private static Command find(String token) {
        for (Command command : COMMANDS) {
            if (command.name.equalsIgnoreCase(token)) {
                return command;
            }
            if (command.alias == null || !command.alias.equalsIgnoreCase(token)) continue;
            return command;
        }
        return null;
    }

    private static class_2172 noopSource() {
        return (class_2172)Proxy.newProxyInstance(Commands.class.getClassLoader(), new Class[]{class_2172.class}, (proxy, method, args) -> {
            Class<?> type = method.getReturnType();
            if (type == Boolean.TYPE) {
                return false;
            }
            if (type == Integer.TYPE) {
                return 0;
            }
            if (type == Long.TYPE) {
                return 0L;
            }
            if (type == Float.TYPE) {
                return Float.valueOf(0.0f);
            }
            if (type == Double.TYPE) {
                return 0.0;
            }
            return null;
        });
    }
}


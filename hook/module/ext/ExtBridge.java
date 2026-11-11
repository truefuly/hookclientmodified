/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_2596
 *  net.minecraft.class_2680
 *  net.minecraft.class_4604
 */
package hook.module.ext;

import hook.module.ext.ClientModule;
import hook.module.ext.EventHandler;
import hook.module.ext.Stage;
import hook.module.ext.event.BlockUpdateEvent;
import hook.module.ext.event.PacketReceiveNettyEvent;
import hook.module.ext.event.PacketSendEvent;
import hook.module.ext.event.Render3DEvent;
import hook.module.ext.event.TickEvent;
import hook.module.ext.setting.Setting;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import net.minecraft.class_2338;
import net.minecraft.class_2596;
import net.minecraft.class_2680;
import net.minecraft.class_4604;

public final class ExtBridge {
    private static final List<ClientModule> MODULES = new CopyOnWriteArrayList<ClientModule>();
    private static final Map<Class<?>, List<Handler>> HANDLERS = new ConcurrentHashMap();
    private static final ConcurrentLinkedQueue<Runnable> LATER = new ConcurrentLinkedQueue();
    private static final TickEvent TICK = new TickEvent();

    private ExtBridge() {
    }

    static void track(ClientModule var0) {
        MODULES.add(var0);
        for (Class<?> var1 = var0.getClass(); var1 != null && var1 != ClientModule.class; var1 = var1.getSuperclass()) {
            for (Method var5 : var1.getDeclaredMethods()) {
                EventHandler var6 = var5.getAnnotation(EventHandler.class);
                if (var6 == null || var5.getParameterCount() != 1) continue;
                var5.setAccessible(true);
                Class<?> var7 = var5.getParameterTypes()[0];
                ExtBridge.add(var7, new Handler(var0, var6.stage(), var2 -> {
                    try {
                        var5.invoke((Object)var0, var2);
                    }
                    catch (ReflectiveOperationException var4) {
                        throw new RuntimeException(var4.getCause() != null ? var4.getCause() : var4);
                    }
                }, false));
            }
        }
    }

    static <E> void subscribe(ClientModule var0, Class<E> var1, Stage var2, Consumer<E> var3, boolean var4) {
        ExtBridge.add(var1, new Handler(var0, var2, var1x -> var3.accept(var1.cast(var1x)), var4));
    }

    static void dropOwned(ClientModule var0) {
        for (List<Handler> var2 : HANDLERS.values()) {
            var2.removeIf(var1 -> ((Handler)var1).owned && ((Handler)var1).owner == var0);
        }
    }

    private static void add(Class<?> var0, Handler var1) {
        HANDLERS.computeIfAbsent(var0, var0x -> new CopyOnWriteArrayList()).add(var1);
    }

    static void later(Runnable var0) {
        LATER.add(var0);
    }

    public static boolean wants(Class<?> var0) {
        List<Handler> var1 = HANDLERS.get(var0);
        if (var1 != null) {
            Iterator<Handler> iterator = var1.iterator();
            while (iterator.hasNext()) {
                Handler var3Raw;
                Handler var3 = var3Raw = iterator.next();
                if (!var3.owner.isEnabled()) continue;
                return true;
            }
        }
        return false;
    }

    private static void post(Object var0, Stage var1) {
        List<Handler> var2 = HANDLERS.get(var0.getClass());
        if (var2 != null) {
            Iterator<Handler> iterator = var2.iterator();
            while (iterator.hasNext()) {
                Handler var4Raw;
                Handler var4 = var4Raw = iterator.next();
                if (var4.stage != var1 || !var4.owner.isEnabled()) continue;
                try {
                    var4.fn.accept(var0);
                }
                catch (Throwable var6) {
                    System.err.println("Hook: " + var4.owner.name() + " failed handling " + var0.getClass().getSimpleName() + ": " + String.valueOf(var6));
                }
            }
        }
    }

    public static void tick(Stage var0) {
        if (var0 == Stage.PRE) {
            Runnable var1;
            while ((var1 = LATER.poll()) != null) {
                try {
                    var1.run();
                }
                catch (Throwable var6) {
                    System.err.println("Hook: deferred task failed: " + String.valueOf(var6));
                }
            }
            for (ClientModule var3 : MODULES) {
                for (Setting var5 : var3.extSettings()) {
                    var5.poll();
                }
            }
        }
        ExtBridge.post(TICK, var0);
    }

    public static void render3D(float var0, class_4604 var1) {
        if (ExtBridge.wants(Render3DEvent.class)) {
            ExtBridge.post(new Render3DEvent(var0, var1), Stage.PRE);
        }
    }

    public static boolean packetReceive(class_2596<?> var0) {
        if (!ExtBridge.wants(PacketReceiveNettyEvent.class)) {
            return false;
        }
        PacketReceiveNettyEvent var1 = new PacketReceiveNettyEvent(var0);
        ExtBridge.post(var1, Stage.PRE);
        return var1.isCancelled();
    }

    public static boolean packetSend(class_2596<?> var0) {
        if (!ExtBridge.wants(PacketSendEvent.class)) {
            return false;
        }
        PacketSendEvent var1 = new PacketSendEvent(var0);
        ExtBridge.post(var1, Stage.PRE);
        return var1.isCancelled();
    }

    public static void blockUpdate(class_2338 var0, class_2680 var1, class_2680 var2) {
        if (ExtBridge.wants(BlockUpdateEvent.class)) {
            ExtBridge.post(new BlockUpdateEvent(var0, var1, var2), Stage.PRE);
        }
    }

    private record Handler(ClientModule owner, Stage stage, Consumer<Object> fn, boolean owned) {
    }
}


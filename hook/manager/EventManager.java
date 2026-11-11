/*
 * Decompiled with CFR 0.152.
 */
package hook.manager;

import hook.Hook;
import hook.event.CancellableEvent;
import hook.event.Event;
import hook.event.EventListener;
import hook.event.Listener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EventManager {
    private static final Listener[] NONE = new Listener[0];
    private final Map<Class<?>, Listener[]> EVENTS = new ConcurrentHashMap();

    public void register(Object var1) {
        // Walk the class hierarchy: listeners declared in a base class (e.g. Block ESP for Tunnel ESP)
        // must fire for subclasses too. Overridden methods are only registered once (most specific).
        java.util.Set<String> seen = new java.util.HashSet<String>();
        for (Class<?> type = var1.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Method var6 : type.getDeclaredMethods()) {
                if (!var6.isAnnotationPresent(EventListener.class) || var6.getParameterCount() != 1 || !Event.class.isAssignableFrom(var6.getParameterTypes()[0])) continue;
                if (!seen.add(var6.getName() + "(" + var6.getParameterTypes()[0].getName() + ")")) continue;
                var6.setAccessible(true);
                this.addListener(var1, var6, var6.getAnnotation(EventListener.class));
            }
        }
    }

    private synchronized void addListener(Object var1, Method var2, EventListener var3) {
        Class<?> var4 = var2.getParameterTypes()[0];
        ArrayList<Listener> var5 = new ArrayList<Listener>(Arrays.asList(this.EVENTS.getOrDefault(var4, NONE)));
        var5.add(new Listener(var1, var2, var3.priority()));
        var5.sort(Comparator.comparingInt(var0 -> var0.getPriority().getValue()));
        this.EVENTS.put(var4, var5.toArray(NONE));
    }

    public synchronized void unregister(Object var1) {
        for (Map.Entry<Class<?>, Listener[]> var3 : this.EVENTS.entrySet()) {
            Listener[] var4 = (Listener[])Arrays.stream(var3.getValue()).filter(var1x -> var1x.getInstance() != var1).toArray(Listener[]::new);
            if (var4.length == var3.getValue().length) continue;
            var3.setValue(var4);
        }
    }

    public synchronized void clear() {
        this.EVENTS.clear();
    }

    public void a(Event var1) {
        Listener[] var2 = this.EVENTS.get(var1.getClass());
        if (var2 != null) {
            for (Listener var6 : var2) {
                try {
                    if (!var6.isActive() || var1.isCancelled() && !(var1 instanceof CancellableEvent)) continue;
                    var6.invoke(var1);
                }
                catch (Throwable var8) {
                    System.err.println("Error dispatching event " + var1.getClass().getSimpleName() + " to " + (var6.getInstance() != null ? var6.getInstance().getClass().getSimpleName() : "unknown"));
                    var8.printStackTrace(System.err);
                }
            }
        }
    }

    private boolean hasActive(Class<? extends Event> var1) {
        Listener[] var2 = this.EVENTS.get(var1);
        if (var2 != null) {
            for (Listener var6 : var2) {
                if (!var6.isActive()) continue;
                return true;
            }
        }
        return false;
    }

    public static void b(Event var0) {
        if (Hook.INSTANCE != null && Hook.INSTANCE.getEventBus() != null) {
            Hook.INSTANCE.getEventBus().a(var0);
        }
    }

    public static boolean hasActiveListener(Class<? extends Event> var0) {
        return Hook.INSTANCE != null && Hook.INSTANCE.getEventBus() != null && Hook.INSTANCE.getEventBus().hasActive(var0);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.orbit;

import hook.compat.orbit.EventHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {
    private static final EventBus INSTANCE = new EventBus();
    private final Map<Class<?>, List<Entry>> handlers = new ConcurrentHashMap();

    public static EventBus get() {
        return INSTANCE;
    }

    public void subscribe(Object subscriber) {
        for (Method method : subscriber.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventHandler.class) || method.getParameterCount() != 1) continue;
            method.setAccessible(true);
            this.handlers.computeIfAbsent(method.getParameterTypes()[0], key -> new CopyOnWriteArrayList()).add(new Entry(subscriber, method));
        }
    }

    public void unsubscribe(Object subscriber) {
        for (List<Entry> entries : this.handlers.values()) {
            entries.removeIf(entry -> entry.instance == subscriber);
        }
    }

    public void post(Object event) {
        List<Entry> entries = this.handlers.get(event.getClass());
        if (entries == null) {
            return;
        }
        for (Entry entry : entries) {
            try {
                entry.method.invoke(entry.instance, event);
            }
            catch (Throwable t) {
                System.err.println("Hook: handler failed for " + event.getClass().getSimpleName());
                t.printStackTrace(System.err);
            }
        }
    }

    private record Entry(Object instance, Method method) {
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package hook.event;

import hook.event.Event;
import hook.event.Priority;
import hook.module.Module;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class Listener {
    private static final MethodType INVOKER_TYPE = MethodType.methodType(Void.TYPE, Object.class, Event.class);
    private final Object instance;
    private final Module module;
    private final Priority priority;
    private final Invoker invoker;

    public Listener(Object var1, Method var2, Priority var3) {
        Module var4;
        this.instance = var1;
        this.module = var1 instanceof Module ? (var4 = (Module)var1) : null;
        this.priority = var3;
        this.invoker = Listener.createInvoker(var2);
    }

    public void invoke(Event var1) throws Throwable {
        this.invoker.invoke(this.instance, var1);
    }

    public boolean isActive() {
        return this.module == null || this.module.isEnabled();
    }

    public Object getInstance() {
        return this.instance;
    }

    public Priority getPriority() {
        return this.priority;
    }

    private static Invoker createInvoker(Method var0) {
        try {
            Class<?> var7 = var0.getDeclaringClass();
            MethodHandles.Lookup var2 = MethodHandles.privateLookupIn(var7, MethodHandles.lookup());
            MethodHandle var3 = var2.unreflect(var0);
            CallSite var4 = LambdaMetafactory.metafactory(var2, "invoke", MethodType.methodType(Invoker.class), INVOKER_TYPE, var3, MethodType.methodType(Void.TYPE, var7, var0.getParameterTypes()[0]));
            return (Invoker)var4.getTarget().invoke();
        }
        catch (Throwable var6) {
            try {
                MethodHandle var1 = MethodHandles.privateLookupIn(var0.getDeclaringClass(), MethodHandles.lookup()).unreflect(var0).asType(INVOKER_TYPE);
                return (var1x, var2x) -> var1.invokeExact(var1x, var2x);
            }
            catch (Throwable var5) {
                var0.setAccessible(true);
                return (var1x, var2x) -> var0.invoke(var1x, var2x);
            }
        }
    }

    @FunctionalInterface
    public static interface Invoker {
        public void invoke(Object var1, Event var2) throws Throwable;
    }
}


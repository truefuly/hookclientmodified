/*
 * Decompiled with CFR 0.152.
 */
package hook.event;

import hook.event.Priority;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface EventListener {
    public Priority priority() default Priority.NORMAL;
}


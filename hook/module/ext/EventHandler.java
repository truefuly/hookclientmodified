/*
 * Decompiled with CFR 0.152.
 */
package hook.module.ext;

import hook.module.ext.Stage;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD})
public @interface EventHandler {
    public Stage stage() default Stage.PRE;
}


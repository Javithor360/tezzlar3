package com.panita.tezzlar3.core.commands.identifiers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for basic Command with its specifications
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandSpec {
    String name();

    String description() default "";

    String syntax() default "";

    String permission() default "";

    String[] aliases() default {};

    boolean playerOnly() default false;
}


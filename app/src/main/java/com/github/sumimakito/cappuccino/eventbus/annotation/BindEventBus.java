/*
 * Copyright 2013-2017 Sumi Makito
 * Copyright 2016-2017 Bitcat Interactive Lab.
 */

package com.github.sumimakito.cappuccino.eventbus.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BindEventBus {
    String value() default "Nothing";
}

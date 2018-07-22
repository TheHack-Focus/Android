
/*
 * Copyright 2013-2017 Sumi Makito
 * Copyright 2016-2017 Bitcat Interactive Lab.
 * All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Sumi Makito <sumimakito@hotmail.com>, June 2016
 */

package com.github.sumimakito.cappuccino.eventbus;

import com.github.sumimakito.cappuccino.CappuccinoConfig;
import com.github.sumimakito.cappuccino.eventbus.annotation.BindEventBus;
import com.github.sumimakito.cappuccino.logger.Logger;
import com.github.sumimakito.cappuccino.logger.LoggerTag;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

@LoggerTag(!CappuccinoConfig.DEBUG_MODE?"":"EventBus")
public class EventBus {
    private static EventBus instance = null;
    private static HashMap<Class, Object> registeredInstances;
    private static HashMap<Class, Method> registeredMethods;

    private EventBus() {
        registeredInstances = new HashMap<>();
        registeredMethods = new HashMap<>();
        Logger.i(EventBus.class, "Instance initialized.");
    }

    public static void init() {
        if (instance == null)
            instance = new EventBus();
    }

    public static void emit(Class receiver, Event event) {
        if (instance == null) throw new IllegalStateException("Forgot to call init()?");
        instance._emit(receiver, event);
    }

    public static void broadcast(Event event) {
        if (instance == null) throw new IllegalStateException("Forgot to call init()?");
        instance._broadcast(event);
    }

    public static void bind(Object target) {
        if (instance == null) throw new IllegalStateException("Forgot to call init()?");
        instance._bind(target);
    }

    public static void unbind(Object target) {
        if (instance == null) throw new IllegalStateException("Forgot to call init()?");
        instance._unbind(target);
    }

    private void _emit(Class receiver, Event event) {
        if (registeredInstances.containsKey(receiver)) {
            Logger.i(EventBus.class, "Will invoke the method with @BindEventBus annotation in target class.");
            invoke(registeredInstances.get(receiver), receiver, event);
        } else {
            Logger.w(EventBus.class, "Target class is not registered.");
        }
    }

    private void _broadcast(Event event) {
        for (Class targetCls : registeredInstances.keySet()) {
            invoke(registeredInstances.get(targetCls), targetCls, event);
        }
    }

    private void _bind(Object target) {
        synchronized (this) {
            if (target instanceof Class) {
                Logger.e(EventBus.class, "EventBus can only bind an instance!");
                return;
            }
            Class target_class = target.getClass();
            Logger.i(EventBus.class, "Will bind class " + target_class.getName());

            Method onEventMethod = getTargetAnnotatedMethod(target_class.getDeclaredMethods());
            if (onEventMethod == null) return;
            if (registeredInstances.containsKey(target_class)) {
                Logger.w(EventBus.class, "Target exists. Duplicated bind target.");
            } else {
                registeredInstances.put(target_class, target);
                registeredMethods.put(target_class, onEventMethod);
            }
        }
    }

    private void _unbind(Object target) {
        synchronized (this) {
            if (registeredInstances.containsKey(target.getClass())) {
                registeredInstances.remove(target.getClass());
            }
            if (registeredMethods.containsKey(target.getClass())) {
                registeredMethods.remove(target.getClass());
            }
        }
    }

    private void invoke(Object receiverInstance, Class receiverClass, Event event) {
        Method onEventMethod = null;
        if (registeredMethods.containsKey(receiverClass)) {
            onEventMethod = registeredMethods.get(receiverClass);
        } else {
            onEventMethod = getTargetAnnotatedMethod(receiverClass.getDeclaredMethods());
        }
        if (onEventMethod == null) {
            Logger.e(EventBus.class, "onEventMethod not found.");
            return;
        }
        try {
            onEventMethod.invoke(receiverInstance, event);
        } catch (Exception e) {
            Logger.e(EventBus.class, "Exception thrown.");
        }
    }

    private static Method getTargetAnnotatedMethod(Method[] methodArray) {
        Method inSightMethod = null;
        try {
            for (Method method : methodArray) {
                // Logger.i(EventBus.class, "INFO: -- Method/" + method.getName() + " -->" + (method.isAnnotationPresent(BindEventBus.class) ? "HIT" : "MISS"));
                if (method.isAnnotationPresent(BindEventBus.class)) {
                    inSightMethod = method;
                    break;
                }
            }
            if (inSightMethod == null) {
                Logger.e(EventBus.class, "No compatible annotated method found.");
                return null;
            }
            Logger.i(EventBus.class, "Annotated method insight: " + inSightMethod.getName());

            Class<?>[] parameterTypes = inSightMethod.getParameterTypes();
            if (parameterTypes.length != 1) {
                Logger.e(EventBus.class, "Annotated method found, but has incompatible parameters. " + Arrays.toString(parameterTypes));
                return null;
            }
            final Class<?> param1 = parameterTypes[0];
            if (param1 != Event.class) {
                Logger.e(EventBus.class, "Annotated method found, but has incompatible parameters. " + Arrays.toString(parameterTypes));
                return null;
            }
            return inSightMethod;

        } catch (Exception e) {
            // Logger.e(EventBus.class, "ERROR: Exception thrown.");
            e.printStackTrace();
            return null;
        }
    }
}

/*
 *  Copyright 2013-2017 Sumi Makito
 *  Copyright 2016-2017 Bitcat Interactive Lab.
 *
 *  All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Sumi Makito <sumimakito@hotmail.com>, June 2016
 */

package com.github.sumimakito.cappuccino.logger;

import android.util.Log;

import com.github.sumimakito.cappuccino.CappuccinoConfig;

public class Logger {

    private enum Level {V, I, D, W, E}

    public static void v(Class sender, String message) {
        invoke(Level.V, sender, message);
    }

    public static void i(Class sender, String message) {
        invoke(Level.I, sender, message);
    }

    public static void d(Class sender, String message) {
        invoke(Level.D, sender, message);
    }

    public static void w(Class sender, String message) {
        invoke(Level.W, sender, message);
    }

    public static void e(Class sender, String message) {
        invoke(Level.E, sender, message);
    }

    public static void v(Object senderInstance, String message) {
        invoke(Level.V, senderInstance, message);
    }

    public static void i(Object senderInstance, String message) {
        invoke(Level.I, senderInstance, message);
    }

    public static void d(Object senderInstance, String message) {
        invoke(Level.D, senderInstance, message);
    }

    public static void w(Object senderInstance, String message) {
        invoke(Level.W, senderInstance, message);
    }

    public static void e(Object senderInstance, String message) {
        invoke(Level.E, senderInstance, message);
    }

    public static void v(Class sender, String format, Object... args) {
        invoke(Level.V, sender, String.format(format, args));
    }

    public static void i(Class sender, String format, Object... args) {
        invoke(Level.I, sender, String.format(format, args));
    }

    public static void d(Class sender, String format, Object... args) {
        invoke(Level.D, sender, String.format(format, args));
    }

    public static void w(Class sender, String format, Object... args) {
        invoke(Level.W, sender, String.format(format, args));
    }

    public static void e(Class sender, String format, Object... args) {
        invoke(Level.E, sender, String.format(format, args));
    }

    public static void v(Object senderInstance, String format, Object... args) {
        invoke(Level.V, senderInstance, String.format(format, args));
    }

    public static void i(Object senderInstance, String format, Object... args) {
        invoke(Level.I, senderInstance, String.format(format, args));
    }

    public static void d(Object senderInstance, String format, Object... args) {
        invoke(Level.D, senderInstance, String.format(format, args));
    }

    public static void w(Object senderInstance, String format, Object... args) {
        invoke(Level.W, senderInstance, String.format(format, args));
    }

    public static void e(Object senderInstance, String format, Object... args) {
        invoke(Level.E, senderInstance, String.format(format, args));
    }


    public static void printStackTrace(Throwable throwable) {
        if (CappuccinoConfig.DEBUG_MODE) throwable.printStackTrace();
    }

    private static void invoke(Level level, Object tag, String message) {
        if (!CappuccinoConfig.DEBUG_MODE) return;
        try {
            if (tag instanceof String) {
                log(level, (String) tag, message);
            } else if (tag instanceof Class) {
                Class rt_class = (Class) tag;
                String s_tag = "";
                boolean flagLoggerTag = rt_class.isAnnotationPresent(LoggerTag.class);
                boolean flagLoggerMute = rt_class.isAnnotationPresent(LoggerMute.class);
                if (flagLoggerMute) {
                    // Mute
                    return;
                }
                if (flagLoggerTag) {
                    LoggerTag loggerTag = (LoggerTag) rt_class.getAnnotation(LoggerTag.class);
                    s_tag = loggerTag.value();
                } else {
                    s_tag = rt_class.getSimpleName();
                }
                log(level, s_tag, message);
            } else {
                Class rt_class = (Class) tag.getClass();
                String s_tag = "";
                boolean flagLoggerTag = rt_class.isAnnotationPresent(LoggerTag.class);
                boolean flagLoggerMute = rt_class.isAnnotationPresent(LoggerMute.class);
                if (flagLoggerMute) {
                    // Mute
                    return;
                }
                if (flagLoggerTag) {
                    LoggerTag loggerTag = (LoggerTag) rt_class.getAnnotation(LoggerTag.class);
                    s_tag = loggerTag.value();
                } else {
                    s_tag = rt_class.getSimpleName();
                }
                log(level, s_tag, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void log(Level level, String tag, String message) {
        if (!CappuccinoConfig.DEBUG_MODE) return;
        switch (level) {
            case V:
                Log.v(tag, message);
                break;
            case I:
                Log.i(tag, message);
                break;
            case D:
                Log.d(tag, message);
                break;
            case W:
                Log.w(tag, message);
                break;
            case E:
                Log.e(tag, message);
                break;
        }
    }
}

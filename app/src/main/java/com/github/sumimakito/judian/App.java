package com.github.sumimakito.judian;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {
    public static String username;
    public static List<Card> cards = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}

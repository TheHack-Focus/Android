package com.github.sumimakito.cappuccino.util;

import java.net.URL;

public class StringHelper {
    public static boolean isValidURL(String url) {
        try {
            URL url_ = new URL(url);
            url_.toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

package com.github.sumimakito.cappuccino.util;

import android.content.Context;

public class MetricsHelper {
    public static int dpToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}

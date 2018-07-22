package com.github.sumimakito.cappuccino.widget;

import android.content.Context;
import android.view.View;
import android.widget.ScrollView;

public class SmartScrollView extends ScrollView {
    public SmartScrollView(Context context) {
        super(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        super.measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec);
    }
}

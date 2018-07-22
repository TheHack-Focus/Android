package com.github.sumimakito.cappuccino.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class ItcptFrameLayout extends FrameLayout {
    private boolean interceptTouchEvent = false;

    public ItcptFrameLayout(Context context) {
        super(context);
    }

    public ItcptFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ItcptFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ItcptFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setInterceptTouchEvent(boolean interceptTouchEvent) {
        this.interceptTouchEvent = interceptTouchEvent;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return interceptTouchEvent || super.onInterceptTouchEvent(ev);
    }
}

package com.lwh912.easycoordinatorlayout.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class NoScrollViewpager extends ViewPager {
    private boolean isNoScroll = false;

    public NoScrollViewpager(@NonNull Context context) {
        super(context);
    }

    public NoScrollViewpager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(isNoScroll){
            return false;
        }else{
            return super.onInterceptTouchEvent(event);
        }
    }

    public void setNoScroll(boolean noScroll) {
        isNoScroll = noScroll;
    }
}

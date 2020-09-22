package com.lwh912.easycoordinatorlayout.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.lwh912.easycoordinatorlayout.behavior.MyScrollViewBehavior;


public class XmCoordinatorLayout extends CoordinatorLayout {

    public XmCoordinatorLayout(@NonNull Context context) {
        super(context);
    }

    public XmCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public XmCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View view = getChildAt(i);
            if (view.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) view.getLayoutParams();

            final Behavior viewBehavior = lp.getBehavior();
            //把fill传到Behavior
            if (viewBehavior instanceof MyScrollViewBehavior) {
                viewBehavior.onNestedFling(this, view, target, velocityX, velocityY, consumed);
                return true;
            }
        }

        return super.onNestedFling(target, velocityX, velocityY, consumed);
    }
}

package com.lwh912.easycoordinatorlayout.behavior;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;


public class MyScrollViewBehavior extends AppBarLayout.ScrollingViewBehavior {

    public MyScrollViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onNestedFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, float velocityX, float velocityY, boolean consumed) {
        if (target instanceof AppBarLayout) {
            if (child instanceof ViewPager) {
                ViewPager viewPager = (ViewPager) child;
                if (viewPager == null || viewPager.getAdapter() == null || viewPager.getAdapter().getCount() == 0)  return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
                Fragment fragment = ((FragmentStatePagerAdapter) viewPager.getAdapter()).getItem(viewPager.getCurrentItem());
                if (fragment != null) {
                    ViewGroup viewGroup = (ViewGroup) fragment.getView();
//                    RecyclerView recyclerView = viewGroup.findViewById(R.id.recyclerView);
                    RecyclerView recyclerView = viewGroup.findViewWithTag("fling");
                    if (recyclerView.isAttachedToWindow()){
                        if (recyclerView.getMeasuredHeight() == viewPager.getMeasuredHeight()){
                            recyclerView.fling(0, (int) velocityY);
                        }
                    }
                    return true;
                }
            }
        }
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);

    }
}

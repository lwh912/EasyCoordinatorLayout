package com.lwh912.easycoordinatorlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.lwh912.easycoordinatorlayout.adapter.ViewpagerAdapter;
import com.lwh912.easycoordinatorlayout.behavior.AppBarLayoutBehavior;
import com.lwh912.easycoordinatorlayout.listener.OffsetUpdateListener;
import com.lwh912.easycoordinatorlayout.view.NoScrollViewpager;

import java.util.ArrayList;


public class EasyTagCorrdinatorLayout extends RelativeLayout {

    AppBarLayout app_bar;
    //用来垫高Appbar的最小高度，当appbar的content没有内容，那么就有holder来垫高，最少= toolbar + tagbar 高度
    View holderView;
    //appbar内容的容器(LinearLayout)
    LinearLayout ll_appbar_content;
    //viewPager
    NoScrollViewpager viewpager;
    //Fragment数组
    ArrayList<Fragment> fragmentList = new ArrayList<>();
    //toolbar 需要注意：xml加上android:tag="easy_corrdinator_toolbar"
    View toolbar;
    //tagbar 需要注意：xml加上android:tag="easy_corrdinator_tagbar"
    View tagbar;
    boolean isOnMeasureDone;
    //toolbar高度，tagbar高度
    int toolbarHeight,tagbarHeight;
    //监听yOffset监听器
    OffsetUpdateListener offsetUpdateListener;

    public EasyTagCorrdinatorLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public EasyTagCorrdinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public EasyTagCorrdinatorLayout(@NonNull Context context, @Nullable  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureHeight();
    }

    public EasyTagCorrdinatorLayout setViewpagerNoScroll(boolean isNoScroll){
        viewpager.setNoScroll(isNoScroll);
        return this;
    }

    private void measureHeight() {
        if (isOnMeasureDone){
            return;
        }
        toolbar = findViewWithTag("easy_corrdinator_toolbar");
        tagbar = findViewWithTag("easy_corrdinator_tagbar");
        isOnMeasureDone = true;
        if (toolbar != null){
            toolbarHeight = toolbar.getMeasuredHeight();
        }
        if (tagbar != null){
            tagbarHeight = tagbar.getMeasuredHeight();
        }
        ll_appbar_content.setPadding(0,0,0,tagbarHeight);

        holderView.getLayoutParams().height = toolbarHeight + tagbarHeight;
        holderView.requestLayout();
        app_bar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                setChangeView(i);
            }
        });
        ((AppBarLayoutBehavior) ((CoordinatorLayout.LayoutParams) app_bar.getLayoutParams()).getBehavior()).setCallFlingListener(new AppBarLayoutBehavior.CallFlingListener() {
            @Override
            public void onOffsetChange(int offset) {
                setChangeView(offset);
            }
        });
    }

    private void setChangeView(int offset) {
        if (offsetUpdateListener != null){
            offsetUpdateListener.onOffsetSetChange(offset);
        }
        float y = app_bar.getMeasuredHeight() + offset - tagbarHeight;
        if (y < toolbarHeight){
            y = toolbarHeight;
        }
        if (tagbar != null){
            tagbar.setY(y);
        }
    }

    private void initView() {
        //如果不是FragmentActivity 抛出异常
        if (!(getContext() instanceof FragmentActivity)) {
            throw new RuntimeException("must use FragmentActivity");
        }
        LayoutInflater.from(getContext()).inflate(R.layout.view_easy_tag_corrdinatorlayout,this);
        app_bar = findViewById(R.id.app_bar);
        holderView = findViewById(R.id.holderView);
        ll_appbar_content = findViewById(R.id.ll_appbar_content);
        viewpager = findViewById(R.id.viewpager);
        viewpager.setAdapter(new ViewpagerAdapter(((FragmentActivity)getContext()).getSupportFragmentManager() , fragmentList));
    }

    public EasyTagCorrdinatorLayout addOnPageChangeListener(ViewPager.OnPageChangeListener listener){
        viewpager.addOnPageChangeListener(listener);
        return this;
    }

    public EasyTagCorrdinatorLayout addAppBarContent(View appBarContent) {
        ll_appbar_content.removeAllViews();
        ll_appbar_content.addView(appBarContent);
        return this;
    }

    public View getAppBarContent(){
        if (ll_appbar_content.getChildCount() == 0) return null;
        return ll_appbar_content.getChildAt(0);
    }

    public EasyTagCorrdinatorLayout setViewpagerData(ArrayList<Fragment> list) {
        fragmentList.clear();
        fragmentList.addAll(list);
        viewpager.getAdapter().notifyDataSetChanged();
        return this;
    }

    public EasyTagCorrdinatorLayout setOffsetUpdateListener(OffsetUpdateListener offsetUpdateListener) {
        this.offsetUpdateListener = offsetUpdateListener;
        return this;
    }
}

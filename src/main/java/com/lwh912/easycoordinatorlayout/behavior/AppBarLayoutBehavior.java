package com.lwh912.easycoordinatorlayout.behavior;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.math.MathUtils;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import java.lang.reflect.Field;


@SuppressLint("LongLogTag")
public class AppBarLayoutBehavior extends AppBarLayout.Behavior {

    private final String TAG = "CustomAppbarLayoutBehavior";
    private VelocityTracker velocityTracker;
    private static final int TYPE_FLING = 1;

    private boolean isFlinging;
    private boolean shouldBlockNestedScroll;
    FlingRunnable flingRunnable;
    private OverScroller scroller;
    CallFlingListener callFlingListener;
    boolean allowFling;

    public interface CallFlingListener {
        void onOffsetChange(int offset);
    }

    public void setCallFlingListener(CallFlingListener callFlingListener) {
        this.callFlingListener = callFlingListener;
    }

    public AppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        velocityTracker = VelocityTracker.obtain();
        if (this.scroller == null) {
            this.scroller = new OverScroller(context);
        }
    }


    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
        velocityTracker.addMovement(ev);
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            allowFling = false;
            return true;
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            allowFling = true;
            velocityTracker.computeCurrentVelocity(1000);
            float yvel = velocityTracker.getYVelocity() * 1.2f;
            fling2(parent, child, Integer.MIN_VALUE, 0, yvel);
            return true;
        } else {
            return super.onTouchEvent(parent, child, ev);
        }
    }

    final void fling2(CoordinatorLayout coordinatorLayout, AppBarLayout layout, int minOffset, int maxOffset, float velocityY) {
        if (this.flingRunnable != null) {
            layout.removeCallbacks(this.flingRunnable);
            this.flingRunnable = null;
        }


        this.scroller.fling(0, this.getTopAndBottomOffset(), 0, (int) velocityY, 0, 0, minOffset, maxOffset);
        if (this.scroller.computeScrollOffset()) {
            this.flingRunnable = new FlingRunnable(coordinatorLayout, layout);
            ViewCompat.postOnAnimation(layout, this.flingRunnable);
            return;
        } else {
            stopAppbarLayoutFling(layout);
            return;
        }
    }

    private class FlingRunnable implements Runnable {
        private final CoordinatorLayout parent;
        private final AppBarLayout layout;

        FlingRunnable(CoordinatorLayout parent, AppBarLayout layout) {
            this.parent = parent;
            this.layout = layout;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            if (layout != null && scroller != null) {
                if (scroller.computeScrollOffset()) {
                    if (allowFling) {
                        scroll();
                    } else {
                        stopAppbarLayoutFling(layout);
                    }
                } else {
                    stopAppbarLayoutFling(layout);
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void scroll() {
//             如果AppBarLayout滑到阈值，此时需要将fling事件传递下去
            if (scroller.getCurrY() <= -getScrollRangeForDragFlingV2(layout) && scroller.getStartX() > scroller.getFinalY()) {
                setHeaderTopBottomOffsetV2(parent, layout, -getScrollRangeForDragFlingV2(layout));
//                if (parent.startNestedScroll(View.SCROLL_AXIS_VERTICAL)) {
                layout.getParent().onNestedFling(layout,scroller.getCurrVelocity(), scroller.getCurrVelocity(), false);
//                }
                scroller.forceFinished(true);
                if (callFlingListener != null) {
//                    callFlingListener.onCallFling(scroller.getCurrVelocity());
                }
                stopAppbarLayoutFling(layout);

//                onNestedPreFling(parent,layout,layout,0,scroller.getCurrVelocity());

            } else { // 如果AppBarLayout还没有滑到阈值，就让开心的滑动。
                setHeaderTopBottomOffsetV2(parent, layout, scroller.getCurrY());
            }
            ViewCompat.postOnAnimation(layout, this);
        }
    }

    private int getScrollRangeForDragFlingV2(AppBarLayout view) {
        return view.getTotalScrollRange();
    }



    int setHeaderTopBottomOffsetV2(CoordinatorLayout parent, AppBarLayout header, int newOffset) {
        if (callFlingListener != null) {
            callFlingListener.onOffsetChange(newOffset);
        }
        return this.setHeaderTopBottomOffsetV2(parent, header, newOffset, -2147483648, 2147483647);
    }

    int setHeaderTopBottomOffsetV2(CoordinatorLayout parent, AppBarLayout header, int newOffset, int minOffset, int maxOffset) {
        int curOffset = this.getTopAndBottomOffset();
        int consumed = 0;
        if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
            newOffset = MathUtils.clamp(newOffset, minOffset, maxOffset);
            if (curOffset != newOffset) {
                this.setTopAndBottomOffset(newOffset);
                consumed = curOffset - newOffset;
            }
        }

        return consumed;
    }



    @Override
    public boolean setTopAndBottomOffset(int offset) {
        return super.setTopAndBottomOffset(offset);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
        Log.d(TAG, "onInterceptTouchEvent:" + child.getTotalScrollRange());
        allowFling = false;
        shouldBlockNestedScroll = false;
        if (isFlinging) {
            shouldBlockNestedScroll = true;
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                allowFling = false;
                scroller.forceFinished(true);
                stopAppbarLayoutFling(child);  //手指触摸屏幕的时候停止fling事件
                break;
        }

        return super.onInterceptTouchEvent(parent, child, ev);
    }

    /**
     * 反射获取私有的flingRunnable 属性，考虑support 28以后变量名修改的问题
     *
     * @return Field
     */
    private Field getFlingRunnableField() throws NoSuchFieldException {
        try {
            // support design 27及以下版本
            Class<?> headerBehaviorType = this.getClass().getSuperclass().getSuperclass();
            return headerBehaviorType.getDeclaredField("mFlingRunnable");
        } catch (NoSuchFieldException e) {
            // 可能是28及以上版本
            Class<?> headerBehaviorType = this.getClass().getSuperclass().getSuperclass().getSuperclass();
            return headerBehaviorType.getDeclaredField("flingRunnable");
        }
    }

    /**
     * 反射获取私有的scroller 属性，考虑support 28以后变量名修改的问题
     *
     * @return Field
     */
    private Field getScrollerField() throws NoSuchFieldException {
        try {
            // support design 27及以下版本
            Class<?> headerBehaviorType = this.getClass().getSuperclass().getSuperclass();
            return headerBehaviorType.getDeclaredField("mScroller");
        } catch (NoSuchFieldException e) {
            // 可能是28及以上版本
            Class<?> headerBehaviorType = this.getClass().getSuperclass().getSuperclass().getSuperclass();
            return headerBehaviorType.getDeclaredField("scroller");
        }
    }

    /**
     * 停止appbarLayout的fling事件
     *
     * @param appBarLayout
     */
    private void stopAppbarLayoutFling(AppBarLayout appBarLayout) {
        if (this.flingRunnable != null) {
            appBarLayout.removeCallbacks(this.flingRunnable);
            this.flingRunnable = null;
        }
        //通过反射拿到HeaderBehavior中的flingRunnable变量
        try {
            Field flingRunnableField = getFlingRunnableField();
            Field scrollerField = getScrollerField();
            flingRunnableField.setAccessible(true);
            scrollerField.setAccessible(true);

            Runnable flingRunnable = (Runnable) flingRunnableField.get(this);
            OverScroller overScroller = (OverScroller) scrollerField.get(this);
            if (flingRunnable != null) {
                Log.d(TAG, "存在flingRunnable");
                appBarLayout.removeCallbacks(flingRunnable);
                flingRunnableField.set(this, null);
            }
            if (overScroller != null && !overScroller.isFinished()) {
                overScroller.abortAnimation();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout parent, AppBarLayout child, View directTargetChild, View target, int nestedScrollAxes, int type) {
        Log.d(TAG, "onStartNestedScroll");
        allowFling = false;
        stopAppbarLayoutFling(child);
        return super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes, type);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed, int type) {
        Log.d(TAG, "onNestedPreScroll:" + child.getTotalScrollRange() + " ,dx:" + dx + " ,dy:" + dy + " ,type:" + type);

        //type返回1时，表示当前target处于非touch的滑动，
        //该bug的引起是因为appbar在滑动时，CoordinatorLayout内的实现NestedScrollingChild2接口的滑动子类还未结束其自身的fling
        //所以这里监听子类的非touch时的滑动，然后block掉滑动事件传递给AppBarLayout
        if (type == TYPE_FLING) {
            isFlinging = true;
        }
        if (!shouldBlockNestedScroll) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        }
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        Log.d(TAG, "onNestedScroll: target:" + target.getClass() + " ," + child.getTotalScrollRange() + " ,dxConsumed:"
                + dxConsumed + " ,dyConsumed:" + dyConsumed + " " + ",type:" + type);
        if (!shouldBlockNestedScroll) {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout abl, View target, int type) {
        Log.d(TAG, "onStopNestedScroll");
        super.onStopNestedScroll(coordinatorLayout, abl, target, type);
        isFlinging = false;
        shouldBlockNestedScroll = false;
    }


    @Override
    public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull AppBarLayout child, @NonNull View target, float velocityX, float velocityY) {
        //velocityY>0  向上滑，用自定义fling，相反走父类的
        if (velocityY > 0) {
            //如果向上滑，target是RecyclerView,判断是否滑到最后一个，如果不是就自定义滑动，是就走父类的方法。
            if (target instanceof RecyclerView){
                RecyclerView recyclerView = (RecyclerView) target;
                boolean isBottom = judgeRecyclerViewScrollIsBottom(recyclerView);
                if (!isBottom){
                    allowFling = true;
                    fling2(coordinatorLayout, child, Integer.MIN_VALUE, 0, -velocityY);
                    return true;
                }else {
                    return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
                }
            }
            allowFling = true;
            fling2(coordinatorLayout, child, Integer.MIN_VALUE, 0, -velocityY);
            return true;
        } else {
            return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
        }

    }

    private boolean judgeRecyclerViewScrollIsBottom(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager){
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (linearLayoutManager.getOrientation() == RecyclerView.VERTICAL){
                if (linearLayoutManager.findLastVisibleItemPosition() == recyclerView.getAdapter().getItemCount() - 1){
                    return true;
                }
            }else {
                return false;
            }
        }
        return false;
    }
}
package com.zhangdroid.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Point;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import com.zhangdroid.library.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A card view can slide around.
 * Created by zhangdroid on 2016/10/9.
 */
public class SlidingCardLayout extends FrameLayout {
    private static final String TAG = "SlidingCardLayout";
    /**
     * 默认可见的子view个数
     */
    private final int DEFAULT_VISIBLE_VIEW_COUNT = 4;
    /**
     * 拖拽工具类
     */
    private ViewDragHelper mViewDragHelper;
    /**
     * 手势监听工具类
     */
    private GestureDetector mGestureDetector;
    /**
     * 滑动的阙值
     */
    private int mTouchSlop;
    /**
     * 判定为抛飞动作的最小速度
     */
    private int mMinFlingVelocity;
    /**
     * 当前处于最顶部的卡片
     */
    private View mTopCardView;
    /**
     * 可见子view的个数
     */
    private int mVisibleViewCount;
    /**
     * 是否显示堆叠的结构
     */
    private boolean mIsStacked;
    /**
     * 保存释放后的view列表
     */
    private List<View> mReleasedViewList = new ArrayList<View>();
    /**
     * 顶部卡片view的初始位置（实现松开后回到原位置的效果）
     */
    private Point mOriginalPoint = new Point();
    private OnCardSlideListener mOnCardSlideListener;
    private OnCardItemClickListener mOnCardItemClickListener;
    private BaseAdapter mBaseAdapter;
    private final DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
        }
    };

    public SlidingCardLayout(Context context) {
        this(context, null);
    }

    public SlidingCardLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingCardLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, mVDHCallback);
        mGestureDetector = new GestureDetector(context, mSimpleOnGestureListener);
        mGestureDetector.setIsLongpressEnabled(false);

        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
        mMinFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            mViewDragHelper.cancel();
            return false;
        }
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(resolveSizeAndState(MeasureSpec.getSize(widthMeasureSpec), widthMeasureSpec, 0),
                resolveSizeAndState(MeasureSpec.getSize(heightMeasureSpec), heightMeasureSpec, 0));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            int left = getPaddingLeft();
            int top = getPaddingTop();
            childView.layout(left, top, left + childView.getMeasuredWidth(), top + childView.getMeasuredHeight());
//            childView.offsetTopAndBottom(40);
        }

        // 保存顶部卡片View的初始位置
        if (mTopCardView != null) {
            mOriginalPoint.set(mTopCardView.getLeft(), mTopCardView.getTop());
        }
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 处理拖拽逻辑的回调方法类
     */
    private ViewDragHelper.Callback mVDHCallback = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mTopCardView;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (releasedChild == mTopCardView) {
                // 松开TopCardView后回到原位置
                if (mViewDragHelper.smoothSlideViewTo(releasedChild, mOriginalPoint.x, mOriginalPoint.y)) {
                    ViewCompat.postInvalidateOnAnimation(SlidingCardLayout.this);
                }
            }
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return top;
        }

       /* @Override
        public int getViewVerticalDragRange(View child) {
            return child == mTopCardView ? getMeasuredHeight() - child.getMeasuredHeight() : 0;
        }*/

        @Override
        public int getViewHorizontalDragRange(View child) {
            return child == mTopCardView ? getMeasuredWidth() - child.getMeasuredWidth() : 0;
        }

    };

    /**
     * 手势监听器
     */
    private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        /**
         * 监听抛飞手势
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            LogUtil.e(TAG, "SimpleOnGestureListener#onFling() velocityX = " + velocityX + "  velocityY = " + velocityY);
            LogUtil.e(TAG, "SimpleOnGestureListener#onFling() touchSlop = " + mTouchSlop + "  flingVelocity = " + mMinFlingVelocity);
            // 水平方向上滑动的距离
            float dx = e2.getX() - e1.getX();
            // 只处理水平方向上的抛飞动作
            if (Math.abs(dx) > mTouchSlop && (Math.abs(velocityX) > Math.abs(velocityY) && Math.abs(velocityX) > mMinFlingVelocity)) {
                disappearWithAnim();
                return true;
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

    };

    /**
     * 添加卡片view
     */
    private void attachChildViews() {
        removeAllViews();
        if (mBaseAdapter != null && mBaseAdapter.getCount() > 0) {
            // 所有的子view按照从上到下的顺序堆叠放置
            for (int pos = mBaseAdapter.getCount() - 1; pos >= 0; pos--) {
                View childView = mBaseAdapter.getView(pos, null, this);
                if (childView != null) {
                    addViewInLayout(childView, -1, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    requestLayout();
                }
            }
        }
    }

    private void disappearWithAnim() {
        final View topCardView = mTopCardView;
        final float targetX = topCardView.getX();
        final float targetY = topCardView.getY();
        mTopCardView = getChildAt(getChildCount() - 2);
        topCardView.animate()
                .setDuration(500)
                .x(targetX)
                .y(targetY)
                .alpha(0.75f)
                .setListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        removeViewInLayout(topCardView);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        onAnimationEnd(animation);
                    }
                });
    }

    // *************************************** Public methods ***************************************

    /**
     * @return the selected top card view
     */
    public View getTopCardView() {
        return mTopCardView;
    }

    /**
     * 设置可见的子view个数
     *
     * @param visibleViewCount 子view个数
     */
    public void setVisibleViewCount(int visibleViewCount) {
        this.mVisibleViewCount = visibleViewCount;
    }

    public void setAdapter(BaseAdapter baseAdapter) {
        if (baseAdapter == null) {
            throw new IllegalArgumentException("The param baseAdapter cannot be null.");
        }
        if (mBaseAdapter != null) {
            mBaseAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mBaseAdapter = baseAdapter;
        mBaseAdapter.registerDataSetObserver(mDataSetObserver);
        attachChildViews();

        int childCount = getChildCount();
        if (childCount > 0) {
            mTopCardView = getChildAt(childCount - 1);
        }
    }

    public BaseAdapter getAdapter() {
        return mBaseAdapter;
    }

    public void setOnCardSlideListener(OnCardSlideListener listener) {
        if (listener != null) {
            this.mOnCardSlideListener = listener;
        }
    }

    public OnCardSlideListener getOnCardSlideListener() {
        return mOnCardSlideListener;
    }

    public interface OnCardSlideListener {

        void onLeftDisappear(Object itemObj);

        void onRightDisappear(Object itemObj);

        void onScroll();
    }

    public void setOnCardItemClickListener(OnCardItemClickListener listener) {
        if (listener != null) {
            this.mOnCardItemClickListener = listener;
        }
    }

    public OnCardItemClickListener getOnCardItemClickListener() {
        return mOnCardItemClickListener;
    }

    public interface OnCardItemClickListener {

        /**
         * called when the top card view has been clicked.
         *
         * @param view    the top card view was clicked
         * @param itemObj the item data object of top card view
         */
        void onCardViewClick(View view, Object itemObj);
    }

}

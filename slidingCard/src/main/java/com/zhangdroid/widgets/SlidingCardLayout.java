package com.zhangdroid.widgets;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Point;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A card view can slide around.
 * Created by zhangdroid on 2016/10/9.
 */
public class SlidingCardLayout extends FrameLayout {
    /**
     * 拖拽工具类
     */
    private ViewDragHelper mViewDragHelper;
    /**
     * 当前处于最顶部的卡片
     */
    private View mTopCardView;
    /**
     * 保存释放后的view列表
     */
    private List<View> mReleasedViewList = new ArrayList<View>();
    private int mChildCount;
    /**
     * 顶部卡片view的初始位置（实现松开后回到原位置的效果）
     */
    private Point mOriginalPoint = new Point();
    private OnCardSlideListener mOnCardSlideListener;
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
        initViewDragHelper();
    }

    private void initViewDragHelper() {
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, mVDHCallback);
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
        for (int i = 0; i < mChildCount; i++) {
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
    protected void onFinishInflate() {
        super.onFinishInflate();
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

    // *************************************** Public methods ***************************************

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

        mChildCount = getChildCount();
        if (mChildCount > 0) {
            mTopCardView = getChildAt(mChildCount - 1);
        }
    }

    public BaseAdapter getAdapter() {
        return mBaseAdapter;
    }

    public void setOnCardSlideListener(OnCardSlideListener listener) {
        if (listener != null) {
            mOnCardSlideListener = listener;
        }
    }

    public OnCardSlideListener getOnCardSlideListener() {
        return mOnCardSlideListener;
    }

    public interface OnCardSlideListener {

        void onLeftDisappear(Object itemData);

        void onRightDisappear(Object itemData);

        void onCardViewClick(View view, Object itemData);

        void onScroll();
    }

}

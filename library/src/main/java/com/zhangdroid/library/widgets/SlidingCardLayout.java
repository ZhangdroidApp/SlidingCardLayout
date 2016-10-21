package com.zhangdroid.library.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Point;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import com.zhangdroid.library.utils.DensityUtil;
import com.zhangdroid.library.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A stacked card view layout which can slide around and revert to the original position.
 * Created by zhangdroid on 2016/10/9.
 */
public class SlidingCardLayout extends FrameLayout {
    private static final String TAG = "SlidingCardLayout";
    /**
     * 默认可见的子view个数
     */
    private static final int DEFAULT_VISIBLE_CHILDVIEW_COUNT = 3;
    /**
     * 默认动画时长
     */
    private static final int DEFAULT_ANIMATOR_DURATION = 200;
    /**
     * 默认动画透明度
     */
    private static final float DEFAULT_ANIMATOR_ALPHA = 0.7f;
    /**
     * 默认动画旋转角度
     */
    private static final int DEFAULT_ANIMATOR_ROTATION = 45;

    /**
     * X方向上滑动的阙值
     */
    private int x_distance_threhold;
    /**
     * 滑动的阙值
     */
    private int mTouchSlop;
    /**
     * 判定为抛飞动作的最小速度
     */
    private int mMinFlingVelocity;
    /**
     * 拖拽工具类
     */
    private ViewDragHelper mViewDragHelper;
    /**
     * 屏幕宽度
     */
    private int mScreenWidth;

    /**
     * 当前处于最顶部的卡片
     */
    private View mTopCardView;
    /**
     * 顶部卡片view的初始位置（实现松开后回到原位置的效果）
     */
    private Point mOriginalPoint = new Point();
    /**
     * 初始x,y坐标中心点坐标
     */
    private float[] mOriginalCenter = new float[2];
    /**
     * 保存释放后的view列表(包括滑出方向)
     */
    private List<View> mReleasedViewList = new ArrayList<View>();
    /**
     * 保存释放后的view对应的数据的列表
     */
    private List<Object> mRelasedViewDataList = new ArrayList<Object>();
    private boolean mIsReverting = false;
    private boolean mIsFlingLeft = false;
    private boolean mIsFlingRight = false;

    /**
     * 可见子view的个数
     */
    private int mVisibleViewCount;
    /**
     * adapter中缓冲保存子view栈的大小
     */
    private int mAdapterStackSize;
    /**
     * 是否显示堆叠的结构
     */
    private boolean mIsStacked;

    private OnCardSlidingListener mOnCardSlidingListener;
    private OnCardItemClickListener mOnCardItemClickListener;
    private OnRevertListener mOnRevertListener;
    private BaseAdapter mBaseAdapter;
    private final DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            removeAllViews();
            mTopCardView = null;
            if (mReleasedViewList != null) {
                mReleasedViewList.clear();
            }
            if (mRelasedViewDataList != null) {
                mRelasedViewDataList.clear();
            }
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
        mAdapterStackSize = 2 * DEFAULT_VISIBLE_CHILDVIEW_COUNT;

    }

    private void init(Context context) {
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, mVDHCallback);

        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
        x_distance_threhold = 5 * mTouchSlop;
        mMinFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        mScreenWidth = DensityUtil.getScreenWidth(context);

        LogUtil.e(TAG, "init() touchSlop = " + mTouchSlop + "  flingVelocity = " + mMinFlingVelocity
                + "  mScreenWidth = " + mScreenWidth);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
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
        if (mBaseAdapter != null) {
            int adapterCount = mBaseAdapter.getCount();
            LogUtil.e(TAG, "onLayout() mAdapterStackSize = " + mAdapterStackSize + "  adapterCount = " + adapterCount);
            if (adapterCount > 0) {
                // 计算当前需要添加的子view的个数
                int child_view_stack = Math.min(mAdapterStackSize, adapterCount);
                // 所有的子view按照从上到下的顺序堆叠放置
                for (int pos = 0; pos < child_view_stack; pos++) {
                    View childView = getChildAt(pos);
                    if (childView != null) {
                        int left = getPaddingLeft();
                        int top = getPaddingTop();
                        childView.layout(left, top, left + childView.getMeasuredWidth(), top + childView.getMeasuredHeight());
                    }
                }
                // 设置当前顶部卡片view
                setupTopCardView();
            } else {
                removeAllViewsInLayout();
            }

            if (adapterCount <= mAdapterStackSize && mOnCardSlidingListener != null) {
                mOnCardSlidingListener.onAdapterApproachInEmpty(adapterCount);
            }
        }
    }

    private void setupTopCardView() {
        LogUtil.e(TAG, "setupTopCardView()");
        final int childCount = getChildCount();
        if (childCount > 0) {
            mTopCardView = getChildAt(childCount - 1);
            if (mTopCardView != null) {
                // 保存顶部卡片View的初始位置
                mOriginalPoint.set(mTopCardView.getLeft(), mTopCardView.getTop());
                mOriginalCenter[0] = mTopCardView.getX();
                mOriginalCenter[1] = mTopCardView.getY();
                LogUtil.e(TAG, "mOriginalPoint.x = " + mOriginalPoint.x + "  mOriginalPoint.y = " + mOriginalPoint.y);
                LogUtil.e(TAG, "mOriginalCenter[0] = " + mOriginalCenter[0] + "  mOriginalCenter[1] = " + mOriginalCenter[1]);
                mTopCardView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnCardItemClickListener != null) {
                            mOnCardItemClickListener.onCardViewClick(v, mBaseAdapter == null ? null : mBaseAdapter.getItem(0));
                        }
                    }
                });
            }
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
            LogUtil.e(TAG, "onViewPositionChanged() left = " + left + " top = " + top + " dx = " + dx + " dy = " + dy);
            if (mOnCardSlidingListener != null) {
                // 计算水平方向上滑动的距离百分比
                int originalLeft = mOriginalPoint.x;
                int childWidth = changedView.getWidth();
                if (left < originalLeft) {// 向左边滑动（X负方向）
                    float xScrolledDistance = originalLeft - left;
                    mOnCardSlidingListener.onScroll(-1.0f * (xScrolledDistance >= childWidth ? 1 : xScrolledDistance / childWidth));
                } else {// 向右边滑动（X正方向）
                    float xScrolledDistance = left - originalLeft;
                    mOnCardSlidingListener.onScroll(1.0f * (xScrolledDistance >= childWidth ? 1 : xScrolledDistance / childWidth));
                }
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (releasedChild == mTopCardView) {
                // 拖动松开时处理左右滑动消失的动画
                disappearWithAnim(xvel, yvel);
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

        @Override
        public int getViewVerticalDragRange(View child) {
            return child == mTopCardView ? getMeasuredHeight() - child.getMeasuredHeight() : 0;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return child == mTopCardView ? getMeasuredWidth() - child.getMeasuredWidth() : 0;
        }

    };

    private void disappearWithAnim(float velocityX, float velocityY) {
        LogUtil.e(TAG, "disappearWithAnim() velocityX = " + velocityX + "  velocityY = " + velocityY);
        if (mTopCardView == null) {
            return;
        }
        float dx = mTopCardView.getLeft() - mOriginalPoint.x;
        if (Math.abs(dx) > x_distance_threhold && Math.abs(velocityX) > 3 * mMinFlingVelocity) {// 水平方向上向左滑动的距离超过阙值且速度最够大超过阙值
            if (velocityX < 0) {// x方向速度为负，向左边滑出屏幕
                flingToLeft();
            } else {// x方向速度为正，向右边滑出屏幕
                flingToRight();
            }
        } else {// 否则松开TopCardView后回到原位置
            int finalLeft = mOriginalPoint.x;
            int finalTop = mOriginalPoint.y;
            if (mViewDragHelper.smoothSlideViewTo(mTopCardView, finalLeft, finalTop)) {
                ViewCompat.postInvalidateOnAnimation(SlidingCardLayout.this);
            }
        }
    }

    /**
     * 移除当前顶部卡片view并从adapter中添加一个子view到末尾
     *
     * @param topCardView
     */
    private void removeTopAndAddToLast(View topCardView) {
        mReleasedViewList.add(topCardView);
        mRelasedViewDataList.add(mBaseAdapter == null ? null : mBaseAdapter.getItem(0));
        if (mOnRevertListener != null) {
            mOnRevertListener.onRevertStateChanged(true);
        }
        removeViewInLayout(topCardView);
        addChildToLast();
    }

    /**
     * 从adapter中添加一个view至FrameLayout的末尾(若adapter已没有待添加的view，则不做处理)
     */
    private void addChildToLast() {
        if (mBaseAdapter != null) {
            int adapterCount = mBaseAdapter.getCount();
            int index = getChildCount();
            if (adapterCount - index > 0) {// adapter中还有未添加过的子view
                View childView = mBaseAdapter.getView(index + 1, null, this);
                LogUtil.e(TAG, "addChildToLast() index = " + index + " childView is null = " + (childView == null));
                if (childView != null) {
                    FrameLayout.LayoutParams layoutParams = (LayoutParams) childView.getLayoutParams();
                    addViewInLayout(childView, 0, layoutParams);
                }
            }
        }
    }

    private void attachChildViews() {
        LogUtil.e(TAG, "attachChildViews()");
        if (mBaseAdapter != null) {
            int adapterCount = mBaseAdapter.getCount();
            if (adapterCount > 0) {
                // 计算当前需要添加的子view的个数
                int child_view_stack = Math.min(mAdapterStackSize, adapterCount);
                // 所有的子view按照从上到下的顺序堆叠放置
                for (int pos = child_view_stack - 1; pos >= 0; pos--) {
                    View childView = mBaseAdapter.getView(pos, null, this);
                    if (childView != null) {
                        FrameLayout.LayoutParams layoutParams = (LayoutParams) childView.getLayoutParams();
                        addViewInLayout(childView, -1, layoutParams);
                    }
                }
            }
        }
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

    public void flingToLeft() {
        flingToLeft(DEFAULT_ANIMATOR_DURATION);
    }

    /**
     * 向左侧滑出
     *
     * @param duration 动画时长
     */
    public void flingToLeft(int duration) {
        if (!mIsFlingLeft && mTopCardView != null) {
            mIsFlingLeft = true;
            if (mOnCardSlidingListener != null) {
                mOnCardSlidingListener.onScroll(-1.0f);
            }
            mTopCardView.animate()
                    .setDuration(duration)
                    .setInterpolator(new AccelerateInterpolator())
                    .x(mTopCardView.getX() - mScreenWidth)
                    .y(mTopCardView.getY())
                    .rotation(-DEFAULT_ANIMATOR_ROTATION)
                    .alpha(DEFAULT_ANIMATOR_ALPHA)
                    .setListener(new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            removeTopAndAddToLast(mTopCardView);
                            if (mOnCardSlidingListener != null) {
                                mOnCardSlidingListener.onLeftDisappear(mBaseAdapter == null ? null : mBaseAdapter.getItem(0));
                                mOnCardSlidingListener.removeFirstObjInAdapter();
                                mOnCardSlidingListener.onScroll(0.0f);
                            }
                            mIsFlingLeft = false;
                        }
                    });
        }
    }

    public void flingToRight() {
        flingToRight(DEFAULT_ANIMATOR_DURATION);
    }

    /**
     * 向右侧滑出
     *
     * @param duration 动画时长
     */
    public void flingToRight(int duration) {
        if (!mIsFlingRight && mTopCardView != null) {
            mIsFlingRight = true;
            if (mOnCardSlidingListener != null) {
                mOnCardSlidingListener.onScroll(1.0f);
            }
            mTopCardView.animate()
                    .setDuration(duration)
                    .setInterpolator(new AccelerateInterpolator())
                    .x(mTopCardView.getX() + mScreenWidth)
                    .y(mTopCardView.getY())
                    .rotation(DEFAULT_ANIMATOR_ROTATION)
                    .alpha(DEFAULT_ANIMATOR_ALPHA)
                    .setListener(new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            removeTopAndAddToLast(mTopCardView);
                            if (mOnCardSlidingListener != null) {
                                mOnCardSlidingListener.onRightDisappear(mBaseAdapter == null ? null : mBaseAdapter.getItem(0));
                                mOnCardSlidingListener.removeFirstObjInAdapter();
                                mOnCardSlidingListener.onScroll(0.0f);
                            }
                            mIsFlingRight = false;
                        }
                    });
        }
    }

    /**
     * @return 是否可以恢复上一个已经滑过的卡片view
     */
    public boolean canRevertToLast() {
        return (mReleasedViewList != null && mReleasedViewList.size() > 0)
                && (mRelasedViewDataList != null && mRelasedViewDataList.size() > 0);
    }

    /**
     * 将上一个已经划过的卡片view移回屏幕
     */
    public void revertToLast() {
        if (!mIsReverting && canRevertToLast()) {
            mIsReverting = true;
            // 获得上一个卡片view
            final int indexOfViewList = mReleasedViewList.size() - 1;
            final View lastView = mReleasedViewList.get(indexOfViewList);
            final int indexOfViewDataList = mRelasedViewDataList.size() - 1;
            final Object lastViewData = mRelasedViewDataList.get(indexOfViewDataList);
            if (lastView != null) {
                // 恢复上一个被移除的卡片view
                FrameLayout.LayoutParams layoutParams = (LayoutParams) lastView.getLayoutParams();
                addViewInLayout(lastView, -1, layoutParams);
                // 将上一个被移除的卡片view移回到屏幕顶部
                lastView.animate()
                        .setDuration(DEFAULT_ANIMATOR_DURATION)
                        .setInterpolator(new DecelerateInterpolator())
                        .x(mOriginalCenter[0])
                        .y(mOriginalCenter[1])
                        .rotation(0)
                        .alpha(1.0f)
                        .setListener(new AnimatorListenerAdapter() {

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                // 更新缓存数据列表
                                mReleasedViewList.remove(indexOfViewList);
                                mRelasedViewDataList.remove(indexOfViewDataList);

                                // 将上一个被移除的卡片view的数据源添加到BaseAdapter中
                                if (lastViewData != null && mOnRevertListener != null) {
                                    mOnRevertListener.onRevertInAdapter(0, lastViewData);
                                    mOnRevertListener.onRevertStateChanged(canRevertToLast());
                                }

                                mIsReverting = false;
                            }
                        });
            }
        }
    }

    public void setAdapter(BaseAdapter baseAdapter) {
        if (baseAdapter == null) {
            throw new IllegalArgumentException("The param baseAdapter cannot be null.");
        }
        if (mBaseAdapter != null && mDataSetObserver != null) {
            mBaseAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mBaseAdapter = baseAdapter;
        mBaseAdapter.registerDataSetObserver(mDataSetObserver);
        attachChildViews();
    }

    public BaseAdapter getAdapter() {
        return mBaseAdapter;
    }

    public void setOnCardSlidingListener(OnCardSlidingListener listener) {
        if (listener != null) {
            this.mOnCardSlidingListener = listener;
        }
    }

    public OnCardSlidingListener getOnCardSlidingListener() {
        return mOnCardSlidingListener;
    }

    public interface OnCardSlidingListener {

        /**
         * 更新adapter数据源，移除第一个
         */
        void removeFirstObjInAdapter();

        /**
         * 更新adapter数据源，加载更多
         *
         * @param adapterDataCount adapter中当前剩余的数据项个数
         */
        void onAdapterApproachInEmpty(int adapterDataCount);

        void onLeftDisappear(Object itemObj);

        void onRightDisappear(Object itemObj);

        /**
         * 滑动监听回调
         *
         * @param scrollPercent 当前已经滑动过的距离百分比，负数表示向X轴负方向即左边
         */
        void onScroll(float scrollPercent);

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

    public void setOnRevertListener(OnRevertListener listener) {
        if (listener != null) {
            this.mOnRevertListener = listener;
        }
    }

    public OnRevertListener getOnRevertListener() {
        return mOnRevertListener;
    }

    public interface OnRevertListener {

        /**
         * called when the last top card view has been reverted.
         * <p>更新adapter数据源，增加上一个，注意添加的位置索引必须是0，这样才能回到顶部</p>
         *
         * @param index             the index of this item added in the adapter，it must be 0
         * @param revertViewDataObj the view data object of last top card view
         */
        void onRevertInAdapter(int index, Object revertViewDataObj);

        /**
         * 是否可以恢复上一个卡片view
         *
         * @param canRevert
         */
        void onRevertStateChanged(boolean canRevert);

    }

}

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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
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
    private static final int DEFAULT_VISIBLE_CHILDVIEW_COUNT = 3;

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

    private boolean mIsInLayout = false;
    /**
     * 当前处于最顶部的卡片
     */
    private View mTopCardView;
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
    /**
     * 保存释放后的view列表
     */
    private List<View> mReleasedViewList = new ArrayList<View>();
    /**
     * 顶部卡片view的初始位置（实现松开后回到原位置的效果）
     */
    private Point mOriginalPoint = new Point();

    private OnCardSlidingListener mOnCardSlidingListener;
    private OnCardItemClickListener mOnCardItemClickListener;
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

        LogUtil.e(TAG, "init() touchSlop = " + mTouchSlop + "  flingVelocity = " + mMinFlingVelocity);
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
        if (mIsInLayout) {
            return;
        }
        mIsInLayout = true;
        if (mBaseAdapter != null) {
            int adapterCount = mBaseAdapter.getCount();
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
//                        childView.offsetTopAndBottom(40);
//                        childView.offsetLeftAndRight(30);
                    }
                }
                // 设置当前顶部卡片view
                setupTopCardView();
            } else {
                removeAllViewsInLayout();
            }

            mIsInLayout = false;

            LogUtil.e(TAG, "onLayout() mAdapterStackSize = " + mAdapterStackSize + "  adapterCount = " + adapterCount);
            if (adapterCount <= mAdapterStackSize && mOnCardSlidingListener != null) {
                mOnCardSlidingListener.onAdapterApproachInEmpty(adapterCount);
            }
        }
    }

    private void setupTopCardView() {
        final int childCount = getChildCount();
        LogUtil.e(TAG, "setupTopCardView() childCount = " + childCount);
        if (childCount > 0) {
            mTopCardView = getChildAt(childCount - 1);
            if (mTopCardView != null) {
                // 保存顶部卡片View的初始位置
                mOriginalPoint.set(mTopCardView.getLeft(), mTopCardView.getTop());
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
            super.onViewPositionChanged(changedView, left, top, dx, dy);
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

       /* @Override
        public int getViewVerticalDragRange(View child) {
            return child == mTopCardView ? getMeasuredHeight() - child.getMeasuredHeight() : 0;
        }*/

        @Override
        public int getViewHorizontalDragRange(View child) {
            return child == mTopCardView ? getMeasuredWidth() - child.getMeasuredWidth() : 0;
        }

    };

    private void onTopCardViewScroll() {

    }

    private void disappearWithAnim(float velocityX, float velocityY) {
        LogUtil.e(TAG, "disappearWithAnim() velocityX = " + velocityX + "  velocityY = " + velocityY);
        if (mTopCardView == null) {
            return;
        }
        final View topCardView = mTopCardView;
        float dx = topCardView.getLeft() - mOriginalPoint.x;
        int finalLeft = mOriginalPoint.x;
        int finalTop = mOriginalPoint.y;
        if (Math.abs(dx) > x_distance_threhold && Math.abs(velocityX) > 3 * mMinFlingVelocity) {// 水平方向上向左滑动的距离超过阙值且速度最够大超过阙值
            // 计算动画最终移动到的x,y坐标上的位置
            if (velocityX < 0) {// x方向速度为负，向左边滑出屏幕
                finalLeft = -topCardView.getWidth();
                finalTop = topCardView.getTop();
            } else {// x方向速度为正，向左边滑出屏幕
                finalLeft = getWidth();
                finalTop = topCardView.getTop();
            }
            mViewDragHelper.smoothSlideViewTo(topCardView, finalLeft, finalTop);

            // 移除当前顶部卡片view并从adapter中添加一个子view到末尾
            mReleasedViewList.add(topCardView);
            removeView(topCardView);
            addChildToLast();

            if (mOnCardSlidingListener != null) {
                Object itemObj = new Object();
                if (mBaseAdapter != null) {
                    itemObj = mBaseAdapter.getItem(0);
                }
                if (velocityX < 0) {
                    mOnCardSlidingListener.onLeftDisappear(itemObj);
                } else {
                    mOnCardSlidingListener.onRightDisappear(itemObj);
                }
                mOnCardSlidingListener.removeFirstObjInAdapter();
            }
        } else {// 否则松开TopCardView后回到原位置
            if (mViewDragHelper.smoothSlideViewTo(topCardView, finalLeft, finalTop)) {
                ViewCompat.postInvalidateOnAnimation(SlidingCardLayout.this);
            }
        }
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
        flingToLeft(200);
    }

    public void flingToLeft(int duration) {
        if (mTopCardView != null) {
            mTopCardView.animate()
                    .setDuration(duration)
                    .setInterpolator(new AccelerateInterpolator())
                    .x(-mTopCardView.getWidth())
                    .y(mTopCardView.getTop())
                    .rotation(45)
                    .alpha(0.8f)
                    .setListener(new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (mOnCardSlidingListener != null) {
                                mOnCardSlidingListener.onLeftDisappear(mBaseAdapter == null ? null : mBaseAdapter.getItem(0));
                            }
                        }
                    });
        }
    }

    public void flingToRight() {
        flingToRight(200);
    }

    public void flingToRight(int duration) {
        if (mTopCardView != null) {
            mTopCardView.animate()
                    .setDuration(duration)
                    .setInterpolator(new AccelerateInterpolator())
                    .x(getWidth())
                    .y(mTopCardView.getTop())
                    .rotation(-45)
                    .alpha(0.8f)
                    .setListener(new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (mOnCardSlidingListener != null) {
                                mOnCardSlidingListener.onRightDisappear(mBaseAdapter == null ? null : mBaseAdapter.getItem(0));
                            }
                        }
                    });
        }
    }

    public void revertToLast() {

    }

    public void setAdapter(BaseAdapter baseAdapter) {
        LogUtil.e(TAG, "setAdapter()");
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

    public void setOnCardSlideListener(OnCardSlidingListener listener) {
        if (listener != null) {
            this.mOnCardSlidingListener = listener;
        }
    }

    public OnCardSlidingListener getOnCardSlideListener() {
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

        void onScroll(int scrollPercent);
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

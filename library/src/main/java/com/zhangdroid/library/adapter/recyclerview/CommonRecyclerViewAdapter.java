package com.zhangdroid.library.adapter.recyclerview;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.zhangdroid.library.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>The common base adapter for RecyclerView. This adapter is only support single item type.
 * For multi item type, use the {@link MultiItemRecyclerViewAdapter} instead.<p/>
 * Created by zhangdroid on 2016/6/21.
 */
public abstract class CommonRecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // header view
    private static final int ITEM_TYPE_HEADER = 0;
    private static final int ITEM_TYPE_NORMAL = 1;
    public Context mContext;
    private int mLayoutResId;
    public List<T> mDataList;
    private View mHeaderView;
    // 单击及长按事件监听器
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        if (onItemClickListener != null) {
            mOnItemClickListener = onItemClickListener;
        }
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        if (onItemLongClickListener != null) {
            mOnItemLongClickListener = onItemLongClickListener;
        }
    }

    public void addHeaderView(View view) {
        if (view != null) {
            mHeaderView = view;
        }
    }

    public void setOnHeaderViewClickListener(View.OnClickListener listener) {
        if (listener != null) {
            mHeaderView.setOnClickListener(listener);
        }
    }

    public List<T> getAdapterData() {
        return mDataList;
    }

    /**
     * 绑定数据源
     */
    public void replaceAll(List<T> list) {
        if (Util.isListEmpty(list)) {
            return;
        }
        if (!Util.isListEmpty(mDataList)) {
            mDataList.clear();
        }
        mDataList.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * 分页时使用
     */
    public void appendToList(List<T> list) {
        if (Util.isListEmpty(list)) {
            return;
        }
        mDataList.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * 清空数据
     */
    public void removeAll() {
        if (!Util.isListEmpty(mDataList)) {
            mDataList.clear();
            notifyDataSetChanged();
        }
    }

    /**
     * 局部刷新
     */
    public void updateItem(int position, T t) {
        if (!isValidPosition(position) || t == null) {
            return;
        }
        mDataList.remove(position);
        mDataList.add(position, t);
        notifyItemChanged(position, t);
    }

    /**
     * 删除指定位置的item
     *
     * @param position
     */
    public void removeItem(int position) {
        if (!isValidPosition(position)) {
            return;
        }
        mDataList.remove(position);
        notifyItemRemoved(position);
        // 由于复用机制，删除Item后需要刷新position之后所有item，防止Position错乱
        notifyItemRangeChanged(position, getItemCount() - 1 - position);
    }

    /**
     * 根据指定位置获得对应的数据对象
     *
     * @param position
     * @return
     */
    public T getItemByPosition(int position) {
        if (!Util.isListEmpty(mDataList) && isValidPosition(position)) {
            return mDataList.get(position);
        }
        return null;
    }

    public boolean isValidPosition(int position) {
        return (position >= 0 && position < getItemCount());
    }

    private int getRealPosition(int position) {
        return mHeaderView == null ? position : position - 1;
    }

    public CommonRecyclerViewAdapter(Context context, int layoutResId) {
        this(context, layoutResId, null);
    }

    public CommonRecyclerViewAdapter(Context context, int layoutResId, List<T> list) {
        this.mContext = context;
        this.mLayoutResId = layoutResId;
        this.mDataList = (list == null ? new ArrayList<T>() : new ArrayList<>(list));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_HEADER) {
            return new HeaderViewHolder(mHeaderView);
        }
        return RecyclerViewHolder.getInstance(mContext, -1, null, parent, mLayoutResId);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mHeaderView != null && getItemViewType(position) == ITEM_TYPE_HEADER) {
            return;
        }
        RecyclerViewHolder recyclerViewHolder = (RecyclerViewHolder) holder;
        final int realPosition = getRealPosition(position);
        convert(realPosition, recyclerViewHolder, mDataList == null ? null : mDataList.get(realPosition));
        addListeners(recyclerViewHolder, realPosition);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        // 处理StaggeredGridLayoutManager，设置充满整行
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        if (layoutParams != null && layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
            int position = holder.getLayoutPosition();
            // 设置HeaderView充满整行
            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
            lp.setFullSpan(getItemViewType(position) == ITEM_TYPE_HEADER);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        // 处理GridLayoutManager，设置充满整行
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            // 设置某个item所占据的列数或行数
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

                @Override
                public int getSpanSize(int position) {
                    // 设置HeaderView充满整行
                    if (getItemViewType(position) == ITEM_TYPE_HEADER) {
                        return gridLayoutManager.getSpanCount();
                    }
                    return 1;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mHeaderView == null ? mDataList.size() : mDataList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mHeaderView != null && position == 0) {
            return ITEM_TYPE_HEADER;
        }
        return ITEM_TYPE_NORMAL;
    }

    /**
     * add item onItemClick and onItenLongClick listener
     *
     * @param holder {@link RecyclerViewHolder}
     */
    private void addListeners(final RecyclerViewHolder holder, final int position) {
        holder.getConvertView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, position, holder);
                }
            }
        });
        holder.getConvertView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onItemLongClick(v, position, holder);
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Implement this method and use the viewHolder to adapt the view of the given item bean.
     *
     * @param position   the item position in the adapter
     * @param viewHolder the RecyclerViewHolder for CommonRecyclerViewAdapter
     * @param bean       the item bean to display
     */
    protected abstract void convert(int position, RecyclerViewHolder viewHolder, T bean);

    public interface OnItemClickListener {
        /**
         * on item click listener for recyclerview
         *
         * @param view
         * @param position
         * @param viewHolder {@link RecyclerViewHolder}
         */
        void onItemClick(View view, int position, RecyclerViewHolder viewHolder);
    }

    public interface OnItemLongClickListener {
        /**
         * on item long click listener for recyclerview
         *
         * @param view
         * @param position
         * @param viewHolder {@link RecyclerViewHolder}
         */
        void onItemLongClick(View view, int position, RecyclerViewHolder viewHolder);
    }

    /**
     * Header view holder
     */
    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

}

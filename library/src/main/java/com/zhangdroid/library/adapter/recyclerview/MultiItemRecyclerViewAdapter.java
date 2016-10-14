package com.zhangdroid.library.adapter.recyclerview;

import android.content.Context;
import android.view.ViewGroup;

import java.util.List;

/**
 * Multi item type for RecyclerView, extends from {@link CommonRecyclerViewAdapter}
 *
 * @see CommonRecyclerViewAdapter
 * Created by zhangdroid on 2016/6/21.
 */
public abstract class MultiItemRecyclerViewAdapter<T> extends CommonRecyclerViewAdapter<T> {

    public MultiItemRecyclerViewAdapter(Context context, int layoutResId) {
        this(context, layoutResId, null);
    }

    public MultiItemRecyclerViewAdapter(Context context, int layoutResId, List<T> list) {
        super(context, layoutResId, list);
    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewType(position, mDataList.get(position));
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (getItemLayoutResId(viewType) > 0) {
            return RecyclerViewHolder.getInstance(mContext, -1, null, parent, getItemLayoutResId(viewType));
        }
        return null;
    }

    /**
     * get item view type
     *
     * @param position the position of recyclerview
     * @param t        the bean of this position, for generate item view type
     * @return
     */
    protected abstract int getItemViewType(int position, T t);

    /**
     * get item view layout resource id by viewType from {@link #getItemViewType(int position, T t)}
     *
     * @param viewType item view type
     * @return item view layout resource id
     */
    protected abstract int getItemLayoutResId(int viewType);

}

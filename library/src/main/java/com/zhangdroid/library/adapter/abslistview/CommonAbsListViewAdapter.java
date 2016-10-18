package com.zhangdroid.library.adapter.abslistview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.zhangdroid.library.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * The common base adapter for ListView and GridView.
 * Created by zhangdroid on 2016/2/17.
 */
public abstract class CommonAbsListViewAdapter<T> extends BaseAdapter {
    protected Context mContext;
    private int mLayoutRes;
    private List<T> mDataList;

    public List<T> getDataList() {
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
    public void clear() {
        if (!Util.isListEmpty(mDataList)) {
            mDataList.clear();
            notifyDataSetChanged();
        }
    }

    /**
     * 局部刷新
     *
     * @param position
     * @param t
     */
    public void updateItem(int position, T t) {
        if (isValidPosition(position)) {
            if (!Util.isListEmpty(mDataList)) {
                mDataList.remove(position);
                mDataList.add(position, t);
                notifyDataSetChanged();
            }
        }
    }

    /**
     * 删除指定位置的Item
     *
     * @param position
     */
    public void removeItem(int position) {
        if (isValidPosition(position)) {
            if (!Util.isListEmpty(mDataList)) {
                mDataList.remove(position);
                notifyDataSetChanged();
            }
        }
    }

    public boolean isValidPosition(int position) {
        return (position >= 0 && position < getCount());
    }

    public CommonAbsListViewAdapter(Context context, int layoutResId) {
        this(context, layoutResId, null);
    }

    public CommonAbsListViewAdapter(Context context, int layoutResId, List<T> list) {
        this.mContext = context;
        this.mLayoutRes = layoutResId;
        this.mDataList = (list == null ? new ArrayList<T>() : new ArrayList<>(list));
    }

    @Override
    public int getCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        if (position >= getCount() || mDataList == null)
            return null;
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BaseAdapterHelper baseAdapterHelper = BaseAdapterHelper.getInstance(mContext, position, convertView, parent, mLayoutRes);
        convert(position, baseAdapterHelper, (T) getItem(position));
        return baseAdapterHelper.getConvertView();
    }

    /**
     * Implement this method and use the helper to adapt the view of the given item bean.
     *
     * @param position the item position in the adapter
     * @param helper   the BaseAdapterHelper for CommonAbsListViewAdapter
     * @param bean     the item bean to display
     */
    protected abstract void convert(int position, BaseAdapterHelper helper, T bean);

}

package com.zhangdroid.library.adapter.recyclerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * <p>The common ViewHolder for RecyclerView. This class help {@link CommonRecyclerViewAdapter} obtain the ViewHolder and the associated item data,
 * and provide some methods to set the views properties.</p>
 * Created by zhangdroid on 2016/6/21.
 */
public class RecyclerViewHolder extends RecyclerView.ViewHolder {
    private Context mContext;
    private int mPosition;
    private View mConvertView;
    /**
     * 保存ViewHolder中的每个Item对应的View，key为View的ID
     */
    private SparseArray<View> mItemViews;

    public Context getContext() {
        return mContext;
    }

    public int getItemPosition() {
        if (mPosition == -1)
            throw new IllegalStateException("You must call the BaseAdapterHelper constructor method to obtain the position.");
        return mPosition;
    }

    public View getConvertView() {
        return mConvertView;
    }

    public RecyclerViewHolder(Context context, int position, View itemView) {
        super(itemView);
        this.mContext = context;
        this.mPosition = position;
        this.mConvertView = itemView;
        mItemViews = new SparseArray<View>();
        // set BaseAdapterHelper as tag
        mConvertView.setTag(this);
    }

    public static RecyclerViewHolder getInstance(Context context, int position, View convertView, ViewGroup parent, int layoutResId) {
        if (convertView == null) {
            View itemView = LayoutInflater.from(context).inflate(layoutResId, parent, false);
            return new RecyclerViewHolder(context, position, itemView);
        }
        // get tag BaseAdapterHelper and set position
        RecyclerViewHolder tagBaseAdapterHelper = (RecyclerViewHolder) convertView.getTag();
        tagBaseAdapterHelper.mPosition = position;
        return tagBaseAdapterHelper;
    }

    /**
     * 通过ID获得View
     *
     * @param viewId the id of view that you want to get
     */
    public View getView(int viewId) {
        View view = mItemViews.get(viewId);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
            mItemViews.put(viewId, view);
        }
        return view;
    }

    public void setText(int viewId, String text) {
        TextView textView = (TextView) getView(viewId);
        textView.setText(text);
    }

    public void setTextColor(int viewId, int color) {
        TextView textView = (TextView) getView(viewId);
        textView.setTextColor(color);
    }

    public void setTextSize(int viewId, float size) {
        TextView textView = (TextView) getView(viewId);
        textView.setTextSize(size);
    }

    public void setImageResource(int viewId, int resId) {
        ImageView imageView = (ImageView) getView(viewId);
        imageView.setImageResource(resId);
    }

    public void setImageBitmap(int viewId, Bitmap bitmap) {
        ImageView imageView = (ImageView) getView(viewId);
        imageView.setImageBitmap(bitmap);
    }

    public void setImageDrawable(int viewId, Drawable drawable) {
        ImageView imageView = (ImageView) getView(viewId);
        imageView.setImageDrawable(drawable);
    }

}

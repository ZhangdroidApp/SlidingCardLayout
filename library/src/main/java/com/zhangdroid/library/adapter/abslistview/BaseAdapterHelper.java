package com.zhangdroid.library.adapter.abslistview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * <p>This class help {@link CommonAbsListViewAdapter} obtain the ViewHolder and the associated item data,
 * and provide some methods to set the views properties.</p>
 * Created by zhangdroid on 2016/2/17.
 */
public class BaseAdapterHelper {
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

    public int getPosition() {
        if (mPosition == -1)
            throw new IllegalStateException("You must call the BaseAdapterHelper constructor method to obtain the position.");
        return mPosition;
    }

    public View getConvertView() {
        return mConvertView;
    }

    private BaseAdapterHelper(Context context, int position, ViewGroup parent, int layoutResId) {
        this.mContext = context;
        this.mPosition = position;
        mItemViews = new SparseArray<>();
        mConvertView = LayoutInflater.from(context).inflate(layoutResId, parent, false);
        // set BaseAdapterHelper as tag
        mConvertView.setTag(this);
    }

    public static BaseAdapterHelper getInstance(Context context, int position, View convertView, ViewGroup parent, int layoutResId) {
        if (convertView == null) {
            return new BaseAdapterHelper(context, position, parent, layoutResId);
        }
        // get tag BaseAdapterHelper and set position
        BaseAdapterHelper tagBaseAdapterHelper = (BaseAdapterHelper) convertView.getTag();
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

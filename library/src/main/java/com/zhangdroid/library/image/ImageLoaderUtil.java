package com.zhangdroid.library.image;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Image loader util, single instance pattern
 * Created by zhangdroid on 2016/5/31.
 */
public class ImageLoaderUtil {

    public static final int LARGE_PIC = 0;
    public static final int MEDIUM_PIC = 1;
    public static final int SMALL_PIC = 2;

    private static volatile ImageLoaderUtil sDefault;

    private ImageLoaderUtil() {
    }

    public static ImageLoaderUtil getInstance() {
        if (sDefault == null) {
            synchronized (ImageLoaderUtil.class) {
                if (sDefault == null) {
                    sDefault = new ImageLoaderUtil();
                }
            }
        }
        return sDefault;
    }

    /**
     * 加载网络图片
     *
     * @param context
     * @param imageLoader
     */
    public void loadImage(Context context, ImageLoader imageLoader) {
        if (TextUtils.isEmpty(imageLoader.getUrl())) {
            return;
        }
        Picasso.with(context)
                .load(imageLoader.getUrl())
                .placeholder(imageLoader.getPlaceHolder())
                .error(imageLoader.getPlaceHolder())
                .transform(imageLoader.getTransformation())
                .config(imageLoader.getConfig())
                .into(imageLoader.getImageView());
    }

    /**
     * 加载圆形图片（drawable）
     *
     * @param context
     * @param imageView
     * @param resId
     */
    public void loadCircleImage(Context context, ImageView imageView, int resId) {
        Picasso.with(context).load(resId).skipMemoryCache().noPlaceholder().transform(new CircleTransform()).into(imageView);
    }

    /**
     * 加载圆形图片（本地File）
     *
     * @param context
     * @param imageView
     * @param file
     */
    public void loadCircleImage(Context context, ImageView imageView, File file) {
        Picasso.with(context).load(file).skipMemoryCache().noPlaceholder().transform(new CircleTransform()).into(imageView);
    }

    /**
     * 加载圆形图片（Uri）
     *
     * @param context
     * @param imageView
     * @param uri
     */
    public void loadCircleImage(Context context, ImageView imageView, Uri uri) {
        Picasso.with(context).load(uri).skipMemoryCache().noPlaceholder().transform(new CircleTransform()).into(imageView);
    }

    /**
     * 加载本地图片（drawable）
     *
     * @param context
     * @param imageView
     * @param resId
     */
    public void loadLocalImage(Context context, ImageView imageView, int resId) {
        Picasso.with(context).load(resId).skipMemoryCache().noPlaceholder().into(imageView);
    }

    /**
     * 加载本地图片（本地File）
     *
     * @param context
     * @param imageView
     * @param file
     */
    public void loadLocalImage(Context context, ImageView imageView, File file) {
        Picasso.with(context).load(file).skipMemoryCache().noPlaceholder().into(imageView);
    }

    /**
     * 加载本地图片（Uri）
     *
     * @param context
     * @param imageView
     * @param uri
     */
    public void loadLocalImage(Context context, ImageView imageView, Uri uri) {
        Picasso.with(context).load(uri).skipMemoryCache().noPlaceholder().into(imageView);
    }

}

package com.zhangdroid.library.image;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.squareup.picasso.Transformation;
import com.zhangdroid.library.R;

/**
 * Image loader config, used build pattern
 * Created by zhangdroid on 2016/5/31.
 */
public class ImageLoader {

    /**
     * 需要加载的图片url
     */
    private String url;
    /**
     * 类型 (大图，中图，小图)
     */
    private int type;
    /**
     * 加载中/加载失败时显示的图片
     */
    private int placeHolder;
    /**
     * 对图片的转换（圆形/圆角）
     */
    private Transformation transformation;
    /**
     * 图片Config
     */
    private Bitmap.Config config;
    private ImageView imageView;

    private ImageLoader(Builder builder) {
        this.url = builder.url;
        this.type = builder.type;
        this.placeHolder = builder.placeHolder;
        this.transformation = builder.transformation;
        this.config = builder.config;
        this.imageView = builder.imageView;
    }

    public String getUrl() {
        return url;
    }

    public int getType() {
        return type;
    }

    public int getPlaceHolder() {
        return placeHolder;
    }

    public Transformation getTransformation() {
        return transformation;
    }

    public Bitmap.Config getConfig() {
        return config;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public static class Builder {
        private String url;
        private int type;
        private int placeHolder;
        private Transformation transformation;
        private Bitmap.Config config;
        private ImageView imageView;

        public Builder() {
            this.type = ImageLoaderUtil.SMALL_PIC;
            this.url = null;
            this.placeHolder = R.drawable.transparent_drawable;
            transformation = new RoundCornerTransform(0);
            config = Bitmap.Config.ARGB_8888;
            this.imageView = null;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder type(int type) {
            this.type = type;
            return this;
        }

        public Builder placeHolder(int placeHolder) {
            this.placeHolder = placeHolder;
            return this;
        }

        public Builder transform(Transformation transformation) {
            this.transformation = transformation;
            return this;
        }

        public Builder config(Bitmap.Config config) {
            this.config = config;
            return this;
        }

        public Builder imageView(ImageView imageView) {
            this.imageView = imageView;
            return this;
        }

        public ImageLoader build() {
            return new ImageLoader(this);
        }

    }

}

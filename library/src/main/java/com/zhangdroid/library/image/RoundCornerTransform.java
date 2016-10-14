package com.zhangdroid.library.image;


import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

/**
 * 圆角图片变换
 * Created by zhangdroid on 2016/6/30.
 */
public class RoundCornerTransform implements Transformation {
    /**
     * 圆角的半径
     */
    private float mRadius;

    public RoundCornerTransform(float radius) {
        this.mRadius = radius;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap squaredBitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight());
        if (squaredBitmap != source) {
            source.recycle();
        }
        Bitmap bitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
        if (bitmap != null){
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            // 圆角矩形
            RectF rectF = new RectF(0, 0, source.getWidth(), source.getHeight());
            canvas.drawRoundRect(rectF, mRadius, mRadius, paint);
        }

        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "RoundCorner";
    }

}

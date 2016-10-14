package com.zhangdroid.library.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 公用工具类
 * Created by zhangdroid on 2016/5/25.
 */
public class Util {

    public static boolean isListEmpty(List<?> list) {
        return list == null || list.size() == 0;
    }

    public static void gotoActivity(Activity from, Class<?> cls, boolean needFinish) {
        Intent intent = new Intent(from, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (needFinish)
            from.finish();
        from.startActivity(intent);
    }

    public static void gotoActivity(Activity from, Class<?> cls, boolean needFinish, String key, boolean value) {
        Intent intent = new Intent(from, cls);
        intent.putExtra(key, value);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (needFinish) {
            from.finish();
        }
        from.startActivity(intent);
    }

    public static void gotoActivity(Activity from, Class<?> cls, boolean needFinish, String key, Serializable value) {
        Intent intent = new Intent(from, cls);
        if (value != null) {
            intent.putExtra(key, value);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (needFinish) {
            from.finish();
        }
        from.startActivity(intent);
    }

    public static void gotoActivity(Activity from, Class<?> cls, boolean needFinish, String key, Parcelable value) {
        Intent intent = new Intent(from, cls);
        if (value != null) {
            intent.putExtra(key, value);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (needFinish) {
            from.finish();
        }
        from.startActivity(intent);
    }

    public static void gotoActivity(Activity from, Class<?> cls, boolean needFinish, Map<String, String> map) {
        Intent intent = new Intent(from, cls);
        if (map != null && !map.isEmpty()) {
            Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = map.get(key);
                if (!TextUtils.isEmpty(value))
                    intent.putExtra(key, value);
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (needFinish) {
            from.finish();
        }
        from.startActivity(intent);
    }

    /**
     * 隐藏键盘
     */
    public static void hideKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 显示键盘
     *
     * @param context
     */
    public static void showKeyboard(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 获取指定位数的小数
     *
     * @param number   double型原始数据
     * @param decimals 小数点后位数, 不能小于1
     */
    public static String getDecimal(double number, int decimals) {
        if (decimals < 1) {
            return number + "";
        }
        StringBuilder builder = new StringBuilder("0.");
        for (int i = 0; i < decimals; i++) {
            builder.append("0");
        }
        DecimalFormat format = new DecimalFormat(builder.toString().trim());
        return format.format(number);
    }

    /**
     * 获得AnimationDrawable，图片动画
     *
     * @param list         动画需要播放的图片集合
     * @param isRepeatable 是否可以重复
     * @param duration     帧间隔（毫秒）
     * @return
     */
    public static AnimationDrawable getFrameAnim(List<Drawable> list, boolean isRepeatable, int duration) {
        AnimationDrawable animationDrawable = new AnimationDrawable();
        animationDrawable.setOneShot(!isRepeatable);
        if (!isListEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                animationDrawable.addFrame(list.get(i), duration);
            }
        }
        return animationDrawable;
    }

}

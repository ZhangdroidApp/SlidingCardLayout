package com.zhangdroid.library.utils;

import android.util.Log;

/**
 * A closeable log util
 * Created by zhangdroid on 2016/7/1.
 */
public class LogUtil {
    private static boolean isPrintLog = true;

    public static final String TAG_WHC = "WHC";
    public static final String TAG_LMF = "LMF";
    public static final String TAG_ZL = "zhangdroid";

    public static void v(String tag, String msg) {
        if (isPrintLog) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (isPrintLog) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (isPrintLog) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (isPrintLog) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (isPrintLog) {
            Log.e(tag, msg);
        }
    }

}

package com.chenxiaov.indexlistview;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * 单位转换工具类
 * Created by chenxv on 16/4/10.
 */
public class DensityUtil {

    private DensityUtil() {
        throw new UnsupportedOperationException("can not be Instantiation.");
    }

    @SuppressWarnings("unused")
    public static float dpToPx(float dp, Context ctx) {
        DisplayMetrics metrics = displayMetrics(ctx);
        if (metrics == null) {
            return dp;
        }
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    @SuppressWarnings("unused")
    public static float spToPx(float sp, Context ctx) {
        DisplayMetrics metrics = displayMetrics(ctx);
        if (metrics == null) {
            return sp;
        }
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics);
    }

    @SuppressWarnings("unused")
    public static float pxToDp(float px, Context ctx) {
        return px / dpScale(ctx);
    }

    @SuppressWarnings("unused")
    public static float pxToSp(float px, Context ctx) {
        return px / spScale(ctx);
    }

    public static float dpScale(Context ctx) {
        DisplayMetrics metrics = displayMetrics(ctx);
        if (metrics == null) {
            return 1;
        }
        return metrics.density;
    }

    public static float spScale(Context ctx) {
        DisplayMetrics metrics = displayMetrics(ctx);
        if (metrics == null) {
            return 1;
        }
        return metrics.scaledDensity;

    }

    public static DisplayMetrics displayMetrics(Context ctx) {
        if (ctx == null) {
            return null;
        }
        return ctx.getResources().getDisplayMetrics();
    }

}

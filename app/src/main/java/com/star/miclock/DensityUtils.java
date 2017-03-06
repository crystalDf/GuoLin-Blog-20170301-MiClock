package com.star.miclock;


import android.content.Context;
import android.util.TypedValue;

public class DensityUtils {

    private DensityUtils() {

    }

    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue,
                context.getResources().getDisplayMetrics());
    }

    public static float px2dp(Context context, int pxValue) {
        return pxValue / context.getResources().getDisplayMetrics().density;
    }

    public static int sp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dpValue,
                context.getResources().getDisplayMetrics());
    }

    public static float px2sp(Context context, int pxValue) {
        return pxValue / context.getResources().getDisplayMetrics().scaledDensity;
    }

}

package com.boilertalk.ballet.toolbox;

import android.content.res.Resources;
import android.util.TypedValue;

public class ConvertHelper {
    public static int dpToPixels(int dp, Resources resources) {
        int px = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                resources.getDisplayMetrics()));
        return px;
    }
}

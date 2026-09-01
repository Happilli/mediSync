package com.bca.medisync.util;

import android.content.Context;

public class ViewUtils {

    public static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }
}


package com.bca.medisync.util;

import android.content.Context;
import android.view.View;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;

public class ViewUtils {

  public static int dp(Context context, int value) {
    return (int) (value * context.getResources().getDisplayMetrics().density);
  }

  public static void setupBackNav(Fragment fragment, MaterialToolbar toolbar) {
    toolbar.setNavigationOnClickListener(v -> back(fragment));
  }

  public static void setupBackNav(Fragment fragment, View clickableView) {
    clickableView.setOnClickListener(v -> back(fragment));
  }

  private static void back(Fragment fragment) {
    fragment.requireActivity().getOnBackPressedDispatcher().onBackPressed();
  }
}

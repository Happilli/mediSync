package com.bca.medisync.util;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;

import com.bca.medisync.R;

public class RoundedListStyler {

  public static void apply(View itemView, int position, int itemCount) {
    Context context = itemView.getContext();
    float density = context.getResources().getDisplayMetrics().density;
    float radius = density * 18f;
    boolean isFirst = position == 0;
    boolean isLast = position == itemCount - 1;

    GradientDrawable bg = new GradientDrawable();
    bg.setColor(context.getColor(R.color.surface));
    bg.setStroke((int) (density * 1.2f), context.getColor(R.color.outline_variant));

    if (isFirst && isLast) {
      bg.setCornerRadius(radius);
    } else if (isFirst) {
      bg.setCornerRadii(new float[] {radius, radius, radius, radius, 0, 0, 0, 0});
    } else if (isLast) {
      bg.setCornerRadii(new float[] {0, 0, 0, 0, radius, radius, radius, radius});
    } else {
      bg.setCornerRadius(0f);
    }
    itemView.setBackground(bg);

    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
    if (lp != null) {
      lp.bottomMargin = isLast ? 0 : (int) (density * 4);
      itemView.setLayoutParams(lp);
    }
  }
}

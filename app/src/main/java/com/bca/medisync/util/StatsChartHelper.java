package com.bca.medisync.util;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bca.medisync.R;
import java.util.List;
import java.util.Map;

public class StatsChartHelper {

  public static final int[] DOT_SHAPES = {
    R.drawable.cookie9sided,
    R.drawable.clover4leaf,
    R.drawable.burst,
    R.drawable.softboom,
    R.drawable.gem,
    R.drawable.pentagon,
    R.drawable.flower,
  };

  public static void bindStatusBreakdown(
      LinearLayout container, Map<String, Integer> statusMap, int total, String emptyMessage) {
    container.removeAllViews();
    if (statusMap == null || statusMap.isEmpty() || total == 0) {
      TextView empty = new TextView(container.getContext());
      empty.setText(emptyMessage);
      empty.setTextColor(container.getContext().getColor(R.color.on_surface_variant));
      container.addView(empty);
      return;
    }
    int i = 0;
    for (Map.Entry<String, Integer> entry : statusMap.entrySet()) {
      container.addView(buildStatusRow(container, entry.getKey(), entry.getValue(), i));
      i++;
    }
  }

  private static View buildStatusRow(LinearLayout parent, String label, int count, int index) {
    android.content.Context context = parent.getContext();
    LinearLayout row = new LinearLayout(context);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(0, ViewUtils.dp(context, 10), 0, ViewUtils.dp(context, 10));

    ImageView dot = new ImageView(context);
    int dotSize = ViewUtils.dp(context, 18);
    LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dotSize, dotSize);
    dotLp.setMarginEnd(ViewUtils.dp(context, 10));
    dot.setLayoutParams(dotLp);
    dot.setImageResource(DOT_SHAPES[index % DOT_SHAPES.length]);
    dot.setColorFilter(context.getColor(statusColor(label)), PorterDuff.Mode.SRC_IN);

    TextView txtLabel = new TextView(context);
    txtLabel.setText(label);
    txtLabel.setTextColor(context.getColor(R.color.on_surface));
    txtLabel.setTextSize(13);
    LinearLayout.LayoutParams lp =
        new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
    txtLabel.setLayoutParams(lp);

    TextView txtCount = new TextView(context);
    txtCount.setText(String.valueOf(count));
    txtCount.setTextColor(context.getColor(R.color.on_surface));
    txtCount.setTextSize(13);
    txtCount.setTypeface(null, Typeface.BOLD);

    row.addView(dot);
    row.addView(txtLabel);
    row.addView(txtCount);
    return row;
  }

  public static int statusColor(String status) {
    switch (status) {
      case "Confirmed":
        return R.color.tertiary;
      case "Pending":
        return R.color.secondary;
      case "Cancelled":
        return R.color.error;
      default:
        return R.color.primary;
    }
  }

  /** months and counts must be the same size and in matching order. */
  public static void bindMonthlyTrend(
      LinearLayout container, List<String> months, List<Integer> counts, int barWidthDp) {
    container.removeAllViews();
    if (months == null || months.isEmpty()) return;
    android.content.Context context = container.getContext();

    int max = 1;
    for (int c : counts) max = Math.max(max, c);

    LinearLayout row = new LinearLayout(context);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.BOTTOM);
    row.setLayoutParams(
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, ViewUtils.dp(context, 140)));

    for (int idx = 0; idx < months.size(); idx++) {
      int count = counts.get(idx);
      LinearLayout col = new LinearLayout(context);
      col.setOrientation(LinearLayout.VERTICAL);
      col.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
      LinearLayout.LayoutParams colLp =
          new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
      col.setLayoutParams(colLp);

      TextView countText = new TextView(context);
      countText.setText(String.valueOf(count));
      countText.setTextSize(11);
      countText.setTextColor(context.getColor(R.color.on_surface_variant));
      countText.setGravity(Gravity.CENTER);

      View bar = new View(context);
      int barHeight =
          (int) (((count / (float) max)) * ViewUtils.dp(context, 90)) + ViewUtils.dp(context, 4);
      LinearLayout.LayoutParams barLp =
          new LinearLayout.LayoutParams(ViewUtils.dp(context, barWidthDp), barHeight);
      barLp.topMargin = ViewUtils.dp(context, 4);
      bar.setLayoutParams(barLp);
      GradientDrawable barDrawable = new GradientDrawable();
      barDrawable.setColor(context.getColor(R.color.primary));
      barDrawable.setCornerRadius(ViewUtils.dp(context, 6));
      bar.setBackground(barDrawable);

      TextView monthLabel = new TextView(context);
      monthLabel.setText(shortMonth(months.get(idx)));
      monthLabel.setTextSize(11);
      monthLabel.setGravity(Gravity.CENTER);
      monthLabel.setTextColor(context.getColor(R.color.on_surface_variant));
      LinearLayout.LayoutParams monthLp =
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      monthLp.topMargin = ViewUtils.dp(context, 4);
      monthLabel.setLayoutParams(monthLp);

      col.addView(countText);
      col.addView(bar);
      col.addView(monthLabel);
      row.addView(col);
    }
    container.addView(row);
  }

  public static String shortMonth(String yyyyMm) {
    try {
      String[] parts = yyyyMm.split("-");
      int monthNum = Integer.parseInt(parts[1]);
      String[] names = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
      };
      return names[monthNum - 1];
    } catch (Exception e) {
      return yyyyMm;
    }
  }
}

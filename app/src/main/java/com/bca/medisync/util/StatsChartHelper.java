package com.bca.medisync.util;

import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bca.medisync.R;
import com.bca.medisync.databinding.ItemMonthBarBinding;
import com.bca.medisync.databinding.ItemStatusRowBinding;
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
    ItemStatusRowBinding rowBinding =
        ItemStatusRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    rowBinding.imgStatusDot.setImageResource(DOT_SHAPES[index % DOT_SHAPES.length]);
    rowBinding.imgStatusDot.setColorFilter(
        parent.getContext().getColor(statusColor(label)), PorterDuff.Mode.SRC_IN);
    rowBinding.txtStatusLabel.setText(label);
    rowBinding.txtStatusCount.setText(String.valueOf(count));
    return rowBinding.getRoot();
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
      ItemMonthBarBinding colBinding =
          ItemMonthBarBinding.inflate(LayoutInflater.from(context), row, false);
      colBinding.txtMonthCount.setText(String.valueOf(count));
      colBinding.txtMonthLabel.setText(shortMonth(months.get(idx)));
      int barHeight =
          (int) (((count / (float) max)) * ViewUtils.dp(context, 90)) + ViewUtils.dp(context, 4);
      LinearLayout.LayoutParams barLp =
          new LinearLayout.LayoutParams(ViewUtils.dp(context, barWidthDp), barHeight);
      colBinding.viewMonthBar.setLayoutParams(barLp);
      GradientDrawable barDrawable = new GradientDrawable();
      barDrawable.setColor(context.getColor(R.color.primary));
      barDrawable.setCornerRadius(ViewUtils.dp(context, 6));
      colBinding.viewMonthBar.setBackground(barDrawable);
      row.addView(colBinding.getRoot());
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

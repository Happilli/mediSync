package com.bca.medisync.util;

import android.content.Context;
import android.widget.TextView;
import com.bca.medisync.R;

public class StatusChip {
  public static void bind(TextView chip, String status) {
    Context ctx = chip.getContext();
    switch (status) {
      case "Confirmed":
        chip.setTextColor(ctx.getColor(R.color.tertiary));
        chip.setBackgroundColor(ctx.getColor(R.color.tertiary_container));
        break;
      case "Pending":
        chip.setTextColor(ctx.getColor(R.color.secondary));
        chip.setBackgroundColor(ctx.getColor(R.color.secondary_container));
        break;
      default:
        chip.setTextColor(ctx.getColor(R.color.primary));
        chip.setBackgroundColor(ctx.getColor(R.color.primary_container));
        break;
    }
  }
}

package com.bca.medisync.util;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bca.medisync.R;

public class InfoRowBinder {

  public static class Row {
    final View view;
    final int icon;
    final String label;
    final String value;

    public Row(View view, int icon, String label, String value) {
      this.view = view;
      this.icon = icon;
      this.label = label;
      this.value = value;
    }
  }

  public static void bind(Row... rows) {
    for (int i = 0; i < rows.length; i++) {
      Row row = rows[i];
      ((ImageView) row.view.findViewById(R.id.imgRowIcon)).setImageResource(row.icon);
      ((TextView) row.view.findViewById(R.id.txtRowLabel)).setText(row.label);
      ((TextView) row.view.findViewById(R.id.txtRowValue)).setText(row.value);
      RoundedListStyler.apply(row.view, i, rows.length);
    }
  }

  public static void setValue(View row, String value) {
    ((TextView) row.findViewById(R.id.txtRowValue)).setText(value);
  }
}

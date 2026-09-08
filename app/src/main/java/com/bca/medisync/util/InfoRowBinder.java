package com.bca.medisync.util;

import android.view.View;
import android.widget.TextView;
import com.bca.medisync.R;

public class InfoRowBinder {
  public static class Row {
    final View view;
    final String label;
    final String value;

    public Row(View view, String label, String value) {
      this.view = view;
      this.label = label;
      this.value = value;
    }
  }

  public static void bind(Row... rows) {
    for (Row row : rows) {
      ((TextView) row.view.findViewById(R.id.txtRowLabel)).setText(row.label);
      ((TextView) row.view.findViewById(R.id.txtRowValue)).setText(row.value);
    }
  }

  public static void setValue(View row, String value) {
    ((TextView) row.findViewById(R.id.txtRowValue)).setText(value);
  }
}

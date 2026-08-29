package com.bca.medisync.util;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bca.medisync.R;

public class InfoRowBinder {

  public static void bind(View[] rows, int[] icons, String[] labels, String[] values) {
    for (int i = 0; i < rows.length; i++) {
      ((ImageView) rows[i].findViewById(R.id.imgRowIcon)).setImageResource(icons[i]);
      ((TextView) rows[i].findViewById(R.id.txtRowLabel)).setText(labels[i]);
      ((TextView) rows[i].findViewById(R.id.txtRowValue)).setText(values[i]);
      RoundedListStyler.apply(rows[i], i, rows.length);
    }
  }

  public static void setValue(View row, String value) {
    ((TextView) row.findViewById(R.id.txtRowValue)).setText(value);
  }
}

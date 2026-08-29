package com.bca.medisync.util;

import android.view.View;

public class EmptyState {
  public static void bind(View listView, View emptyView, boolean isEmpty) {
    listView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
  }
}

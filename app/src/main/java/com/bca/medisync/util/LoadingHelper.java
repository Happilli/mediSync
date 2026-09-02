package com.bca.medisync.util;

import android.view.View;

import com.bca.medisync.data.remote.ApiCallback;
import com.google.android.material.loadingindicator.LoadingIndicator;

public class LoadingHelper {

  private static final long MIN_VISIBLE_MS = 1000;

  public static void show(LoadingIndicator indicator) {
    indicator.setTag(System.currentTimeMillis());
    indicator.setVisibility(View.INVISIBLE);
    indicator.setVisibility(View.VISIBLE);
    indicator.show();
  }

  public static void hide(LoadingIndicator indicator) {
    hide(indicator, null);
  }

  public static void hide(LoadingIndicator indicator, Runnable after) {
    long startedAt = indicator.getTag() instanceof Long ? (Long) indicator.getTag() : 0L;
    long elapsed = System.currentTimeMillis() - startedAt;
    long remaining = Math.max(0, MIN_VISIBLE_MS - elapsed);

    indicator.postDelayed(
        () -> {
          indicator.hide();
          indicator.setVisibility(View.INVISIBLE);
          if (after != null) after.run();
        },
        remaining);
  }

  public static <T> ApiCallback.OnSuccess<T> wrapSuccess(
      LoadingIndicator indicator, View contentView, ApiCallback.OnSuccess<T> onSuccess) {
    return body ->
        hide(
            indicator,
            () -> {
              contentView.setVisibility(View.VISIBLE);
              onSuccess.run(body);
            });
  }

  public static ApiCallback.OnError wrapError(
      LoadingIndicator indicator, View contentView, ApiCallback.OnError onError) {
    return (code, msg) ->
        hide(
            indicator,
            () -> {
              contentView.setVisibility(View.VISIBLE);
              onError.run(code, msg);
            });
  }
}

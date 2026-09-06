package com.bca.medisync.util;

import android.content.Context;
import android.widget.Toast;
import com.bca.medisync.data.remote.ApiCallback;
import java.util.HashMap;
import java.util.Map;

public class ApiErrorHandler {
  private final Context context;
  private final Map<Integer, String> messages = new HashMap<>();
  private String fallback = "Something went wrong.";
  private Runnable onHandled;

  public static ApiErrorHandler with(Context context) {
    return new ApiErrorHandler(context);
  }

  private ApiErrorHandler(Context context) {
    this.context = context;
  }

  public ApiErrorHandler on(int code, String message) {
    messages.put(code, message);
    return this;
  }

  public ApiErrorHandler fallback(String message) {
    this.fallback = message;
    return this;
  }

  public ApiErrorHandler then(Runnable r) {
    this.onHandled = r;
    return this;
  }

  public ApiCallback.OnError build() {
    return (code, msg) -> {
      if (code == -1) {
        Toast.makeText(context, "Network error: " + msg, Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(context, messages.getOrDefault(code, fallback), Toast.LENGTH_SHORT).show();
      }
      if (onHandled != null) onHandled.run();
    };
  }
}

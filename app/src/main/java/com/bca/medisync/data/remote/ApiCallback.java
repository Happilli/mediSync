package com.bca.medisync.data.remote;

import android.content.Context;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiCallback {

  public interface OnSuccess<T> {
    void run(T body);
  }

  public interface OnError {
    void run(int code, String message);
  }

  public static <T> void handle(Call<T> call, Fragment f, OnSuccess<T> onOk, OnError onErr) {
    call.enqueue(
        new Callback<T>() {
          @Override
          public void onResponse(Call<T> call, Response<T> response) {
            if (!f.isAdded()) return;
            if (response.isSuccessful()) {
              onOk.run(response.body());
            } else {
              onErr.run(response.code(), null);
            }
          }

          @Override
          public void onFailure(Call<T> call, Throwable t) {
            if (!f.isAdded()) return;
            onErr.run(-1, t.getMessage());
          }
        });
  }

  public static <T> void handle(Call<T> call, OnSuccess<T> onOk, OnError onErr) {
    call.enqueue(
        new Callback<T>() {
          @Override
          public void onResponse(Call<T> call, Response<T> response) {
            if (response.isSuccessful()) {
              onOk.run(response.body());
            } else {
              onErr.run(response.code(), null);
            }
          }

          @Override
          public void onFailure(Call<T> call, Throwable t) {
            onErr.run(-1, t.getMessage());
          }
        });
  }

  public static OnError simpleError(Context ctx, String fallbackMessage) {
    return (code, msg) -> {
      if (code == -1) {
        Toast.makeText(ctx, "Network error: " + msg, Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(ctx, fallbackMessage, Toast.LENGTH_SHORT).show();
      }
    };
  }
}

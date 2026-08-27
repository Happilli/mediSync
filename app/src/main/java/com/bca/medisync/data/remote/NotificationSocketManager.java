package com.bca.medisync.data.remote;

import android.os.Handler;
import android.os.Looper;

import com.bca.medisync.data.remote.dto.notification.NotificationResponse;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class NotificationSocketManager {
  private static final long RECONNECT_DELAY_MS = 5000;

  private WebSocket webSocket;
  private final OkHttpClient client;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  private String currentToken;
  private Listener currentListener;
  private boolean manuallyDisconnected = false;

  public interface Listener {
    void onNotification(NotificationResponse notification);

    void onSocketClosed();
  }

  public NotificationSocketManager(OkHttpClient client) {
    this.client = client;
  }

  public void connect(String token, Listener listener) {
    this.currentToken = token;
    this.currentListener = listener;
    this.manuallyDisconnected = false;
    openSocket(token, listener);
  }

  private void openSocket(String token, Listener listener) {
    String wsUrl =
        ApiClient.BASE_URL.replaceFirst("^http", "ws") + "api/v1/ws/notifications?token=" + token;
    Request request = new Request.Builder().url(wsUrl).build();
    webSocket =
        client.newWebSocket(
            request,
            new WebSocketListener() {
              @Override
              public void onMessage(WebSocket ws, String text) {
                NotificationResponse n = new Gson().fromJson(text, NotificationResponse.class);
                mainHandler.post(() -> listener.onNotification(n));
              }

              @Override
              public void onFailure(WebSocket ws, Throwable t, Response r) {
                mainHandler.post(listener::onSocketClosed);
                scheduleReconnect();
              }

              @Override
              public void onClosed(WebSocket ws, int code, String reason) {
                mainHandler.post(listener::onSocketClosed);
                if (code != 1000) {
                  scheduleReconnect();
                }
              }
            });
  }

  private void scheduleReconnect() {
    if (manuallyDisconnected || currentToken == null || currentListener == null) return;
    mainHandler.postDelayed(
        () -> {
          if (!manuallyDisconnected && currentToken != null) {
            openSocket(currentToken, currentListener);
          }
        },
        RECONNECT_DELAY_MS);
  }

  public void disconnect() {
    manuallyDisconnected = true;
    if (webSocket != null) webSocket.close(1000, "bye");
  }
}

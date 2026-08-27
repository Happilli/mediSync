package com.bca.medisync.data.remote;

import com.bca.medisync.data.remote.dto.notification.NotificationResponse;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class NotificationSocketManager {
  private WebSocket webSocket;
  private final OkHttpClient client;

  public interface Listener {
    void onNotification(NotificationResponse notification);

    void onSocketClosed();
  }

  public NotificationSocketManager(OkHttpClient client) {
    this.client = client;
  }

  public void connect(String token, Listener listener) {
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
                listener.onNotification(n);
              }

              @Override
              public void onFailure(WebSocket ws, Throwable t, Response r) {
                listener.onSocketClosed();
              }

              @Override
              public void onClosed(WebSocket ws, int code, String reason) {
                listener.onSocketClosed();
              }
            });
  }

  public void disconnect() {
    if (webSocket != null) webSocket.close(1000, "bye");
  }
}

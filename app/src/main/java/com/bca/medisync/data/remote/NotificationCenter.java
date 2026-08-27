package com.bca.medisync.data.remote;

import android.os.Handler;
import android.os.Looper;

import com.bca.medisync.data.remote.dto.notification.NotificationResponse;

import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationCenter {

  public interface Listener {
    void onNotificationReceived(NotificationResponse notification);
  }

  private static NotificationCenter instance;

  private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  private NotificationCenter() {}

  public static NotificationCenter get() {
    if (instance == null) {
      instance = new NotificationCenter();
    }
    return instance;
  }

  public void register(Listener listener) {
    listeners.addIfAbsent(listener);
  }

  public void unregister(Listener listener) {
    listeners.remove(listener);
  }

  public void broadcast(NotificationResponse notification) {
    mainHandler.post(
        () -> {
          for (Listener listener : listeners) {
            listener.onNotificationReceived(notification);
          }
        });
  }
}

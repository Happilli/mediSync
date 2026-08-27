package com.bca.medisync.data.remote;

public class NotificationSocketHolder {
  private static NotificationSocketManager instance;

  public static NotificationSocketManager get() {
    if (instance == null) {
      instance = new NotificationSocketManager(ApiClient.getOkHttpClient());
    }
    return instance;
  }

  public static void reset() {
    instance = null;
  }
}

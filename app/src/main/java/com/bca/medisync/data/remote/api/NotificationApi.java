package com.bca.medisync.data.remote.api;

import com.bca.medisync.data.remote.dto.notification.NotificationResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationApi {
  @GET("api/v1/notifications/me")
  Call<List<NotificationResponse>> getMyNotifications(@Query("unread_only") boolean unreadOnly);

  @GET("api/v1/notifications/unread-count")
  Call<Map<String, Integer>> getUnreadCount();

  @PATCH("api/v1/notifications/{notification_id}/read")
  Call<NotificationResponse> markAsRead(@Path("notification_id") int notificationId);

  @PATCH("api/v1/notifications/read-all")
  Call<Map<String, String>> markAllRead();
}

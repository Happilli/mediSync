package com.bca.medisync.patient;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.SimpleListAdapter;
import com.bca.medisync.data.model.Notification;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.NotificationCenter;
import com.bca.medisync.data.remote.api.NotificationApi;
import com.bca.medisync.data.remote.dto.notification.NotificationResponse;
import com.bca.medisync.util.DateTimeUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity
    implements NotificationCenter.Listener {

  private MaterialToolbar toolbar;
  private RecyclerView rvNotifications;
  private android.widget.TextView txtEmpty;
  private MaterialButton btnMarkAllRead;
  private SimpleListAdapter<Notification> adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_notifications);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.main),
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });
    initViews();
    setupToolbar();
    setupRecyclerView();
    setupListeners();
    loadNotifications();
  }

  @Override
  protected void onResume() {
    super.onResume();
    NotificationCenter.get().register(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    NotificationCenter.get().unregister(this);
  }

  @Override
  public void onNotificationReceived(NotificationResponse notification) {
    adapter.prependItem(mapToNotification(notification));
    txtEmpty.setVisibility(View.GONE);
    rvNotifications.setVisibility(View.VISIBLE);
  }

  private void initViews() {
    toolbar = findViewById(R.id.toolbar);
    rvNotifications = findViewById(R.id.rvNotifications);
    txtEmpty = findViewById(R.id.txtEmpty);
    btnMarkAllRead = findViewById(R.id.btnMarkAllRead);
  }

  private void setupToolbar() {
    toolbar.setNavigationOnClickListener(v -> finish());
  }

  private void setupRecyclerView() {
    adapter =
        new SimpleListAdapter<>(
            R.layout.item_notification,
            new ArrayList<>(),
            (itemView, notification, pos) -> {
              ((android.widget.TextView) itemView.findViewById(R.id.txtNotifTitle))
                  .setText(notification.getTitle());
              ((android.widget.TextView) itemView.findViewById(R.id.txtNotifMessage))
                  .setText(notification.getMessage());
              ((android.widget.TextView) itemView.findViewById(R.id.txtNotifTime))
                  .setText(DateTimeUtils.format(notification.getCreatedAt(), "dd MMM, hh:mm a"));

              View unreadDot = itemView.findViewById(R.id.unreadDot);
              if (notification.isRead()) {
                unreadDot.setVisibility(View.INVISIBLE);
                itemView.setAlpha(0.6f);
              } else {
                unreadDot.setVisibility(View.VISIBLE);
                itemView.setAlpha(1f);
              }
            },
            notification -> {
              if (!notification.isRead()) {
                markAsRead(notification.getId());
              }
            });
    rvNotifications.setLayoutManager(new LinearLayoutManager(this));
    rvNotifications.setAdapter(adapter);
  }

  private void setupListeners() {
    btnMarkAllRead.setOnClickListener(v -> markAllRead());
  }

  private void loadNotifications() {
    NotificationApi api = ApiClient.getRetrofit().create(NotificationApi.class);
    ApiCallback.handle(
        api.getMyNotifications(false),
        body -> {
          List<Notification> list = new ArrayList<>();
          for (NotificationResponse r : body) {
            list.add(mapToNotification(r));
          }
          adapter.updateData(list);
          txtEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
          rvNotifications.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
        },
        (code, msg) ->
            Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show());
  }

  private void markAsRead(int notificationId) {
    NotificationApi api = ApiClient.getRetrofit().create(NotificationApi.class);
    ApiCallback.handle(
        api.markAsRead(notificationId),
        body -> loadNotifications(),
        (code, msg) -> loadNotifications());
  }

  private void markAllRead() {
    NotificationApi api = ApiClient.getRetrofit().create(NotificationApi.class);
    ApiCallback.handle(
        api.markAllRead(),
        body -> loadNotifications(),
        (code, msg) -> Toast.makeText(this, "Network error: " + msg, Toast.LENGTH_LONG).show());
  }

  private Notification mapToNotification(NotificationResponse r) {
    return new Notification(
        r.getId(),
        r.getType(),
        r.getTitle(),
        r.getMessage(),
        r.getRelated_id(),
        r.getRelated_type(),
        r.isIs_read(),
        r.getCreated_at());
  }
}

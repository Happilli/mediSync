package com.bca.medisync.patient;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.NotificationAdapter;
import com.bca.medisync.data.model.Notification;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.NotificationCenter;
import com.bca.medisync.data.remote.api.NotificationApi;
import com.bca.medisync.data.remote.dto.notification.NotificationResponse;
import com.bca.medisync.util.EmptyState;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity
    implements NotificationCenter.Listener {

  private MaterialToolbar toolbar;
  private RecyclerView rvNotifications;
  private android.widget.TextView txtEmpty;
  private MaterialButton btnMarkAllRead;
  private MaterialButtonToggleGroup toggleGroup;
  private NotificationAdapter adapter;

  private List<Notification> allNotifications = new ArrayList<>();
  private boolean showUnreadOnly = true;

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
    loadNotifications();
  }

  private void initViews() {
    toolbar = findViewById(R.id.toolbar);
    rvNotifications = findViewById(R.id.rvNotifications);
    txtEmpty = findViewById(R.id.txtEmpty);
    btnMarkAllRead = findViewById(R.id.btnMarkAllRead);
    toggleGroup = findViewById(R.id.toggleGroup);
  }

  private void setupToolbar() {
    toolbar.setNavigationOnClickListener(v -> finish());
  }

  private void setupRecyclerView() {
    adapter =
        new NotificationAdapter(
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
    toggleGroup.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          if (!isChecked) return;
          showUnreadOnly = checkedId == R.id.btnUnread;
          applyFilter();
        });
  }

  private void loadNotifications() {
    NotificationApi api = ApiClient.api(NotificationApi.class);
    ApiCallback.handle(
        api.getMyNotifications(false),
        body -> {
          allNotifications = new ArrayList<>();
          for (NotificationResponse r : body) {
            allNotifications.add(mapToNotification(r));
          }
          applyFilter();
        },
        (code, msg) ->
            Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show());
  }

  private void applyFilter() {
    List<Notification> filtered = new ArrayList<>();
    for (Notification n : allNotifications) {
      if (!showUnreadOnly || !n.isRead()) filtered.add(n);
    }
    adapter.submitList(filtered);
    EmptyState.bind(rvNotifications, txtEmpty, filtered.isEmpty());
    txtEmpty.setText(showUnreadOnly ? "No unread notifications" : "No notifications yet");
  }

  private void markAsRead(int notificationId) {
    NotificationApi api = ApiClient.api(NotificationApi.class);
    ApiCallback.handle(
        api.markAsRead(notificationId),
        body -> loadNotifications(),
        (code, msg) -> loadNotifications());
  }

  private void markAllRead() {
    NotificationApi api = ApiClient.api(NotificationApi.class);
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

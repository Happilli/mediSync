package com.bca.medisync.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bca.medisync.R;
import com.bca.medisync.adapter.GroupedListAdapter;
import com.bca.medisync.data.model.Notification;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.NotificationCenter;
import com.bca.medisync.data.remote.api.NotificationApi;
import com.bca.medisync.data.remote.dto.notification.NotificationResponse;
import com.bca.medisync.databinding.ActivityNotificationsBinding;
import com.bca.medisync.databinding.ItemNotificationBinding;
import com.bca.medisync.util.DateTimeUtils;
import com.bca.medisync.util.EmptyState;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity
    implements NotificationCenter.Listener {
  private ActivityNotificationsBinding binding;
  private GroupedListAdapter<Notification, ItemNotificationBinding> adapter;
  private List<Notification> allNotifications = new ArrayList<>();
  private boolean showUnreadOnly = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    ViewCompat.setOnApplyWindowInsetsListener(
        binding.main,
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });
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

  private void setupToolbar() {
    binding.toolbar.setNavigationOnClickListener(v -> finish());
  }

  private void setupRecyclerView() {
    adapter =
        new GroupedListAdapter<>(
            ItemNotificationBinding::inflate,
            n -> n.getType() == null ? "Other" : capitalize(n.getType()),
            this::bindNotificationRow,
            n -> {
              if (!n.isRead()) markAsRead(n.getId());
              handleNotificationClick(n);
            });
    binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
    binding.rvNotifications.setAdapter(adapter);
  }

  private void handleNotificationClick(Notification n) {
    if (n.getRelatedId() == null) return;
    if ("appointment_completed".equals(n.getType())) {
      Bundle args = new Bundle();
      args.putInt("appointment_id", n.getRelatedId());
      ConsultationDetailFragment fragment = new ConsultationDetailFragment();
      fragment.setArguments(args);
      Intent intent = new Intent(this, MainTabActivity.class);
      intent.putExtra("open_fragment", "consultation_detail");
      intent.putExtra("appointment_id", n.getRelatedId());
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
      finish();
    }
  }

  private void bindNotificationRow(
      ItemNotificationBinding rowBinding, Notification n, int posInGroup, int groupSize) {
    rowBinding.txtNotifTitle.setText(n.getTitle());
    rowBinding.txtNotifMessage.setText(n.getMessage());
    rowBinding.txtNotifTime.setText(DateTimeUtils.format(n.getCreatedAt(), "dd MMM, hh:mm a"));
    if (n.isRead()) {
      rowBinding.unreadDot.setVisibility(View.INVISIBLE);
      rowBinding.getRoot().setAlpha(0.6f);
    } else {
      rowBinding.unreadDot.setVisibility(View.VISIBLE);
      rowBinding.getRoot().setAlpha(1f);
    }
  }

  private String capitalize(String s) {
    if (s.isEmpty()) return s;
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }

  private void setupListeners() {
    binding.btnMarkAllRead.setOnClickListener(v -> markAllRead());
    binding.toggleGroup.addOnButtonCheckedListener(
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
        ApiCallback.simpleError(this, "Failed to load notifications"));
  }

  private void applyFilter() {
    List<Notification> filtered = new ArrayList<>();
    for (Notification n : allNotifications) {
      if (!showUnreadOnly || !n.isRead()) filtered.add(n);
    }
    adapter.submitList(filtered);
    EmptyState.bind(binding.rvNotifications, binding.txtEmpty, filtered.isEmpty());
    binding.txtEmpty.setText(showUnreadOnly ? "No unread notifications" : "No notifications yet");
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
        (code, msg) ->
            android.widget.Toast.makeText(
                    this, "Network error: " + msg, android.widget.Toast.LENGTH_LONG)
                .show());
  }

  private Notification mapToNotification(NotificationResponse r) {
    return new Notification(
        r.getId(),
        r.getType(),
        r.getTitle(),
        r.getMessage(),
        r.getRelated_id(),
        r.getRelated_type(),
        r.is_read(),
        r.getCreated_at());
  }
}

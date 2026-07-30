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
import com.bca.medisync.adapter.NotificationAdapter;
import com.bca.medisync.data.model.Notification;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.NotificationApi;
import com.bca.medisync.data.remote.dto.notification.NotificationResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity {

  private MaterialToolbar toolbar;
  private RecyclerView rvNotifications;
  private android.widget.TextView txtEmpty;
  private MaterialButton btnMarkAllRead;
  private NotificationAdapter adapter;

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
        new NotificationAdapter(
            this,
            new ArrayList<>(),
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
    api.getMyNotifications(false)
        .enqueue(
            new Callback<List<NotificationResponse>>() {
              @Override
              public void onResponse(
                  Call<List<NotificationResponse>> call,
                  Response<List<NotificationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                  List<Notification> list = new ArrayList<>();
                  for (NotificationResponse r : response.body()) {
                    list.add(mapToNotification(r));
                  }
                  adapter.updateData(list);
                  txtEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                  rvNotifications.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                } else {
                  Toast.makeText(
                          NotificationsActivity.this,
                          "Failed to load notifications",
                          Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<List<NotificationResponse>> call, Throwable t) {
                Toast.makeText(
                        NotificationsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG)
                    .show();
              }
            });
  }

  private void markAsRead(int notificationId) {
    NotificationApi api = ApiClient.getRetrofit().create(NotificationApi.class);
    api.markAsRead(notificationId)
        .enqueue(
            new Callback<NotificationResponse>() {
              @Override
              public void onResponse(
                  Call<NotificationResponse> call, Response<NotificationResponse> response) {
                loadNotifications();
              }

              @Override
              public void onFailure(Call<NotificationResponse> call, Throwable t) {}
            });
  }

  private void markAllRead() {
    NotificationApi api = ApiClient.getRetrofit().create(NotificationApi.class);
    api.markAllRead()
        .enqueue(
            new Callback<Map<String, String>>() {
              @Override
              public void onResponse(
                  Call<Map<String, String>> call, Response<Map<String, String>> response) {
                loadNotifications();
              }

              @Override
              public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(
                        NotificationsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG)
                    .show();
              }
            });
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

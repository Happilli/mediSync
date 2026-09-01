package com.bca.medisync.util;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.bca.medisync.R;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.NotificationApi;
import com.google.android.material.button.MaterialButton;

public class NotificationBadgeHelper {

  public static void refresh(Fragment fragment, MaterialButton btn) {
    NotificationApi api = ApiClient.api(NotificationApi.class);
    ApiCallback.handle(
        api.getUnreadCount(),
        fragment,
        body -> {
          Integer count = body.get("unread_count");
          if (count != null && count > 0) showUnread(fragment, btn);
          else showRead(fragment, btn);
        },
        (code, msg) -> {});
  }

  public static void showUnread(Fragment fragment, MaterialButton btn) {
    btn.setIcon(ContextCompat.getDrawable(fragment.requireContext(), R.drawable.notification_dot));
  }

  public static void showRead(Fragment fragment, MaterialButton btn) {
    btn.setIcon(ContextCompat.getDrawable(fragment.requireContext(), R.drawable.notification));
  }
}

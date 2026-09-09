package com.bca.medisync.util;

import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import com.bca.medisync.R;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.NotificationApi;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;

@OptIn(markerClass = ExperimentalBadgeUtils.class)
public class NotificationBadgeHelper {
  public static void refresh(Fragment fragment, ImageView icon, FrameLayout container) {
    NotificationApi api = ApiClient.api(NotificationApi.class);
    ApiCallback.handle(
        api.getUnreadCount(),
        fragment,
        body -> {
          Integer count = body.get("unread_count");
          if (count != null && count > 0) showUnread(fragment, icon, container, count);
          else showRead(icon);
        },
        (code, msg) -> {});
  }

  public static void showUnread(
      Fragment fragment, ImageView icon, FrameLayout container, int count) {
    BadgeDrawable badge = getOrCreateBadge(fragment, icon, container);
    badge.setNumber(count);
    badge.setMaxCharacterCount(3);
    badge.setBackgroundColor(fragment.requireContext().getColor(R.color.error));
    badge.setBadgeTextColor(fragment.requireContext().getColor(R.color.on_error));
    badge.setVisible(true);
  }

  public static void showRead(ImageView icon) {
    Object tag = icon.getTag();
    if (tag instanceof BadgeDrawable) {
      BadgeDrawable badge = (BadgeDrawable) tag;
      badge.setVisible(false);
      BadgeUtils.detachBadgeDrawable(badge, icon);
      icon.setTag(null);
    }
  }

  private static BadgeDrawable getOrCreateBadge(
      Fragment fragment, ImageView icon, FrameLayout container) {
    Object tag = icon.getTag();
    if (tag instanceof BadgeDrawable) return (BadgeDrawable) tag;
    BadgeDrawable badge = BadgeDrawable.create(fragment.requireContext());
    icon.setTag(badge);
    BadgeUtils.attachBadgeDrawable(badge, icon, container);
    return badge;
  }
}

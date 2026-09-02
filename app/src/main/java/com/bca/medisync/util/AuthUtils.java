package com.bca.medisync.util;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.bca.medisync.MainActivity;
import com.bca.medisync.data.local.SessionManager;
import com.bca.medisync.data.remote.NotificationSocketHolder;

public class AuthUtils {
  public static void logout(AppCompatActivity activity) {
    new SessionManager(activity).clearSession();
    NotificationSocketHolder.get().disconnect();
    NotificationSocketHolder.reset();

    Intent intent = new Intent(activity, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    activity.startActivity(intent);
    activity.finish();
  }
}

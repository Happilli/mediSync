package com.bca.medisync;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bca.medisync.data.local.SessionManager;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.NotificationCenter;
import com.bca.medisync.data.remote.NotificationSocketHolder;
import com.bca.medisync.data.remote.NotificationSocketManager;
import com.bca.medisync.data.remote.api.AuthApi;
import com.bca.medisync.data.remote.dto.login.LoginRequest;
import com.bca.medisync.data.remote.dto.notification.NotificationResponse;
import com.bca.medisync.doctor.DoctorTabActivity;
import com.bca.medisync.patient.MainTabActivity;
import com.bca.medisync.patient.RegisterActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {
  private MaterialButton btnLogin;
  private ChipGroup chipGroupRole;
  private TextInputEditText etEmail, etPassword;
  private SessionManager sessionManager;
  private TextView goToRegister;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    sessionManager = new SessionManager(this);

    if (sessionManager.isLoggedIn()) {
      Intent intent;
      if ("doctor".equalsIgnoreCase(sessionManager.getRole())) {
        intent = new Intent(MainActivity.this, DoctorTabActivity.class);
      } else {
        intent = new Intent(MainActivity.this, MainTabActivity.class);
      }
      NotificationSocketHolder.get().connect(sessionManager.getToken(), globalNotificationListener);
      startActivity(intent);
      finish();
      return;
    }

    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_login);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.mainStuff),
        (v, i) -> {
          Insets systemBars = i.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return i;
        });

    sessionManager = new SessionManager(this);
    btnLogin = findViewById(R.id.btnLogin);
    chipGroupRole = findViewById(R.id.chipGroupRole);
    etEmail = findViewById(R.id.etEmail);
    etPassword = findViewById(R.id.etPassword);
    goToRegister = findViewById(R.id.GoToRegister);
    chipGroupRole.setOnCheckedStateChangeListener((group, checkedIds) -> updateRegisterLabel());
    updateRegisterLabel();
    btnLogin.setOnClickListener(
        v -> {
          attemptLogin();
        });
    findViewById(R.id.txtForgotPassword)
        .setOnClickListener(
            v -> startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class)));
  }

  private final NotificationSocketManager.Listener globalNotificationListener =
      new NotificationSocketManager.Listener() {
        @Override
        public void onSocketClosed() {}

        @Override
        public void onNotification(NotificationResponse notification) {
          NotificationCenter.get().broadcast(notification);
          showSystemNotification(notification);
        }
      };

  private void updateRegisterLabel() {
    boolean isDoctor = chipGroupRole.getCheckedChipId() == R.id.chipDoctor;
    if (isDoctor) {
      goToRegister.setText("Only hospitals can register\ndoctor accounts");
      goToRegister.setOnClickListener(null);
      goToRegister.setClickable(false);
    } else {
      goToRegister.setText("No Account?\nRegister");
      goToRegister.setOnClickListener(
          v -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));
      goToRegister.setClickable(true);
    }
  }

  @SuppressLint("MissingPermission")
  private void showSystemNotification(NotificationResponse n) {
    if (!sessionManager.isNotificationsEnabled()) return;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
      return;
    }

    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    if (manager == null) return;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel =
          new NotificationChannel(
              "GENERAL_CHANNEL", "General Alerts", NotificationManager.IMPORTANCE_DEFAULT);
      manager.createNotificationChannel(channel);
    }

    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(this, "GENERAL_CHANNEL")
            .setSmallIcon(R.drawable.ic_nav_medicine)
            .setContentTitle(n.getTitle())
            .setContentText(n.getMessage())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    manager.notify(n.getId(), builder.build());
  }

  public void attemptLogin() {
    String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
    String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

    if (email.isEmpty()) {
      etEmail.setError("Email is required");
      return;
    }
    if (password.isEmpty()) {
      etPassword.setError("Password is required");
      return;
    }
    String selectedRole =
        chipGroupRole.getCheckedChipId() == R.id.chipDoctor ? "doctor" : "patient";
    btnLogin.setEnabled(false);

    AuthApi authApi = ApiClient.getRetrofit().create(AuthApi.class);
    ApiCallback.handle(
        authApi.login(new LoginRequest(email, password)),
        body -> {
          btnLogin.setEnabled(true);
          if (!selectedRole.equalsIgnoreCase(body.getRole())) {
            Toast.makeText(
                    MainActivity.this,
                    "This account is not registered as " + selectedRole,
                    Toast.LENGTH_LONG)
                .show();
            return;
          }
          sessionManager.saveSession(body.getAccess_token(), body.getRole(), body.getEmail());
          NotificationSocketHolder.get()
              .connect(sessionManager.getToken(), globalNotificationListener);

          Intent intent;
          if ("doctor".equalsIgnoreCase(body.getRole())) {
            intent = new Intent(MainActivity.this, DoctorTabActivity.class);
          } else {
            intent = new Intent(MainActivity.this, MainTabActivity.class);
          }
          startActivity(intent);
          finish();
        },
        (code, msg) -> {
          btnLogin.setEnabled(true);
          if (code == -1) {
            Toast.makeText(MainActivity.this, "network error: " + msg, Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(
                    MainActivity.this, "login failed: invalid credentials", Toast.LENGTH_SHORT)
                .show();
          }
        });
  }
}

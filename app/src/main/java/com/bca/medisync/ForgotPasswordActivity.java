package com.bca.medisync;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.AuthApi;
import com.bca.medisync.data.remote.dto.auth.ForgotPasswordCheckRequest;
import com.bca.medisync.data.remote.dto.auth.ForgotPasswordVerifyRequest;
import com.bca.medisync.databinding.ActivityForgotPasswordBinding;
import com.bca.medisync.util.ApiErrorHandler;
import com.bca.medisync.util.ViewUtils;

public class ForgotPasswordActivity extends AppCompatActivity {
  private ActivityForgotPasswordBinding binding;
  private String verifiedEmail;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    ViewCompat.setOnApplyWindowInsetsListener(
        binding.mainStuff,
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });
    setupListeners();
  }

  private void setupListeners() {
    binding.btnCheckEmail.setOnClickListener(v -> attemptCheckEmail());
    binding.btnResetPassword.setOnClickListener(v -> attemptResetPassword());
    binding.txtBackToLogin.setOnClickListener(v -> finish());
  }

  private void attemptCheckEmail() {
    String email = ViewUtils.textOf(binding.etEmail);
    if (email.isEmpty()) {
      binding.etEmail.setError("Email is required");
      return;
    }
    binding.btnCheckEmail.setEnabled(false);
    AuthApi api = ApiClient.api(AuthApi.class);
    ApiCallback.handle(
        api.checkForgotPassword(new ForgotPasswordCheckRequest(email)),
        body -> {
          binding.btnCheckEmail.setEnabled(true);
          verifiedEmail = email;
          binding.txtSecurityQuestion.setText(body.getQuestion());
          binding.stepEmail.setVisibility(View.GONE);
          binding.stepReset.setVisibility(View.VISIBLE);
          binding.txtSubtitle.setText("Answer your security question to reset your password");
        },
        (code, msg) -> {
          binding.btnCheckEmail.setEnabled(true);
          if (code == 404) {
            ApiErrorHandler.with(this)
                .on(404, "No account found with this email.")
                .on(400, "This account has no security answer set up. Contact support.")
                .fallback("Something went wrong.")
                .build()
                .run(code, msg);
          }
        });
  }

  private void attemptResetPassword() {
    String answer = ViewUtils.textOf(binding.etSecurityAnswer);
    String newPassword = ViewUtils.textOf(binding.etNewPassword);
    String confirmPassword = ViewUtils.textOf(binding.etConfirmPassword);
    if (answer.isEmpty()) {
      binding.etSecurityAnswer.setError("Answer is required");
      return;
    }
    if (newPassword.isEmpty()) {
      binding.etNewPassword.setError("New password is required");
      return;
    }
    if (!newPassword.equals(confirmPassword)) {
      binding.etConfirmPassword.setError("Passwords do not match");
      return;
    }
    if (verifiedEmail == null) {
      Toast.makeText(this, "Something went wrong, please start over.", Toast.LENGTH_SHORT).show();
      return;
    }
    binding.btnResetPassword.setEnabled(false);
    AuthApi api = ApiClient.api(AuthApi.class);
    ApiCallback.handle(
        api.verifyForgotPassword(
            new ForgotPasswordVerifyRequest(verifiedEmail, answer, newPassword)),
        body -> {
          binding.btnResetPassword.setEnabled(true);
          Toast.makeText(this, "Password reset! You can now log in.", Toast.LENGTH_LONG).show();
          finish();
        },
        (code, msg) -> {
          binding.btnResetPassword.setEnabled(true);
          ApiErrorHandler.with(this)
              .on(401, "That answer isn't correct.")
              .fallback("Failed to reset password.")
              .build()
              .run(code, msg);
        });
  }
}

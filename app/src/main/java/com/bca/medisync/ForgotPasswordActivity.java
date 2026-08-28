package com.bca.medisync;

import android.os.Bundle;
import android.widget.TextView;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

  private TextInputEditText etEmail, etSecurityAnswer, etNewPassword, etConfirmPassword;
  private MaterialButton btnCheckEmail, btnResetPassword;
  private android.view.View stepEmail, stepReset;
  private TextView txtSubtitle, txtSecurityQuestion, txtBackToLogin;

  private String verifiedEmail;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_forgot_password);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.mainStuff),
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });
    initViews();
    setupListeners();
  }

  private void initViews() {
    etEmail = findViewById(R.id.etEmail);
    etSecurityAnswer = findViewById(R.id.etSecurityAnswer);
    etNewPassword = findViewById(R.id.etNewPassword);
    etConfirmPassword = findViewById(R.id.etConfirmPassword);
    btnCheckEmail = findViewById(R.id.btnCheckEmail);
    btnResetPassword = findViewById(R.id.btnResetPassword);
    stepEmail = findViewById(R.id.stepEmail);
    stepReset = findViewById(R.id.stepReset);
    txtSubtitle = findViewById(R.id.txtSubtitle);
    txtSecurityQuestion = findViewById(R.id.txtSecurityQuestion);
    txtBackToLogin = findViewById(R.id.txtBackToLogin);
  }

  private void setupListeners() {
    btnCheckEmail.setOnClickListener(v -> attemptCheckEmail());
    btnResetPassword.setOnClickListener(v -> attemptResetPassword());
    txtBackToLogin.setOnClickListener(v -> finish());
  }

  private void attemptCheckEmail() {
    String email = textOf(etEmail);
    if (email.isEmpty()) {
      etEmail.setError("Email is required");
      return;
    }

    btnCheckEmail.setEnabled(false);
    AuthApi api = ApiClient.getRetrofit().create(AuthApi.class);
    ApiCallback.handle(
        api.checkForgotPassword(new ForgotPasswordCheckRequest(email)),
        body -> {
          btnCheckEmail.setEnabled(true);
          verifiedEmail = email;
          txtSecurityQuestion.setText(body.getQuestion());
          stepEmail.setVisibility(android.view.View.GONE);
          stepReset.setVisibility(android.view.View.VISIBLE);
          txtSubtitle.setText("Answer your security question to reset your password");
        },
        (code, msg) -> {
          btnCheckEmail.setEnabled(true);
          if (code == 404) {
            Toast.makeText(this, "No account found with this email.", Toast.LENGTH_LONG).show();
          } else if (code == 400) {
            Toast.makeText(
                    this,
                    "This account has no security answer set up. Contact support.",
                    Toast.LENGTH_LONG)
                .show();
          } else if (code == -1) {
            Toast.makeText(this, "Network error: " + msg, Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show();
          }
        });
  }

  private void attemptResetPassword() {
    String answer = textOf(etSecurityAnswer);
    String newPassword = textOf(etNewPassword);
    String confirmPassword = textOf(etConfirmPassword);

    if (answer.isEmpty()) {
      etSecurityAnswer.setError("Answer is required");
      return;
    }
    if (newPassword.isEmpty()) {
      etNewPassword.setError("New password is required");
      return;
    }
    if (!newPassword.equals(confirmPassword)) {
      etConfirmPassword.setError("Passwords do not match");
      return;
    }
    if (verifiedEmail == null) {
      Toast.makeText(this, "Something went wrong, please start over.", Toast.LENGTH_SHORT).show();
      return;
    }

    btnResetPassword.setEnabled(false);
    AuthApi api = ApiClient.getRetrofit().create(AuthApi.class);
    ApiCallback.handle(
        api.verifyForgotPassword(
            new ForgotPasswordVerifyRequest(verifiedEmail, answer, newPassword)),
        body -> {
          btnResetPassword.setEnabled(true);
          Toast.makeText(this, "Password reset! You can now log in.", Toast.LENGTH_LONG).show();
          finish();
        },
        (code, msg) -> {
          btnResetPassword.setEnabled(true);
          if (code == 401) {
            Toast.makeText(this, "That answer isn't correct.", Toast.LENGTH_LONG).show();
          } else if (code == -1) {
            Toast.makeText(this, "Network error: " + msg, Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(this, "Failed to reset password.", Toast.LENGTH_SHORT).show();
          }
        });
  }

  private String textOf(TextInputEditText et) {
    return et.getText() != null ? et.getText().toString().trim() : "";
  }
}

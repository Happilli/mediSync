package com.bca.medisync.patient;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bca.medisync.R;
import com.google.android.material.button.MaterialButton;

public class HospitalDetailActivity extends AppCompatActivity {
  private TextView txtName, txtRating, txtDescription, txtAddress;
  private MaterialButton btnBack, btnCall, btnWebsite, btnDirection, btnSeeDoctors;

  private String hospitalName;
  private String hospitalPhone;
  private String hospitalWebsite;
  private String hospitalAddress;
  String hospitalDescription;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_hospital_detail);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.main),
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });
    initViews();
    loadData();
    setupListeners();
  }

  private void initViews() {
    txtName = findViewById(R.id.txtHospitalName);
    txtRating = findViewById(R.id.txtRating);
    txtDescription = findViewById(R.id.txtDescription);
    txtAddress = findViewById(R.id.txtAddress);
    btnBack = findViewById(R.id.btnBack);
    btnCall = findViewById(R.id.btnCall);
    btnWebsite = findViewById(R.id.btnWebsite);
    btnDirection = findViewById(R.id.btnDirection);
    btnSeeDoctors = findViewById(R.id.btnSeeDoctors);
  }

  private void loadData() {
    hospitalName = getIntent().getStringExtra("hospital_name");
    hospitalPhone = getIntent().getStringExtra("hospital_phone");
    hospitalWebsite = getIntent().getStringExtra("hospital_website");
    hospitalAddress = getIntent().getStringExtra("hospital_address");
    hospitalDescription = getIntent().getStringExtra("hospital_description");
    double hospitalRating = getIntent().getDoubleExtra("hospital_rating", 0.0);

    txtName.setText(hospitalName);
    txtRating.setText("+ " + hospitalRating);
    txtDescription.setText(hospitalDescription);
    txtAddress.setText(hospitalAddress);
  }

  private void setupListeners() {
    btnBack.setOnClickListener(v -> finish());
    btnCall.setOnClickListener(
        v -> {
          Intent intent = new Intent(Intent.ACTION_DIAL);
          intent.setData(Uri.parse("tel:" + hospitalPhone));
          startActivity(intent);
        });

    btnWebsite.setOnClickListener(
        v -> {
          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(hospitalWebsite));
          startActivity(intent);
        });

    btnDirection.setOnClickListener(
        v -> {
          Intent intent =
              new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + hospitalAddress));
          startActivity(intent);
        });
    btnSeeDoctors.setOnClickListener(
        v -> {
          Intent intent = new Intent(HospitalDetailActivity.this, DoctorActivity.class);
          intent.putExtra("hospital_id", getIntent().getStringExtra("hospital_id"));
          startActivity(intent);
        });
  }
}

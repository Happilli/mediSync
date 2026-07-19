package com.bca.medisync.patient;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.HospitalAdapter;
import com.bca.medisync.data.model.Hospital;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.HospitalApi;
import com.bca.medisync.data.remote.dto.hospital.HospitalResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HospitalActivity extends AppCompatActivity {
  private RecyclerView rvHospitals;
  private MaterialToolbar toolbar;
  private HospitalAdapter adapter;
  private TextInputEditText etSearch;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_hospital);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.main),
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });
    initView();
    setupToolbar();
    setupRecycleView();
    setupSearch();
    loadHospitals(null);
  }

  private void initView() {
    rvHospitals = findViewById(R.id.rvHospitals);
    toolbar = findViewById(R.id.toolbar);
    etSearch = findViewById(R.id.etSearch);
  }

  private void setupToolbar() {
    toolbar.setNavigationOnClickListener(
        v -> {
          finish();
        });
  }

  private void setupRecycleView() {
    adapter =
        new HospitalAdapter(
            this,
            new ArrayList<>(),
            hospital -> {
              Intent intent = new Intent(HospitalActivity.this, HospitalDetailActivity.class);
              intent.putExtra("hospital_id", hospital.getId());
              intent.putExtra("hospital_name", hospital.getName());
              intent.putExtra("hospital_address", hospital.getAddress());
              intent.putExtra("hospital_phone", hospital.getPhone());
              intent.putExtra("hospital_website", hospital.getWebsite());
              intent.putExtra("hospital_description", hospital.getDescription());
              intent.putExtra("hospita_rating", hospital.getRating());
              startActivity(intent);
            });
    rvHospitals.setLayoutManager(new LinearLayoutManager(this));
    rvHospitals.setAdapter(adapter);
  }

  private void loadHospitals(String search) {
    HospitalApi api = ApiClient.getRetrofit().create(HospitalApi.class);
    api.getHospitals(search)
        .enqueue(
            new Callback<List<HospitalResponse>>() {
              @Override
              public void onResponse(
                  Call<List<HospitalResponse>> call, Response<List<HospitalResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                  List<Hospital> hospitals = new ArrayList<>();
                  for (HospitalResponse r : response.body()) {
                    hospitals.add(mapToHospital(r));
                  }
                  adapter.updateData(hospitals);
                } else {
                  Toast.makeText(
                          HospitalActivity.this, "Failed to load hosptials..", Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<List<HospitalResponse>> call, Throwable t) {
                Toast.makeText(
                        HospitalActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG)
                    .show();
              }
            });
  }

  private Hospital mapToHospital(HospitalResponse r) {
    return new Hospital(
        String.valueOf(r.getId()),
        r.getName(),
        r.getAddress(),
        r.getPhone(),
        r.getWebsite(),
        r.getDescription(),
        0.0,
        r.getImage_url());
  }

  private void setupSearch() {
    etSearch.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            adapter.filter(s.toString());
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });
  }
}

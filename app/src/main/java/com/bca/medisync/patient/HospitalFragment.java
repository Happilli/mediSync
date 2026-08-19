package com.bca.medisync.patient;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class HospitalFragment extends Fragment {
  private RecyclerView rvHospitals;
  private MaterialToolbar toolbar;
  private HospitalAdapter adapter;
  private TextInputEditText etSearch;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.activity_hospital, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initView(view);
    setupToolbar();
    setupRecycleView();
    setupSearch();
    loadHospitals(null);
  }

  private void initView(View view) {
    rvHospitals = view.findViewById(R.id.rvHospitals);
    toolbar = view.findViewById(R.id.toolbar);
    etSearch = view.findViewById(R.id.etSearch);
  }

  private void setupToolbar() {
    toolbar.setNavigationOnClickListener(
        v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
  }

  private void setupRecycleView() {
    adapter =
        new HospitalAdapter(
            requireContext(),
            new ArrayList<>(),
            hospital -> {
              Bundle args = new Bundle();
              args.putString("hospital_id", hospital.getId());
              args.putString("hospital_name", hospital.getName());
              args.putString("hospital_address", hospital.getAddress());
              args.putString("hospital_phone", hospital.getPhone());
              args.putString("hospital_website", hospital.getWebsite());
              args.putString("hospital_description", hospital.getDescription());
              args.putDouble("hospital_rating", hospital.getRating());

              HospitalDetailFragment fragment = new HospitalDetailFragment();
              fragment.setArguments(args);
              ((MainTabActivity) requireActivity()).pushFragment(fragment);
            });
    rvHospitals.setLayoutManager(new LinearLayoutManager(requireContext()));
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
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                  List<Hospital> hospitals = new ArrayList<>();
                  for (HospitalResponse r : response.body()) {
                    hospitals.add(mapToHospital(r));
                  }
                  adapter.updateData(hospitals);
                } else {
                  Toast.makeText(requireContext(), "Failed to load hospitals..", Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<List<HospitalResponse>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(
                        requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG)
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

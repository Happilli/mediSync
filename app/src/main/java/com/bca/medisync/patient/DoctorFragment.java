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
import com.bca.medisync.adapter.DoctorAdapter;
import com.bca.medisync.data.model.Doctor;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.dto.doctor.DoctorResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorFragment extends Fragment {
  private RecyclerView rvDoctors;
  private MaterialToolbar toolbar;
  private TextInputEditText etSearch;
  private DoctorAdapter adapter;

  private Integer filterHospitalId;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.activity_doctor, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Bundle args = getArguments();
    if (args != null) {
      String hospitalIdStr = args.getString("hospital_id");
      if (hospitalIdStr != null) {
        try {
          filterHospitalId = Integer.parseInt(hospitalIdStr);
        } catch (NumberFormatException ignored) {
        }
      }
    }
    initViews(view);
    setupToolbar();
    setupRecyclerView();
    setupSearch();
    loadDoctors(null);
  }

  private void initViews(View view) {
    rvDoctors = view.findViewById(R.id.rvDoctors);
    toolbar = view.findViewById(R.id.toolbar);
    etSearch = view.findViewById(R.id.etSearch);
  }

  private void setupToolbar() {
    toolbar.setNavigationOnClickListener(
        v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
  }

  private void setupRecyclerView() {
    adapter =
        new DoctorAdapter(
            requireContext(),
            new ArrayList<>(),
            doctor -> {
              Bundle args = new Bundle();
              args.putString("doctor_id", doctor.getId());
              args.putString("doctor_name", doctor.getName());
              args.putString("doctor_speciality", doctor.getSpeciality());
              args.putString("doctor_info", doctor.getInfo());
              args.putString("doctor_department", doctor.getDepartment());

              BookAppointmentFragment fragment = new BookAppointmentFragment();
              fragment.setArguments(args);
              ((MainTabActivity) requireActivity()).pushFragment(fragment);
            });
    rvDoctors.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvDoctors.setAdapter(adapter);
  }

  private void loadDoctors(String search) {
    DoctorApi api = ApiClient.getRetrofit().create(DoctorApi.class);
    api.getDoctors(filterHospitalId, null, null, search)
        .enqueue(
            new Callback<List<DoctorResponse>>() {
              @Override
              public void onResponse(
                  Call<List<DoctorResponse>> call, Response<List<DoctorResponse>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                  List<Doctor> doctors = new ArrayList<>();
                  for (DoctorResponse r : response.body()) {
                    doctors.add(mapToDoctor(r));
                  }
                  adapter.updateData(doctors);
                } else {
                  Toast.makeText(requireContext(), "failed to load doctors", Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<List<DoctorResponse>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(
                        requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  private Doctor mapToDoctor(DoctorResponse r) {
    String info =
        r.getYears_experience() != null
            ? r.getYears_experience() + "+ Years Exp"
            : (r.getBio() != null ? r.getBio() : "");
    String imageUrl =
        r.getProfile_pic_url() != null
            ? ApiClient.BASE_URL.replaceAll("/$", "") + "/api/v1" + r.getProfile_pic_url()
            : null;
    return new Doctor(
        String.valueOf(r.getId()),
        r.getName(),
        r.getSpeciality(),
        info,
        r.getDepartment(),
        r.getPhone(),
        imageUrl);
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

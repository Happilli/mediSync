package com.bca.medisync.patient;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bca.medisync.R;
import com.bca.medisync.adapter.SimpleListAdapter;
import com.bca.medisync.data.model.Doctor;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.dto.doctor.DoctorResponse;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class DoctorFragment extends Fragment {
  private RecyclerView rvDoctors;
  private MaterialToolbar toolbar;
  private TextInputEditText etSearch;
  private SimpleListAdapter<Doctor> adapter;

  private Integer filterHospitalId;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_doctor, container, false);
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
        new SimpleListAdapter<>(
            R.layout.item_doctor,
            new ArrayList<>(),
            (itemView, doctor, pos) -> {
              ((TextView) itemView.findViewById(R.id.txtDoctorName)).setText(doctor.getName());
              ((TextView) itemView.findViewById(R.id.txtSpeciality))
                  .setText(doctor.getSpeciality());
              ((TextView) itemView.findViewById(R.id.txtInfo)).setText(doctor.getInfo());
              ImageView img = itemView.findViewById(R.id.imgDoctor);
              if (doctor.getImageUrl() != null && !doctor.getImageUrl().isEmpty()) {
                img.setImageTintList(null);
                Glide.with(requireContext())
                    .load(doctor.getImageUrl())
                    .placeholder(R.drawable.stethoscope)
                    .error(R.drawable.stethoscope)
                    .into(img);
              } else {
                img.setImageResource(R.drawable.stethoscope);
              }
              itemView.findViewById(R.id.btnBook).setOnClickListener(v -> onBookClicked(doctor));
            },
            null,
            (doctor, q) ->
                doctor.getName().toLowerCase().contains(q)
                    || doctor.getSpeciality().toLowerCase().contains(q));

    rvDoctors.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvDoctors.setAdapter(adapter);
  }

  private void onBookClicked(Doctor doctor) {
    Bundle args = new Bundle();
    args.putString("doctor_id", doctor.getId());
    args.putString("doctor_name", doctor.getName());
    args.putString("doctor_speciality", doctor.getSpeciality());
    args.putString("doctor_info", doctor.getInfo());
    args.putString("doctor_department", doctor.getDepartment());
    args.putString("doctor_image_url", doctor.getImageUrl());

    BookAppointmentFragment fragment = new BookAppointmentFragment();
    fragment.setArguments(args);
    ((MainTabActivity) requireActivity()).pushFragment(fragment);
  }

  private void loadDoctors(String search) {
    DoctorApi api = ApiClient.getRetrofit().create(DoctorApi.class);
    ApiCallback.handle(
        api.getDoctors(filterHospitalId, null, null, search),
        this,
        body -> {
          List<Doctor> doctors = new ArrayList<>();
          for (DoctorResponse r : body) {
            doctors.add(mapToDoctor(r));
          }
          adapter.updateData(doctors);
        },
        ApiCallback.simpleError(requireContext(), "Failed to load doctors."));
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

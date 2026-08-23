package com.bca.medisync.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.AppointmentAdapter;
import com.bca.medisync.data.model.DataProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

public class DoctorHomeFragment extends Fragment {
  private RecyclerView rvAppointments;
  private MaterialButton btnPatients, btnSchedule;

  public DoctorHomeFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_doctor_home, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    setupAppointments();
    setupListeners();
  }

  private void initViews(View view) {
    rvAppointments = view.findViewById(R.id.rvAppointments);
    btnPatients = view.findViewById(R.id.btnPatients);
    btnSchedule = view.findViewById(R.id.btnSchedule);

    ShapeableImageView btnProfile = view.findViewById(R.id.btnProfile);
    btnProfile.setOnClickListener(
        v -> {
          BottomNavGoTo(R.id.nav_doctor_profile);
        });
  }

  private void BottomNavGoTo(int navItemId) {
    com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
        requireActivity().findViewById(R.id.bottomNavDoctor);
    bottomNav.setSelectedItemId(navItemId);
  }

  private void setupAppointments() {
    rvAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvAppointments.setAdapter(
        new AppointmentAdapter(
            requireContext(), DataProvider.getDoctorSchedule(), true, appointment -> {}));
  }

  private void setupListeners() {
    btnPatients.setOnClickListener(v -> BottomNavGoTo(R.id.nav_doctor_patients));
    btnSchedule.setOnClickListener(v -> BottomNavGoTo(R.id.nav_doctor_schedule));
  }
}

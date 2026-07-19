package com.bca.medisync.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.DashboardAdapter;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.AppointmentApi;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.dto.patient.PatientResponse;
import com.bca.medisync.data.remote.helpers.AppointmentEnricher;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

  private RecyclerView rvDashboard;
  private TextView txtPatientName;
  private MaterialCardView cardAppointment;
  private TextView txtAppointmentDoctor,
      txtAppointmentSpeciality,
      txtAppointmentDate,
      txtAppointmentTime;

  public HomeFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_home, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    setupDashboard();
  }

  @Override
  public void onResume() {
    super.onResume();
    loadPatientName();
    loadUpcomingAppointment();
  }

  private void initViews(View view) {
    txtPatientName = view.findViewById(R.id.txtPatientName);
    rvDashboard = view.findViewById(R.id.rvDashboard);
    cardAppointment = view.findViewById(R.id.cardAppointment);
    txtAppointmentDoctor = view.findViewById(R.id.txtAppointmentDoctor);
    txtAppointmentSpeciality = view.findViewById(R.id.txtAppointmentSpeciality);
    txtAppointmentDate = view.findViewById(R.id.txtAppointmentDate);
    txtAppointmentTime = view.findViewById(R.id.txtAppointmentTime);

    cardAppointment.setVisibility(View.GONE);
    cardAppointment.setOnClickListener(v -> goToTab(R.id.nav_appointments));
  }

  private void goToTab(int navItemId) {
    BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNav);
    bottomNav.setSelectedItemId(navItemId);
  }

  private void loadPatientName() {
    PatientApi api = ApiClient.getRetrofit().create(PatientApi.class);
    api.getMyProfile()
        .enqueue(
            new Callback<PatientResponse>() {
              @Override
              public void onResponse(
                  Call<PatientResponse> call, Response<PatientResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                  txtPatientName.setText(response.body().getName());
                }
              }

              @Override
              public void onFailure(Call<PatientResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(
                        requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  private void loadUpcomingAppointment() {
    AppointmentApi api = ApiClient.getRetrofit().create(AppointmentApi.class);
    api.getMyAppointments(null, null)
        .enqueue(
            new Callback<List<AppointmentResponse>>() {
              @Override
              public void onResponse(
                  Call<List<AppointmentResponse>> call,
                  Response<List<AppointmentResponse>> response) {
                if (!isAdded()) return;
                if (!response.isSuccessful() || response.body() == null) return;

                AppointmentResponse next = AppointmentEnricher.findNextUpcoming(response.body());
                if (next == null) {
                  cardAppointment.setVisibility(View.GONE);
                  return;
                }
                AppointmentEnricher.enrichOne(
                    next,
                    appointment -> {
                      if (!isAdded()) return;
                      txtAppointmentDoctor.setText(appointment.getDoctorName());
                      txtAppointmentSpeciality.setText(appointment.getSpeciality());
                      txtAppointmentDate.setText(appointment.getDate());
                      txtAppointmentTime.setText(appointment.getTime());
                      cardAppointment.setVisibility(View.VISIBLE);
                    });
              }

              @Override
              public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {}
            });
  }

  private void setupDashboard() {
    List<String> titles =
        Arrays.asList(
            "Appointments",
            "Prescriptions",
            "Medications",
            "My Profile",
            "Health Records",
            "Hospitals");
    List<Integer> icons =
        Arrays.asList(
            R.drawable.ic_nav_calendar,
            R.drawable.edit,
            R.drawable.ic_nav_medicine,
            R.drawable.ic_nav_profile,
            R.drawable.record,
            R.drawable.hospital);

    DashboardAdapter adapter =
        new DashboardAdapter(
            requireContext(),
            titles,
            icons,
            position -> {
              switch (position) {
                case 0:
                  goToTab(R.id.nav_appointments);
                  break;
                case 1:
                  break;
                case 2:
                  startActivity(new Intent(requireContext(), MedicationActivity.class));
                  break;
                case 3:
                  goToTab(R.id.nav_profile);
                  break;
                case 4:
                  break;
                case 5:
                  startActivity(new Intent(requireContext(), HospitalActivity.class));
                  break;
              }
            });
    rvDashboard.setLayoutManager(new GridLayoutManager(requireContext(), 3));
    rvDashboard.setAdapter(adapter);
  }
}

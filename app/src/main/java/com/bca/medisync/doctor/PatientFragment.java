package com.bca.medisync.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.DoctorTabActivity;
import com.bca.medisync.R;
import com.bca.medisync.adapter.PatientAdapter;
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.Patient;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.patient.PatientPublicResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientFragment extends Fragment {

  private RecyclerView rvPatients;

  public PatientFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_patient, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    loadPatients();
  }

  private void initViews(View view) {
    rvPatients = view.findViewById(R.id.rvPatients);
    rvPatients.setLayoutManager(new LinearLayoutManager(requireContext()));
  }

  private void loadPatients() {
    if (DoctorDataConfig.USE_REAL_PATIENTS) {
      loadRealPatients();
    } else {
      bindMockPatients();
    }
  }

  private void bindMockPatients() {
    List<Patient> patients = DataProvider.getPatients();
    rvPatients.setAdapter(
        new PatientAdapter(
            requireContext(),
            patients,
            patient -> {
              Bundle args = new Bundle();
              args.putString("patient_name", patient.getName());
              args.putString("patient_phone", patient.getPhone());
              args.putString("patient_email", patient.getEmail());
              args.putString("patient_dob", patient.getDateOfBirth());
              args.putString("patient_gender", patient.getGender());
              args.putString("patient_blood", patient.getBloodGroup());
              args.putString("patient_address", patient.getAddress());
              args.putString("patient_emergency", patient.getEmergencyContact());

              PatientDetailsFragment fragment = new PatientDetailsFragment();
              fragment.setArguments(args);
              ((DoctorTabActivity) requireActivity()).pushFragment(fragment);
            }));
  }

  private void loadRealPatients() {
    PatientApi api = ApiClient.getRetrofit().create(PatientApi.class);
    api.getTreatedPatients()
        .enqueue(
            new Callback<List<PatientPublicResponse>>() {
              @Override
              public void onResponse(
                  Call<List<PatientPublicResponse>> call,
                  Response<List<PatientPublicResponse>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                  List<Patient> patients = new ArrayList<>();
                  for (PatientPublicResponse r : response.body()) {
                    patients.add(mapToPatient(r));
                  }
                  bindRealPatients(response.body());
                } else {
                  Toast.makeText(requireContext(), "Failed to load patients", Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<List<PatientPublicResponse>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(
                        requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG)
                    .show();
              }
            });
  }

  private void bindRealPatients(List<PatientPublicResponse> patients) {
    List<Patient> mapped = new ArrayList<>();
    for (PatientPublicResponse r : patients) {
      mapped.add(mapToPatient(r));
    }
    rvPatients.setAdapter(
        new PatientAdapter(
            requireContext(),
            mapped,
            patient -> {
              PatientPublicResponse match = null;
              for (PatientPublicResponse r : patients) {
                if (String.valueOf(r.getId()).equals(patient.getId())) {
                  match = r;
                  break;
                }
              }
              Bundle args = new Bundle();
              args.putInt("patient_id", match != null ? match.getId() : -1);
              args.putString("patient_name", patient.getName());
              args.putString("patient_phone", patient.getPhone());
              args.putString("patient_gender", patient.getGender());
              args.putString("patient_blood", patient.getBloodGroup());
              args.putString("patient_emergency", patient.getEmergencyContact());
              args.putString("patient_email", patient.getEmail());
              args.putString("patient_address", patient.getAddress());
              args.putString("patient_dob", patient.getDateOfBirth());
              args.putString("patient_pic_url", match != null ? match.getProfile_pic_url() : null);

              PatientDetailsFragment fragment = new PatientDetailsFragment();
              fragment.setArguments(args);
              ((DoctorTabActivity) requireActivity()).pushFragment(fragment);
            }));
  }

  private Patient mapToPatient(PatientPublicResponse r) {
    return new Patient(
        String.valueOf(r.getId()),
        r.getName(),
        r.getEmail(),
        r.getPhone(),
        r.getAddress(),
        r.getDate_of_birth(),
        r.getGender(),
        r.getBlood_group(),
        r.getEmergency_contact(),
        r.getProfile_pic_url());
  }
}

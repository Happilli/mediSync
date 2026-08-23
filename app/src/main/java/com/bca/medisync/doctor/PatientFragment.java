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

import com.bca.medisync.DoctorTabActivity;
import com.bca.medisync.R;
import com.bca.medisync.adapter.PatientAdapter;
import com.bca.medisync.data.model.DataProvider;

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
    setupRecyclerView();
  }

  private void initViews(View view) {
    rvPatients = view.findViewById(R.id.rvPatients);
  }

  private void setupRecyclerView() {
    rvPatients.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvPatients.setAdapter(
        new PatientAdapter(
            requireContext(),
            DataProvider.getPatients(),
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
}

package com.bca.medisync.doctor;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bca.medisync.R;
import com.bca.medisync.adapter.SimpleListAdapter;
import com.bca.medisync.data.model.Patient;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.patient.PatientPublicResponse;
import com.bca.medisync.databinding.ItemPatientBinding;
import com.bca.medisync.util.ApiErrorHandler;
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.SearchSuggestionHelper;
import com.bca.medisync.util.SearchableListFragment;
import java.util.ArrayList;
import java.util.List;

public class PatientFragment extends SearchableListFragment<Patient> {
  private RecyclerView rvPatients;
  private SimpleListAdapter<Patient, ItemPatientBinding> adapter;
  private List<PatientPublicResponse> currentResponses = new ArrayList<>();

  @Override
  protected int getLayoutRes() {
    return R.layout.fragment_patient;
  }

  @Override
  protected void setupResultsView(@NonNull View view) {
    rvPatients = view.findViewById(R.id.rvPatients);
    rvPatients.setLayoutManager(new LinearLayoutManager(requireContext()));
    adapter =
        new SimpleListAdapter<>(
            ItemPatientBinding::inflate,
            new ArrayList<>(),
            (binding, patient, pos) -> {
              binding.txtPatientName.setText(patient.name());
              binding.txtBloodGroup.setText(patient.bloodGroup());
              binding.txtPhone.setText(patient.phone());
              ImageLoader.loadProfilePic(
                  PatientFragment.this, binding.imgPatientPic, patient.profilePicUrl());
            },
            this::onPatientClicked);
    rvPatients.setAdapter(adapter);
    adapter.setRoundedList(true);
  }

  @Override
  protected void search(String query, SearchSuggestionHelper.OnResult<Patient> onResult) {
    PatientApi api = ApiClient.api(PatientApi.class);
    ApiCallback.handle(
        api.getTreatedPatients(query),
        this,
        body -> {
          currentResponses = body;
          List<Patient> patients = new ArrayList<>();
          for (PatientPublicResponse r : body) patients.add(mapToPatient(r));
          onResult.onResult(patients);
        },
        (code, msg) -> onResult.onResult(new ArrayList<>()));
  }

  @Override
  protected SearchSuggestionHelper.SuggestionBinder<Patient> getSuggestionBinder() {
    return new SearchSuggestionHelper.SuggestionBinder<Patient>() {
      @Override
      public String getTitle(Patient item) {
        return item.name();
      }

      @Override
      public String getSubtitle(Patient item) {
        return item.phone();
      }

      @Override
      public String getImageUrl(Patient item) {
        return ApiClient.mediaUrl(item.profilePicUrl());
      }

      @Override
      public int getPlaceholderRes() {
        return R.drawable.ic_nav_profile;
      }
    };
  }

  @Override
  protected void onSuggestionSelected(Patient patient) {
    onPatientClicked(patient);
  }

  @Override
  protected void loadResults(@Nullable String query) {
    PatientApi api = ApiClient.api(PatientApi.class);
    ApiCallback.handle(
        api.getTreatedPatients(query),
        this,
        this::bindPatients,
        ApiErrorHandler.with(requireContext()).fallback("Failed to load patients.").build());
  }

  private void bindPatients(List<PatientPublicResponse> patients) {
    currentResponses = patients;
    List<Patient> mapped = new ArrayList<>();
    for (PatientPublicResponse r : patients) {
      mapped.add(mapToPatient(r));
    }
    adapter.updateData(mapped);
  }

  private void onPatientClicked(Patient patient) {
    PatientPublicResponse match = null;
    for (PatientPublicResponse r : currentResponses) {
      if (String.valueOf(r.id()).equals(patient.id())) {
        match = r;
        break;
      }
    }
    Bundle args = new Bundle();
    args.putInt("patient_id", match != null ? match.id() : -1);
    args.putString("patient_name", patient.name());
    args.putString("patient_phone", patient.phone());
    args.putString("patient_gender", patient.gender());
    args.putString("patient_blood", patient.bloodGroup());
    args.putString("patient_emergency", patient.emergencyContact());
    args.putString("patient_email", patient.email());
    args.putString("patient_address", patient.address());
    args.putString("patient_dob", patient.dateOfBirth());
    args.putString("patient_pic_url", match != null ? match.profile_pic_url() : null);
    PatientDetailsFragment fragment = new PatientDetailsFragment();
    fragment.setArguments(args);
    ((DoctorTabActivity) requireActivity()).pushFragment(fragment);
  }

  private Patient mapToPatient(PatientPublicResponse r) {
    return new Patient(
        String.valueOf(r.id()),
        r.name(),
        r.email(),
        r.phone(),
        r.address(),
        r.date_of_birth(),
        r.gender(),
        r.blood_group(),
        r.emergency_contact(),
        r.profile_pic_url());
  }
}

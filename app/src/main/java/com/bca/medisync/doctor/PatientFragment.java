package com.bca.medisync.doctor;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
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
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.SearchSuggestionHelper;
import com.bca.medisync.util.SearchableListFragment;
import java.util.ArrayList;
import java.util.List;

public class PatientFragment extends SearchableListFragment<Patient> {
  private RecyclerView rvPatients;
  private SimpleListAdapter<Patient> adapter;
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
            R.layout.item_patient,
            new ArrayList<>(),
            (itemView, patient, pos) -> {
              ((TextView) itemView.findViewById(R.id.txtPatientName)).setText(patient.getName());
              ((TextView) itemView.findViewById(R.id.txtBloodGroup))
                  .setText(patient.getBloodGroup());
              ((TextView) itemView.findViewById(R.id.txtPhone)).setText(patient.getPhone());
              ImageLoader.loadProfilePic(
                  PatientFragment.this,
                  itemView.findViewById(R.id.imgPatientPic),
                  patient.getProfilePicUrl());
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
        return item.getName();
      }

      @Override
      public String getSubtitle(Patient item) {
        return item.getPhone();
      }

      @Override
      public String getImageUrl(Patient item) {
        return ApiClient.mediaUrl(item.getProfilePicUrl());
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
        ApiCallback.simpleError(requireContext(), "Failed to load patients."));
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

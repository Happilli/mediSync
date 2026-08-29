package com.bca.medisync.doctor;

import android.os.Bundle;
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
import com.bca.medisync.data.model.Patient;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.patient.PatientPublicResponse;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class PatientFragment extends Fragment {

  private RecyclerView rvPatients;
  private SimpleListAdapter<Patient> adapter;
  private List<PatientPublicResponse> currentResponses = new ArrayList<>();

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

    adapter =
        new SimpleListAdapter<>(
            R.layout.item_patient,
            new ArrayList<>(),
            (itemView, patient, pos) -> {
              ((TextView) itemView.findViewById(R.id.txtPatientName)).setText(patient.getName());
              ((TextView) itemView.findViewById(R.id.txtBloodGroup))
                  .setText(patient.getBloodGroup());
              ((TextView) itemView.findViewById(R.id.txtPhone)).setText(patient.getPhone());

              ImageView img = itemView.findViewById(R.id.imgPatientPic);
              String url = patient.getProfilePicUrl();
              if (url != null && !url.isEmpty()) {
                Glide.with(requireContext())
                    .load(ApiClient.BASE_URL.replaceAll("/$", "") + "/api/v1" + url)
                    .placeholder(R.drawable.ic_nav_profile)
                    .error(R.drawable.ic_nav_profile)
                    .centerCrop()
                    .into(img);
              } else {
                img.setImageResource(R.drawable.ic_nav_profile);
              }
            },
            this::onPatientClicked);

    rvPatients.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvPatients.setAdapter(adapter);
    adapter.setRoundedList(true);
  }

  private void loadPatients() {
    PatientApi api = ApiClient.getRetrofit().create(PatientApi.class);
    ApiCallback.handle(
        api.getTreatedPatients(),
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

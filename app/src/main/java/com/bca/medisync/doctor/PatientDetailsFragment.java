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
import com.bca.medisync.R;
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.InfoRowBinder;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class PatientDetailsFragment extends Fragment {

  private int patientId = -1;
  private MaterialToolbar toolbar;
  private ImageView imgProfile;
  private TextView txtName;
  private View rowGender, rowBlood, rowPhone, rowEmail, rowDob, rowAddress, rowEmergency;
  private MaterialButton btnConsultation;
  private ExtendedFloatingActionButton fabHistory;

  private String patientName;
  private int appointmentId = -1;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_patient_details, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    setupToolbar();
    loadData();
    setupListener();
  }

  private void initViews(View view) {
    toolbar = view.findViewById(R.id.toolbar);
    txtName = view.findViewById(R.id.txtPatientName);
    rowGender = view.findViewById(R.id.rowGender);
    rowBlood = view.findViewById(R.id.rowBlood);
    rowPhone = view.findViewById(R.id.rowPhone);
    rowEmail = view.findViewById(R.id.rowEmail);
    rowDob = view.findViewById(R.id.rowDob);
    rowAddress = view.findViewById(R.id.rowAddress);
    rowEmergency = view.findViewById(R.id.rowEmergency);
    btnConsultation = view.findViewById(R.id.btnConsultation);
    fabHistory = view.findViewById(R.id.fabHistory);
    imgProfile = view.findViewById(R.id.imgPatientProfile);
  }

  private void setupToolbar() {
    toolbar.setNavigationOnClickListener(
        v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
  }

  private void loadData() {
    Bundle args = getArguments();
    if (args == null) return;

    patientId = args.getInt("patient_id", -1);
    patientName = args.getString("patient_name");
    toolbar.setTitle(patientName);
    txtName.setText(patientName);
    appointmentId = args.getInt("appointment_id", -1);

    InfoRowBinder.bind(
        new View[] {rowGender, rowBlood, rowPhone, rowEmail, rowDob, rowAddress, rowEmergency},
        new int[] {
          R.drawable.stethoscope,
          R.drawable.stethoscope,
          R.drawable.phone,
          R.drawable.email,
          R.drawable.birthdate,
          R.drawable.location,
          R.drawable.emergency
        },
        new String[] {
          "Gender", "Blood Group", "Phone", "Email", "Date of Birth", "Address", "Emergency Contact"
        },
        new String[] {
          args.getString("patient_gender"),
          args.getString("patient_blood"),
          args.getString("patient_phone"),
          args.getString("patient_email"),
          args.getString("patient_dob"),
          args.getString("patient_address"),
          args.getString("patient_emergency")
        });

    bindProfilePic(args.getString("patient_pic_url"));
    btnConsultation.setVisibility(appointmentId != -1 ? View.VISIBLE : View.GONE);
  }

  private void bindProfilePic(String url) {
    ImageLoader.loadProfilePic(this, imgProfile, url);
  }

  private void setupListener() {
    fabHistory.setOnClickListener(
        v -> {
          Bundle args = new Bundle();
          args.putString("patient_name", patientName);
          args.putInt("patient_id", patientId);
          args.putInt("appointment_id", appointmentId);
          MedicalHistoryFragment fragment = new MedicalHistoryFragment();
          fragment.setArguments(args);
          ((DoctorTabActivity) requireActivity()).pushFragment(fragment);
        });
    btnConsultation.setOnClickListener(
        v -> {
          Bundle args = new Bundle();
          args.putString("patient_name", patientName);
          args.putInt("appointment_id", appointmentId);
          ConsultationFragment fragment = new ConsultationFragment();
          fragment.setArguments(args);
          ((DoctorTabActivity) requireActivity()).pushFragment(fragment);
        });
  }
}

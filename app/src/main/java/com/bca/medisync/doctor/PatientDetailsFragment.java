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

import com.bca.medisync.DoctorTabActivity;
import com.bca.medisync.R;
import com.bca.medisync.data.remote.ApiClient;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class PatientDetailsFragment extends Fragment {

  private int patientId = -1;
  private MaterialToolbar toolbar;
  private ImageView imgProfile;
  private TextView txtName,
      txtGender,
      txtBlood,
      txtPhone,
      txtEmail,
      txtDob,
      txtAddress,
      txtEmergency;
  private MaterialButton btnHistory, btnConsultation;

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
    txtGender = view.findViewById(R.id.txtGender);
    txtBlood = view.findViewById(R.id.txtBloodGroup);
    txtPhone = view.findViewById(R.id.txtPhone);
    txtEmail = view.findViewById(R.id.txtEmail);
    txtDob = view.findViewById(R.id.txtDob);
    txtAddress = view.findViewById(R.id.txtAddress);
    txtEmergency = view.findViewById(R.id.txtEmergency);
    btnHistory = view.findViewById(R.id.btnHistory);
    btnConsultation = view.findViewById(R.id.btnConsultation);
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
    txtGender.setText(args.getString("patient_gender"));
    txtBlood.setText(args.getString("patient_blood"));
    txtPhone.setText(args.getString("patient_phone"));
    txtEmail.setText(args.getString("patient_email"));
    txtDob.setText(args.getString("patient_dob"));
    txtAddress.setText(args.getString("patient_address"));
    txtEmergency.setText(args.getString("patient_emergency"));
    appointmentId = args.getInt("appointment_id", -1);
    bindProfilePic(args.getString("patient_pic_url"));
    btnConsultation.setVisibility(appointmentId != -1 ? View.VISIBLE : View.GONE);
  }

  private void bindProfilePic(String url) {
    if (url == null || url.isEmpty()) {
      imgProfile.setImageResource(R.drawable.ic_nav_profile);
      return;
    }
    Glide.with(this)
        .load(ApiClient.BASE_URL.replaceAll("/$", "") + "/api/v1" + url)
        .placeholder(R.drawable.ic_nav_profile)
        .error(R.drawable.ic_nav_profile)
        .centerCrop()
        .into(imgProfile);
  }

  private void setupListener() {
    btnHistory.setOnClickListener(
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

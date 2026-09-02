package com.bca.medisync.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bca.medisync.databinding.FragmentPatientDetailsBinding;
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.InfoRowBinder;

public class PatientDetailsFragment extends Fragment {

  private FragmentPatientDetailsBinding binding;

  private int patientId = -1;
  private String patientName;
  private int appointmentId = -1;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentPatientDetailsBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setupToolbar();
    loadData();
    setupListener();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void setupToolbar() {
    binding.toolbar.setNavigationOnClickListener(
        v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
  }

  private void loadData() {
    Bundle args = getArguments();
    if (args == null) return;

    patientId = args.getInt("patient_id", -1);
    patientName = args.getString("patient_name");
    binding.toolbar.setTitle(patientName);
    binding.txtPatientName.setText(patientName);
    appointmentId = args.getInt("appointment_id", -1);

    InfoRowBinder.bind(
        new InfoRowBinder.Row(
            binding.rowGender.getRoot(),
            com.bca.medisync.R.drawable.stethoscope,
            "Gender",
            args.getString("patient_gender")),
        new InfoRowBinder.Row(
            binding.rowBlood.getRoot(),
            com.bca.medisync.R.drawable.stethoscope,
            "Blood Group",
            args.getString("patient_blood")),
        new InfoRowBinder.Row(
            binding.rowPhone.getRoot(),
            com.bca.medisync.R.drawable.phone,
            "Phone",
            args.getString("patient_phone")),
        new InfoRowBinder.Row(
            binding.rowEmail.getRoot(),
            com.bca.medisync.R.drawable.email,
            "Email",
            args.getString("patient_email")),
        new InfoRowBinder.Row(
            binding.rowDob.getRoot(),
            com.bca.medisync.R.drawable.birthdate,
            "Date of Birth",
            args.getString("patient_dob")),
        new InfoRowBinder.Row(
            binding.rowAddress.getRoot(),
            com.bca.medisync.R.drawable.location,
            "Address",
            args.getString("patient_address")),
        new InfoRowBinder.Row(
            binding.rowEmergency.getRoot(),
            com.bca.medisync.R.drawable.emergency,
            "Emergency Contact",
            args.getString("patient_emergency")));

    bindProfilePic(args.getString("patient_pic_url"));
    binding.btnConsultation.setVisibility(appointmentId != -1 ? View.VISIBLE : View.GONE);
  }

  private void bindProfilePic(String url) {
    ImageLoader.loadProfilePic(this, binding.imgPatientProfile, url);
  }

  private void setupListener() {
    binding.fabHistory.setOnClickListener(
        v -> {
          Bundle args = new Bundle();
          args.putString("patient_name", patientName);
          args.putInt("patient_id", patientId);
          args.putInt("appointment_id", appointmentId);
          MedicalHistoryFragment fragment = new MedicalHistoryFragment();
          fragment.setArguments(args);
          ((DoctorTabActivity) requireActivity()).pushFragment(fragment);
        });
    binding.btnConsultation.setOnClickListener(
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

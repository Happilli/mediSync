package com.bca.medisync.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bca.medisync.BaseBindingFragment;
import com.bca.medisync.databinding.FragmentPatientDetailsBinding;
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.InfoRowBinder;
import com.bca.medisync.util.ViewUtils;
import com.bca.medisync.R;

public class PatientDetailsFragment extends BaseBindingFragment<FragmentPatientDetailsBinding> {
  private int patientId = -1;
  private String bookingNotes;
  private String patientName;
  private int appointmentId = -1;

  @Override
  protected FragmentPatientDetailsBinding inflateBinding(
      LayoutInflater inflater, ViewGroup container) {
    return FragmentPatientDetailsBinding.inflate(inflater, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ViewUtils.setupBackNav(this, binding.toolbar);
    loadData();
    setupListener();
  }

  private void loadData() {
    Bundle args = getArguments();
    if (args == null) return;
    patientId = args.getInt("patient_id", -1);
    patientName = args.getString("patient_name");
    bookingNotes = args.getString("booking_notes");
    appointmentId = args.getInt("appointment_id", -1);

    binding.toolbar.setTitle("");
    binding.txtPatientName.setText(patientName);

    String gender = args.getString("patient_gender");
    String blood = args.getString("patient_blood");
    binding.chipGender.setText(isEmpty(gender) ? "--" : capitalize(gender));
    binding.chipBloodGroup.setText(isEmpty(blood) ? "--" : blood);

    String emergency = args.getString("patient_emergency");
    binding.txtEmergencyContact.setText(isEmpty(emergency) ? "Not provided" : emergency);

    InfoRowBinder.bind(
        new InfoRowBinder.Row(binding.rowPhone.getRoot(), "Phone", args.getString("patient_phone")),
        new InfoRowBinder.Row(binding.rowEmail.getRoot(), "Email", args.getString("patient_email")),
        new InfoRowBinder.Row(
            binding.rowDob.getRoot(), "Date of Birth", args.getString("patient_dob")),
        new InfoRowBinder.Row(
            binding.rowAddress.getRoot(), "Address", args.getString("patient_address")));

    bindProfilePic(args.getString("patient_pic_url"));
    binding.btnConsultation.setVisibility(appointmentId != -1 ? View.VISIBLE : View.GONE);
  }

  private void bindProfilePic(String url) {
    int borderPx = ViewUtils.dp(requireContext(), 4);
    int borderColor = requireContext().getColor(R.color.primary);
    ImageLoader.loadProfilePicShaped(
        this, binding.imgPatientProfile, url, R.drawable.sunny, borderPx, borderColor);
  }

  private void setupListener() {
    binding.fabHistory.setOnClickListener(
        v -> {
          Bundle args = new Bundle();
          args.putBoolean("is_doctor_view", true);
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
          args.putString("booking_notes", bookingNotes);
          ConsultationFragment fragment = new ConsultationFragment();
          fragment.setArguments(args);
          ((DoctorTabActivity) requireActivity()).pushFragment(fragment);
        });
  }

  private boolean isEmpty(String s) {
    return s == null || s.trim().isEmpty();
  }

  private String capitalize(String s) {
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }
}

package com.bca.medisync.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import com.bca.medisync.R;
import com.bca.medisync.adapter.TimeSlotAdapter;
import com.bca.medisync.data.model.TimeSlot;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.AppointmentApi;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.dto.TimeSlotResponse;
import com.bca.medisync.data.remote.dto.appointment.AppointmentCreateRequest;
import com.bca.medisync.databinding.FragmentBookAppointmentBinding;
import com.bca.medisync.util.DateTimeUtils;
import com.bca.medisync.util.EmptyState;
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class BookAppointmentFragment extends Fragment {
  private FragmentBookAppointmentBinding binding;
  private TimeSlot selectedTimeSlot;
  private String doctorName, doctorSpeciality, doctorInfo, doctorDepartment;
  private int doctorId = -1;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentBookAppointmentBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    binding.btnConfirm.setEnabled(false);
    ViewUtils.setupBackNav(this, binding.toolbar);
    loadDoctorData();
    setupTimeSlots();
    setupConfirmButton();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void loadDoctorData() {
    Bundle args = getArguments();
    String doctorIdStr = args != null ? args.getString("doctor_id") : null;
    if (args == null || doctorIdStr == null) {
      bailNoDoctor("Doctor not specified");
      return;
    }
    try {
      doctorId = Integer.parseInt(doctorIdStr);
    } catch (NumberFormatException e) {
      bailNoDoctor("Invalid doctor reference");
      return;
    }
    doctorName = args.getString("doctor_name");
    doctorSpeciality = args.getString("doctor_speciality");
    doctorInfo = args.getString("doctor_info");
    doctorDepartment = args.getString("doctor_department");
    if (doctorName != null) binding.txtDoctorName.setText(doctorName);
    if (doctorSpeciality != null) binding.txtDoctorSpeciality.setText(doctorSpeciality);
    if (doctorInfo != null) binding.txtDoctorInfo.setText(doctorInfo);
    ImageLoader.loadTinted(
        this, binding.imgDoctor, args.getString("doctor_image_url"), R.drawable.stethoscope);
  }

  private void bailNoDoctor(String message) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    requireActivity().getOnBackPressedDispatcher().onBackPressed();
  }

  private void bindTimeSlots(List<TimeSlot> slots) {
    if (binding == null) return;
    EmptyState.bind(binding.rvTimeSlots, binding.txtNoSlots, slots.isEmpty());
    binding.btnConfirm.setEnabled(!slots.isEmpty());
    if (slots.isEmpty()) return;
    TimeSlotAdapter adapter =
        new TimeSlotAdapter(requireContext(), slots, slot -> selectedTimeSlot = slot);
    binding.rvTimeSlots.setLayoutManager(new GridLayoutManager(requireContext(), 3));
    binding.rvTimeSlots.setAdapter(adapter);
  }

  private TimeSlot mapToTimeSlot(TimeSlotResponse r) {
    String displayTime = DateTimeUtils.format(r.getAppointment_at(), "dd MMM, hh:mm a");
    return new TimeSlot(r.getId(), r.getAppointment_at(), displayTime, r.is_available());
  }

  private void setupTimeSlots() {
    if (doctorId == -1) return;
    DoctorApi api = ApiClient.api(DoctorApi.class);
    ApiCallback.handle(
        api.getDoctorTimeslots(doctorId, true),
        this,
        body -> {
          List<TimeSlot> slots = new ArrayList<>();
          for (TimeSlotResponse r : body) {
            slots.add(mapToTimeSlot(r));
          }
          bindTimeSlots(slots);
        },
        ApiCallback.simpleError(requireContext(), "Failed to load available slots."));
  }

  private void setupConfirmButton() {
    binding.btnConfirm.setOnClickListener(
        v -> {
          if (selectedTimeSlot == null) {
            Toast.makeText(requireContext(), "Please select a time slot", Toast.LENGTH_SHORT)
                .show();
            return;
          }
          String notes =
              binding.etNotes.getText() != null ? binding.etNotes.getText().toString() : "";
          binding.btnConfirm.setEnabled(false);
          AppointmentApi api = ApiClient.api(AppointmentApi.class);
          ApiCallback.handle(
              api.createAppointment(new AppointmentCreateRequest(selectedTimeSlot.getId(), notes)),
              this,
              body -> {
                if (binding == null) return;
                binding.btnConfirm.setEnabled(true);
                Toast.makeText(requireContext(), "Appointment booked!", Toast.LENGTH_LONG).show();
                ((MainTabActivity) requireActivity()).popToRootAndRefreshAppointments();
              },
              (code, msg) -> {
                if (binding == null) return;
                binding.btnConfirm.setEnabled(true);
                if (code == 403) {
                  Toast.makeText(
                          requireContext(),
                          "You need to be verified before booking appointments.",
                          Toast.LENGTH_LONG)
                      .show();
                } else if (code == 400) {
                  Toast.makeText(
                          requireContext(), "This slot is no longer available.", Toast.LENGTH_LONG)
                      .show();
                  setupTimeSlots();
                } else if (code == -1) {
                  Toast.makeText(requireContext(), "Network error: " + msg, Toast.LENGTH_LONG)
                      .show();
                } else {
                  Toast.makeText(requireContext(), "Booking failed.", Toast.LENGTH_SHORT).show();
                }
              });
        });
  }
}

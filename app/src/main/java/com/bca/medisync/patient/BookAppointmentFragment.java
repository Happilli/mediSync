package com.bca.medisync.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.TimeSlotAdapter;
import com.bca.medisync.data.model.TimeSlot;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.AppointmentApi;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.dto.TimeSlotResponse;
import com.bca.medisync.data.remote.dto.appointment.AppointmentCreateRequest;
import com.bca.medisync.util.DateTimeUtils;
import com.bca.medisync.util.EmptyState;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class BookAppointmentFragment extends Fragment {
  private MaterialToolbar toolbar;
  private TextView txtDoctorName, txtDoctorSpeciality, txtDoctorInfo;
  private MaterialButton btnConfirm;
  private TextInputEditText etNotes;
  private RecyclerView rvTimeSlots;
  private TextView txtNoSlots;

  private TimeSlot selectedTimeSlot;
  private String doctorName, doctorSpeciality, doctorInfo, doctorDepartment;
  private int doctorId = -1;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.activity_book_appointment, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    setupToolbar();
    loadDoctorData();
    setupTimeSlots();
    setupConfirmButton();
  }

  private void initViews(View view) {
    toolbar = view.findViewById(R.id.toolbar);
    txtDoctorName = view.findViewById(R.id.txtDoctorName);
    txtDoctorSpeciality = view.findViewById(R.id.txtDoctorSpeciality);
    txtDoctorInfo = view.findViewById(R.id.txtDoctorInfo);
    btnConfirm = view.findViewById(R.id.btnConfirm);
    btnConfirm.setEnabled(false);
    etNotes = view.findViewById(R.id.etNotes);
    rvTimeSlots = view.findViewById(R.id.rvTimeSlots);
    txtNoSlots = view.findViewById(R.id.txtNoSlots);
  }

  private void setupToolbar() {
    toolbar.setNavigationOnClickListener(
        v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
  }

  private void loadDoctorData() {
    Bundle args = getArguments();
    if (args == null) {
      Toast.makeText(requireContext(), "Doctor not specified", Toast.LENGTH_SHORT).show();
      requireActivity().getOnBackPressedDispatcher().onBackPressed();
      return;
    }

    String doctorIdStr = args.getString("doctor_id");
    doctorName = args.getString("doctor_name");
    doctorSpeciality = args.getString("doctor_speciality");
    doctorInfo = args.getString("doctor_info");
    doctorDepartment = args.getString("doctor_department");

    if (doctorIdStr != null) {
      try {
        doctorId = Integer.parseInt(doctorIdStr);
      } catch (NumberFormatException e) {
        Toast.makeText(requireContext(), "Invalid doctor reference", Toast.LENGTH_SHORT).show();
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
        return;
      }
    } else {
      Toast.makeText(requireContext(), "Doctor not specified", Toast.LENGTH_SHORT).show();
      requireActivity().getOnBackPressedDispatcher().onBackPressed();
      return;
    }

    if (doctorName != null) txtDoctorName.setText(doctorName);
    if (doctorSpeciality != null) txtDoctorSpeciality.setText(doctorSpeciality);
    if (doctorInfo != null) txtDoctorInfo.setText(doctorInfo);
  }

  private void bindTimeSlots(List<TimeSlot> slots) {
    EmptyState.bind(rvTimeSlots, txtNoSlots, slots.isEmpty());
    btnConfirm.setEnabled(!slots.isEmpty());
    if (slots.isEmpty()) return;
    TimeSlotAdapter adapter =
        new TimeSlotAdapter(requireContext(), slots, slot -> selectedTimeSlot = slot);
    rvTimeSlots.setLayoutManager(new GridLayoutManager(requireContext(), 3));
    rvTimeSlots.setAdapter(adapter);
  }

  private TimeSlot mapToTimeSlot(TimeSlotResponse r) {
    String displayTime = DateTimeUtils.format(r.getAppointment_at(), "dd MMM, hh:mm a");
    return new TimeSlot(r.getId(), r.getAppointment_at(), displayTime, r.isIs_available());
  }

  private void setupTimeSlots() {
    if (doctorId == -1) return;
    DoctorApi api = ApiClient.getRetrofit().create(DoctorApi.class);
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
        (code, msg) ->
            Toast.makeText(requireContext(), "Failed to load available slots", Toast.LENGTH_SHORT)
                .show());
  }

  private void setupConfirmButton() {
    btnConfirm.setOnClickListener(
        v -> {
          if (selectedTimeSlot == null) {
            Toast.makeText(requireContext(), "Please select a time slot", Toast.LENGTH_SHORT)
                .show();
            return;
          }
          String notes = etNotes.getText() != null ? etNotes.getText().toString() : "";
          btnConfirm.setEnabled(false);

          AppointmentApi api = ApiClient.getRetrofit().create(AppointmentApi.class);
          ApiCallback.handle(
              api.createAppointment(new AppointmentCreateRequest(selectedTimeSlot.getId(), notes)),
              this,
              body -> {
                btnConfirm.setEnabled(true);
                Toast.makeText(requireContext(), "Appointment booked!", Toast.LENGTH_LONG).show();
                ((MainTabActivity) requireActivity()).popToRootAndRefreshAppointments();
              },
              (code, msg) -> {
                btnConfirm.setEnabled(true);
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

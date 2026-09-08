package com.bca.medisync.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bca.medisync.BaseBindingFragment;
import com.bca.medisync.R;
import com.bca.medisync.adapter.AppointmentAdapter;
import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.AppointmentApi;
import com.bca.medisync.data.remote.helpers.AppointmentEnricher;
import com.bca.medisync.databinding.FragmentAppointmentBinding;
import com.bca.medisync.util.ApiErrorHandler;
import com.bca.medisync.util.EmptyState;
import com.bca.medisync.util.SwipeActionHelper;
import java.util.ArrayList;
import java.util.List;

public class AppointmentFragment extends BaseBindingFragment<FragmentAppointmentBinding> {
  public AppointmentFragment() {}

  @Override
  protected FragmentAppointmentBinding inflateBinding(
      LayoutInflater inflater, ViewGroup container) {
    return FragmentAppointmentBinding.inflate(inflater, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setupToolbar();
    setupTabs();
    setUpRecylerViews();
    setupSwipe();
    setupFab();
    loadAppointments();
  }

  @Override
  public void onResume() {
    super.onResume();
    loadAppointments();
  }

  public void refresh() {
    loadAppointments();
  }

  private void setupToolbar() {
    binding.toolbar.setNavigationOnClickListener(null);
  }

  private void setupTabs() {
    binding.toggleGroup.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          if (!isChecked) return;
          boolean upcoming = checkedId == R.id.btnUpcoming;
          binding.upcomingContainer.setVisibility(upcoming ? View.VISIBLE : View.GONE);
          binding.historyContainer.setVisibility(upcoming ? View.GONE : View.VISIBLE);
        });
  }

  private void setUpRecylerViews() {
    binding.rvUpcoming.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
  }

  private void setupSwipe() {
    new SwipeActionHelper(
            this,
            position -> {
              AppointmentAdapter adapter = (AppointmentAdapter) binding.rvUpcoming.getAdapter();
              if (adapter == null) return 0;
              Appointment a = adapter.getItemAt(position);
              if (a == null) return 0;
              return a.getStatus().equalsIgnoreCase("Pending") ? ItemTouchHelper.LEFT : 0;
            },
            (position, direction) -> {
              AppointmentAdapter adapter = (AppointmentAdapter) binding.rvUpcoming.getAdapter();
              if (adapter == null) return;
              Appointment a = adapter.getItemAt(position);
              if (a != null) cancelAppointment(a);
            })
        .withColors(
            R.color.error_container, R.color.on_error_container,
            R.color.error_container, R.color.on_error_container)
        .attachTo(binding.rvUpcoming);
  }

  private void setupFab() {
    binding.fabBookAppointment.setOnClickListener(
        v -> ((MainTabActivity) requireActivity()).pushFragment(new HospitalFragment()));
  }

  @Override
  public void onHiddenChanged(boolean hidden) {
    super.onHiddenChanged(hidden);
    if (!hidden && isAdded()) {
      loadAppointments();
    }
  }

  private void bindLists(List<Appointment> all) {
    if (!isAdded() || binding == null) return;
    List<Appointment> upcoming = new ArrayList<>();
    List<Appointment> history = new ArrayList<>();
    for (Appointment a : all) {
      if (a.getStatus().equalsIgnoreCase("Confirmed")
          || a.getStatus().equalsIgnoreCase("Pending")) {
        upcoming.add(a);
      } else {
        history.add(a);
      }
    }
    binding.rvUpcoming.setAdapter(
        new AppointmentAdapter(
            requireContext(), upcoming, false, true, this::onAppointmentClicked));
    binding.rvHistory.setAdapter(
        new AppointmentAdapter(requireContext(), history, false, this::onAppointmentClicked));

    EmptyState.bind(binding.rvUpcoming, binding.txtNoUpcoming, upcoming.isEmpty());
    EmptyState.bind(binding.rvHistory, binding.txtNoHistory, history.isEmpty());
  }

  private void onAppointmentClicked(Appointment appointment) {
    Toast.makeText(
            requireContext(),
            appointment.getStatus().equalsIgnoreCase("Pending")
                ? "Swipe left to cancel this appointment."
                : "This appointment can no longer be modified.",
            Toast.LENGTH_SHORT)
        .show();
  }

  private void loadAppointments() {
    AppointmentApi api = ApiClient.api(AppointmentApi.class);
    ApiCallback.handle(
        api.getMyAppointments(null, null),
        this,
        body -> AppointmentEnricher.enrichAll(body, AppointmentFragment.this::bindLists),
        ApiErrorHandler.with(requireContext()).fallback("Failed to load appointments.").build());
  }

  private void cancelAppointment(Appointment appointment) {
    int appointmentId;
    try {
      appointmentId = Integer.parseInt(appointment.getId());
    } catch (NumberFormatException e) {
      Toast.makeText(requireContext(), "Invalid appointment reference", Toast.LENGTH_SHORT).show();
      loadAppointments();
      return;
    }
    AppointmentApi api = ApiClient.api(AppointmentApi.class);
    ApiCallback.handle(
        api.cancelAppointment(appointmentId),
        this,
        body -> {
          Toast.makeText(requireContext(), "Appointment cancelled.", Toast.LENGTH_SHORT).show();
          loadAppointments();
        },
        (code, msg) ->
            ApiErrorHandler.with(requireContext())
                .on(400, "This appointment can no longer be cancelled.")
                .on(403, "Not your appointment.")
                .fallback("Failed to cancel appointment.")
                .then(this::loadAppointments)
                .build()
                .run(code, msg));
  }
}

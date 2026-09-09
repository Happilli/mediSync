package com.bca.medisync.adapter;

import android.content.Context;
import android.view.View;
import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.databinding.ItemAppointmentBinding;
import com.bca.medisync.util.RoundedListStyler;
import com.bca.medisync.util.StatusChip;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppointmentAdapter extends SimpleListAdapter<Appointment, ItemAppointmentBinding> {
  public interface OnItemClickListener {
    void onItemClick(Appointment appointment);
  }

  private final boolean showPatientView;
  private final boolean swipeEnabled;
  private final Set<String> hintShown = new HashSet<>();

  public AppointmentAdapter(
      Context context,
      List<Appointment> appointments,
      boolean showPatientView,
      OnItemClickListener listener) {
    this(context, appointments, showPatientView, false, listener);
  }

  public AppointmentAdapter(
      Context context,
      List<Appointment> appointments,
      boolean showPatientView,
      boolean swipeEnabled,
      OnItemClickListener listener) {
    super(ItemAppointmentBinding::inflate, appointments, null, null);
    this.showPatientView = showPatientView;
    this.swipeEnabled = swipeEnabled;
    setBinder((binding, a, position) -> bindRow(binding, a, position, listener));
  }

  private void bindRow(
      ItemAppointmentBinding binding, Appointment a, int position, OnItemClickListener listener) {
    boolean pending = a.status().equalsIgnoreCase("Pending");
    binding.txtDoctorName.setText(showPatientView ? a.patientName() : a.doctorName());
    binding.txtSpeciality.setText(a.speciality());
    binding.txtDepartment.setText(a.department());
    binding.txtStatus.setText(a.status());
    binding.txtDate.setText(a.date() + " - " + a.time());
    StatusChip.bind(binding.txtStatus, a.status());
    boolean showHint = swipeEnabled && pending && hintShown.contains(a.id());
    binding.footerRow.setVisibility(showHint ? View.GONE : View.VISIBLE);
    binding.txtSwipeHint.setVisibility(showHint ? View.VISIBLE : View.GONE);
    binding.divider.setVisibility(View.GONE);
    RoundedListStyler.apply(binding.getRoot(), position, getItemCount());
    binding
        .getRoot()
        .setOnClickListener(
            v -> {
              if (swipeEnabled && pending) {
                if (!hintShown.add(a.id())) hintShown.remove(a.id());
                notifyItemChanged(position);
              } else {
                listener.onItemClick(a);
              }
            });
  }
}

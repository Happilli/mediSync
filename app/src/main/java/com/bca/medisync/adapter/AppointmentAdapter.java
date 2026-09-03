package com.bca.medisync.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.databinding.ItemAppointmentBinding;
import com.bca.medisync.util.RoundedListStyler;
import com.bca.medisync.util.StatusChip;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
  public interface OnItemClickListener {
    void onItemClick(Appointment appointment);
  }

  private final Context context;
  private final List<Appointment> appointments;
  private final OnItemClickListener listener;
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
    this.context = context;
    this.appointments = appointments;
    this.showPatientView = showPatientView;
    this.swipeEnabled = swipeEnabled;
    this.listener = listener;
  }

  public Appointment getItemAt(int position) {
    if (position < 0 || position >= appointments.size()) return null;
    return appointments.get(position);
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemAppointmentBinding binding =
        ItemAppointmentBinding.inflate(LayoutInflater.from(context), parent, false);
    return new ViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Appointment a = appointments.get(position);
    boolean pending = a.getStatus().equalsIgnoreCase("Pending");
    holder.binding.txtDoctorName.setText(showPatientView ? a.getPatientName() : a.getDoctorName());
    holder.binding.txtSpeciality.setText(a.getSpeciality());
    holder.binding.txtDepartment.setText(a.getDepartment());
    holder.binding.txtStatus.setText(a.getStatus());
    holder.binding.txtDate.setText(a.getDate() + " - " + a.getTime());
    StatusChip.bind(holder.binding.txtStatus, a.getStatus());
    boolean showHint = swipeEnabled && pending && hintShown.contains(a.getId());
    holder.binding.footerRow.setVisibility(showHint ? View.GONE : View.VISIBLE);
    holder.binding.txtSwipeHint.setVisibility(showHint ? View.VISIBLE : View.GONE);
    holder.binding.divider.setVisibility(View.GONE);
    RoundedListStyler.apply(holder.itemView, position, getItemCount());
    holder.itemView.setOnClickListener(
        v -> {
          if (swipeEnabled && pending) {
            if (!hintShown.add(a.getId())) hintShown.remove(a.getId());
            notifyItemChanged(holder.getAbsoluteAdapterPosition());
          } else {
            listener.onItemClick(a);
          }
        });
  }

  @Override
  public int getItemCount() {
    return appointments.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    final ItemAppointmentBinding binding;

    public ViewHolder(@NonNull ItemAppointmentBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}

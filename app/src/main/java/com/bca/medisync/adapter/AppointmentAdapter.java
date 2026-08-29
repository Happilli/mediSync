package com.bca.medisync.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.R;

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
    View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Appointment a = appointments.get(position);
    boolean pending = a.getStatus().equalsIgnoreCase("Pending");

    holder.txtDoctorName.setText(showPatientView ? a.getPatientName() : a.getDoctorName());
    holder.txtSpeciality.setText(a.getSpeciality());
    holder.txtDepartment.setText(a.getDepartment());
    holder.txtStatus.setText(a.getStatus());
    holder.txtDate.setText(a.getDate() + " - " + a.getTime());

    switch (a.getStatus()) {
      case "Confirmed":
        holder.txtStatus.setTextColor(context.getColor(R.color.tertiary));
        holder.txtStatus.setBackgroundColor(context.getColor(R.color.tertiary_container));
        break;
      case "Pending":
        holder.txtStatus.setTextColor(context.getColor(R.color.secondary));
        holder.txtStatus.setBackgroundColor(context.getColor(R.color.secondary_container));
        break;
      default:
        holder.txtStatus.setTextColor(context.getColor(R.color.primary));
        holder.txtStatus.setBackgroundColor(context.getColor(R.color.primary_container));
        break;
    }

    boolean showHint = swipeEnabled && pending && hintShown.contains(a.getId());
    holder.footerRow.setVisibility(showHint ? View.GONE : View.VISIBLE);
    holder.txtSwipeHint.setVisibility(showHint ? View.VISIBLE : View.GONE);
    holder.divider.setVisibility(View.GONE);

    float density = context.getResources().getDisplayMetrics().density;
    float radius = density * 18f;
    boolean isFirst = position == 0;
    boolean isLast = position == getItemCount() - 1;

    GradientDrawable bg = new GradientDrawable();
    bg.setColor(context.getColor(R.color.surface));
    bg.setStroke((int) (density * 1.2f), context.getColor(R.color.outline_variant));
    if (isFirst && isLast) {
      bg.setCornerRadius(radius);
    } else if (isFirst) {
      bg.setCornerRadii(new float[] {radius, radius, radius, radius, 0, 0, 0, 0});
    } else if (isLast) {
      bg.setCornerRadii(new float[] {0, 0, 0, 0, radius, radius, radius, radius});
    } else {
      bg.setCornerRadius(0f);
    }
    holder.itemView.setBackground(bg);

    ViewGroup.MarginLayoutParams lp =
        (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
    if (lp != null) {
      lp.bottomMargin = isLast ? 0 : (int) (density * 4);
      holder.itemView.setLayoutParams(lp);
    }

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
    TextView txtDoctorName, txtSpeciality, txtDate, txtDepartment, txtStatus, txtSwipeHint;
    View footerRow, divider;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      txtDoctorName = itemView.findViewById(R.id.txtDoctorName);
      txtSpeciality = itemView.findViewById(R.id.txtSpeciality);
      txtDate = itemView.findViewById(R.id.txtDate);
      txtDepartment = itemView.findViewById(R.id.txtDepartment);
      txtStatus = itemView.findViewById(R.id.txtStatus);
      txtSwipeHint = itemView.findViewById(R.id.txtSwipeHint);
      footerRow = itemView.findViewById(R.id.footerRow);
      divider = itemView.findViewById(R.id.divider);
    }
  }
}

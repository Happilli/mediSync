package com.bca.medisync.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.data.model.Appointment;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Appointment appointment);
    }

    private final Context context;
    private final List<Appointment> appointments;
    private final OnItemClickListener listener;
    private final boolean showPatientView;

    public AppointmentAdapter(Context context,
                              List<Appointment> appointments,
                              boolean showPatientView,
                              OnItemClickListener listener) {
        this.context = context;
        this.appointments = appointments;
        this.showPatientView = showPatientView;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Appointment appointment = appointments.get(position);

        if (showPatientView) {
            holder.txtDoctorName.setText(appointment.getPatientName());
        } else {
            holder.txtDoctorName.setText(appointment.getDoctorName());
        }

        holder.txtSpeciality.setText(appointment.getSpeciality());
        holder.txtDepartment.setText(appointment.getDepartment());
        holder.txtStatus.setText(appointment.getStatus());
        holder.txtDate.setText(appointment.getDate() + " - " + appointment.getTime());

        switch (appointment.getStatus()) {

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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(appointment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointments == null ? 0 : appointments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtDoctorName;
        TextView txtSpeciality;
        TextView txtDate;
        TextView txtDepartment;
        TextView txtStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtDoctorName = itemView.findViewById(R.id.txtDoctorName);
            txtSpeciality = itemView.findViewById(R.id.txtSpeciality);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtDepartment = itemView.findViewById(R.id.txtDepartment);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}
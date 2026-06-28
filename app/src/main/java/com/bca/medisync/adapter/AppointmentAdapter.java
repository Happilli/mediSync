package com.bca.medisync.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.R;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
    public interface OnItemClickListener{
       void onItemClick(Appointment appointment);
   }
   private final Context context;
   private final List<Appointment> appointments;
   private final OnItemClickListener listener;
   private final boolean showPatientView;


   public AppointmentAdapter(Context context, List<Appointment> appointments, Boolean showPatientView, OnItemClickListener listener){
       this.context =context;
       this.appointments = appointments;
       this.showPatientView = showPatientView;
       this.listener  = listener;
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

//        holder.txtDoctorName.setText(a.getDoctorName());
        holder.txtDoctorName.setText(showPatientView ? a.getPatientName() : a.getDoctorName());
        holder.txtSpeciality.setText(a.getSpeciality());
        holder.txtDepartment.setText(a.getDepartment());
        holder.txtStatus.setText(a.getStatus());
        holder.txtDate.setText(a.getDate() + " - " + a.getTime());


        switch (a.getStatus()){
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
        holder.itemView.setOnClickListener(v->listener.onItemClick(a));
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
       TextView txtDoctorName, txtSpeciality, txtDate, txtDepartment, txtStatus;
       public ViewHolder(@NonNull View itemView){
           super(itemView);
           txtDoctorName = itemView.findViewById(R.id.txtDoctorName);
           txtSpeciality = itemView.findViewById(R.id.txtSpeciality);
           txtDate = itemView.findViewById(R.id.txtDate);
           txtDepartment = itemView.findViewById(R.id.txtDepartment);
            txtStatus = itemView.findViewById(R.id.txtStatus);
       }
    }
}

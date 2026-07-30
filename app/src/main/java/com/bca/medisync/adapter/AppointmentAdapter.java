package com.bca.medisync.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.data.remote.ApiClient;
import com.bumptech.glide.Glide;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(Appointment appointment);
    }

    private final Context context;
    private List<Appointment> appointments;
    private final OnItemClickListener listener;
    private final boolean showPatientView;


    public AppointmentAdapter(Context context, List<Appointment> appointments, Boolean showPatientView, OnItemClickListener listener) {
        this.context = context;
        this.appointments = appointments;
        this.showPatientView = showPatientView;
        this.listener = listener;
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

        if (showPatientView) {
            // Patient's view: Show Doctor info
            holder.txtPrimaryName.setText(a.getDoctorName() == null ? "" : a.getDoctorName());
            holder.txtSecondaryInfo.setText(a.getSpeciality());
            holder.txtPhone.setVisibility(View.GONE);
            holder.imgProfile.setImageResource(R.drawable.ic_nav_profile);
            holder.imgProfile.setImageTintList(android.content.res.ColorStateList.valueOf(context.getColor(R.color.primary)));
        } else {
            // Doctor's view: Show Patient info
            holder.txtPrimaryName.setText(a.getPatientName() == null ? "" : a.getPatientName());
            String gender = a.getPatientGender() != null ? a.getPatientGender() : "";
            String blood = a.getPatientBloodGroup() != null ? a.getPatientBloodGroup() : "";
            if (!gender.isEmpty() && !blood.isEmpty()) {
                holder.txtSecondaryInfo.setText(gender + " • " + blood);
            } else {
                holder.txtSecondaryInfo.setText(gender + blood);
            }

            if (a.getPatientPhone() != null && !a.getPatientPhone().isEmpty()) {
                holder.txtPhone.setText(a.getPatientPhone());
                holder.txtPhone.setVisibility(View.VISIBLE);
            } else {
                holder.txtPhone.setVisibility(View.GONE);
            }

            if (a.getPatientProfilePicUrl() != null && !a.getPatientProfilePicUrl().isEmpty()) {
                String imageUrl = ApiClient.BASE_URL.replaceAll("/$", "") + "/api/v1" + a.getPatientProfilePicUrl();
                holder.imgProfile.setImageTintList(null); // Clear tint for actual profile pictures
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_nav_profile)
                        .error(R.drawable.ic_nav_profile)
                        .centerCrop()
                        .into(holder.imgProfile);
            } else {
                holder.imgProfile.setImageResource(R.drawable.ic_nav_profile);
                holder.imgProfile.setImageTintList(android.content.res.ColorStateList.valueOf(context.getColor(R.color.primary)));
            }
        }

        if (holder.txtStatus != null) holder.txtStatus.setText(a.getStatus());
        if (holder.txtDate != null) holder.txtDate.setText(a.getDate() + " - " + a.getTime());
        if (holder.txtDepartment != null) holder.txtDepartment.setText(a.getDepartment());

        if (a.getStatus() != null && holder.txtStatus != null) {
            holder.txtStatus.setTextColor(context.getColor(R.color.white));
            switch (a.getStatus().toLowerCase()) {
                case "confirmed":
                case "completed":
                    holder.txtStatus.setBackgroundTintList(context.getColorStateList(R.color.status_completed));
                    break;
                case "pending":
                    holder.txtStatus.setBackgroundTintList(context.getColorStateList(R.color.status_pending));
                    break;
                case "cancelled":
                    holder.txtStatus.setBackgroundTintList(context.getColorStateList(R.color.status_cancelled));
                    break;
                default:
                    holder.txtStatus.setBackgroundTintList(context.getColorStateList(R.color.primary));
                    break;
            }
        }
        holder.itemView.setOnClickListener(v -> listener.onItemClick(a));
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void updateList(List<Appointment> newList) {
        this.appointments.clear();
        this.appointments.addAll(newList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPrimaryName, txtSecondaryInfo, txtDate, txtDepartment, txtStatus, txtPhone;
        ImageView imgProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPrimaryName = itemView.findViewById(R.id.txtPrimaryName);
            txtSecondaryInfo = itemView.findViewById(R.id.txtSecondaryInfo);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtDepartment = itemView.findViewById(R.id.txtDepartment);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtPhone = itemView.findViewById(R.id.txtPhone);
            imgProfile = itemView.findViewById(R.id.imgProfile);
        }
    }
}

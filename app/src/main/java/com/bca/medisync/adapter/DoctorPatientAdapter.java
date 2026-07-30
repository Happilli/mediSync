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
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.dto.patient.DoctorPatientResponse;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class DoctorPatientAdapter extends RecyclerView.Adapter<DoctorPatientAdapter.ViewHolder> {

    private static final String TAG = "DoctorPatientAdapter";

    public interface OnItemClickListener {
        void onItemClick(DoctorPatientResponse patient);
    }

    private final Context context;
    private final List<DoctorPatientResponse> patients;
    private final List<DoctorPatientResponse> filteredPatients;
    private final OnItemClickListener listener;

    public DoctorPatientAdapter(Context context, List<DoctorPatientResponse> patients, OnItemClickListener listener) {
        this.context = context;
        this.patients = new ArrayList<>(patients);
        this.filteredPatients = new ArrayList<>(this.patients);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor_patient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoctorPatientResponse patient = filteredPatients.get(position);

        holder.txtName.setText(patient.getName());
        holder.txtPhone.setText(patient.getPhone());
        holder.txtBloodGroup.setText(patient.getBlood_group());
        holder.txtGender.setText(capitalize(patient.getGender()));

        // Display appointment count if available, otherwise hide or set default
        holder.txtStats.setText("• 0 Appointments");

        // Hide last appointment by default as it's not in DTO yet
        holder.txtLastAppointment.setVisibility(View.GONE);

        if (patient.getProfile_pic_url() != null && !patient.getProfile_pic_url().isEmpty()) {
            String imageUrl = ApiClient.BASE_URL.replaceAll("/$", "") + "/api/v1" + patient.getProfile_pic_url();
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_nav_profile)
                    .error(R.drawable.ic_nav_profile)
                    .centerCrop()
                    .into(holder.imgProfile);
        } else {
            holder.imgProfile.setImageResource(R.drawable.ic_nav_profile);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(patient));
    }

    @Override
    public int getItemCount() {
        return filteredPatients.size();
    }

    public void updateList(List<DoctorPatientResponse> newList) {
        this.patients.clear();
        if (newList != null) {
            this.patients.addAll(newList);
        }
        this.filteredPatients.clear();
        this.filteredPatients.addAll(this.patients);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredPatients.clear();
        if (query == null || query.isEmpty()) {
            filteredPatients.addAll(patients);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (DoctorPatientResponse p : patients) {
                if (p.getName().toLowerCase().contains(lowerCaseQuery) ||
                        p.getPhone().contains(lowerCaseQuery)) {
                    filteredPatients.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile;
        TextView txtName, txtPhone, txtBloodGroup, txtGender, txtStats, txtLastAppointment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtName = itemView.findViewById(R.id.txtName);
            txtPhone = itemView.findViewById(R.id.txtPhone);
            txtBloodGroup = itemView.findViewById(R.id.txtBloodGroup);
            txtGender = itemView.findViewById(R.id.txtGender);
            txtStats = itemView.findViewById(R.id.txtStats);
            txtLastAppointment = itemView.findViewById(R.id.txtLastAppointment);
        }
    }
}
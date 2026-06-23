package com.bca.medisync.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.data.model.Patient;
import com.bca.medisync.R;

import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.ViewHolder> {
    public  interface  OnItemClickListener{
        void onItemClick(Patient patient);
    }
    private final Context context;
    private  final List<Patient> patients;
    private final OnItemClickListener listener;
    public PatientAdapter( Context context, List<Patient> patients, OnItemClickListener listener){
        this.context = context;
        this.patients = patients;
        this.listener= listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_patient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Patient p=patients.get(position);
        holder.txtName.setText(p.getName());
        holder.txtBloodGroup.setText(p.getBloodGroup());
        holder.txtPhone.setText(p.getPhone());
        holder.itemView.setOnClickListener(v -> {
           listener.onItemClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView txtName, txtBloodGroup, txtPhone;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            txtName = itemView.findViewById(R.id.txtPatientName);
            txtBloodGroup = itemView.findViewById(R.id.txtBloodGroup);
            txtPhone = itemView.findViewById(R.id.txtPhone);
        }
    }
}

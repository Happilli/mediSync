package com.bca.medisync.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.data.model.Medication;
import com.bca.medisync.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {
    public interface OnItemClickListener{
        void onItemClick(Medication medication);
    }
    private final Context context;
    private final List<Medication> medications;
    private final OnItemClickListener listener;

    public MedicationAdapter(Context context, List<Medication> medications, OnItemClickListener listener){
        this.context = context;
        this.medications  = medications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication,  parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medication med = medications.get(position);
        holder.tvMedName.setText(med.getName()+ " "+med.getDosage());
        holder.tvMedFrequency.setText(med.getFrequency());
        holder.tvMedTime.setText(med.getTime());
        holder.itemView.setOnClickListener(v->listener.onItemClick(med));
    }

    @Override
    public int getItemCount(){
        return medications.size();
    }
    public class ViewHolder extends  RecyclerView.ViewHolder{
        TextView tvMedName, tvMedFrequency, tvMedTime;
        public ViewHolder(@NotNull View itemView){
            super(itemView);
            tvMedFrequency = itemView.findViewById(R.id.tvMedFrequency);
            tvMedName = itemView.findViewById(R.id.tvMedName);
            tvMedTime = itemView.findViewById(R.id.tvMedTime);
        }
    }
}

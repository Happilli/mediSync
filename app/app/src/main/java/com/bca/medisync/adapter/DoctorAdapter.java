package com.bca.medisync.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.bca.medisync.R;
import com.bca.medisync.data.model.Doctor;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;



public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    public interface OnItemClickListener{
        void onBookClick(Doctor doctor);
    }
    private final Context context;
    private List<Doctor> doctors;
    private final List<Doctor> allDoctors;
    private final OnItemClickListener listener;

    public DoctorAdapter(Context context, List<Doctor> doctors, OnItemClickListener listener){
        this.context = context;
        this.doctors = doctors;
        this.allDoctors = new ArrayList<>(doctors);
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);
        holder.txtName.setText(doctor.getName());
        holder.txtSpeciality.setText(doctor.getSpeciality());
        holder.txtInfo.setText(doctor.getInfo());
        holder.btnBook.setOnClickListener(v->{
            listener.onBookClick(doctor);
        });
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    public void filter(String query) {
        if (query.isEmpty()) {
            doctors = new ArrayList<>(allDoctors);
        } else {
            List<Doctor> filtered = new ArrayList<>();
            for (Doctor d : allDoctors) {
                if (d.getName().toLowerCase().contains(query.toLowerCase()) || d.getSpeciality().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(d);
                }
            }
            doctors = filtered;
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
       TextView txtName, txtSpeciality, txtInfo;
       Button btnBook;

       public ViewHolder(@NonNull View itemView){
           super(itemView);
           txtName = itemView.findViewById(R.id.txtDoctorName);
           txtSpeciality = itemView.findViewById(R.id.txtSpeciality);
           txtInfo = itemView.findViewById(R.id.txtInfo);
           btnBook = itemView.findViewById(R.id.btnBook);
       }
    }
}

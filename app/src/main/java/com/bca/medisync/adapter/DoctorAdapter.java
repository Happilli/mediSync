package com.bca.medisync.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bca.medisync.R;
import com.bca.medisync.data.model.Doctor;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

  public interface OnItemClickListener {
    void onBookClick(Doctor doctor);
  }

  private final Context context;
  private List<Doctor> doctors;
  private final List<Doctor> allDoctors;
  private final OnItemClickListener listener;

  public DoctorAdapter(Context context, List<Doctor> doctors, OnItemClickListener listener) {
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

    if (doctor.getImageUrl() != null && !doctor.getImageUrl().isEmpty()) {
      Glide.with(context)
          .load(doctor.getImageUrl())
          .placeholder(R.drawable.stethoscope)
          .error(R.drawable.stethoscope)
          .into(holder.imgDoctor);
    } else {
      holder.imgDoctor.setImageResource(R.drawable.stethoscope);
    }
    holder.btnBook.setOnClickListener(
        v -> {
          listener.onBookClick(doctor);
        });
  }

  @Override
  public int getItemCount() {
    return doctors.size();
  }

  public void updateData(List<Doctor> newDoctors) {
    this.doctors = newDoctors;
    this.allDoctors.clear();
    this.allDoctors.addAll(newDoctors);
    notifyDataSetChanged();
  }

  public void filter(String query) {
    if (query.isEmpty()) {
      doctors = new ArrayList<>(allDoctors);
    } else {
      List<Doctor> filtered = new ArrayList<>();
      for (Doctor d : allDoctors) {
        if (d.getName().toLowerCase().contains(query.toLowerCase())
            || d.getSpeciality().toLowerCase().contains(query.toLowerCase())) {
          filtered.add(d);
        }
      }
      doctors = filtered;
    }
    notifyDataSetChanged();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView txtName, txtSpeciality, txtInfo;
    ImageView imgDoctor;
    Button btnBook;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      txtName = itemView.findViewById(R.id.txtDoctorName);
      txtSpeciality = itemView.findViewById(R.id.txtSpeciality);
      txtInfo = itemView.findViewById(R.id.txtInfo);
      imgDoctor = itemView.findViewById(R.id.imgDoctor);
      btnBook = itemView.findViewById(R.id.btnBook);
    }
  }
}

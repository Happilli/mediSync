package com.bca.medisync.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.data.model.Hospital;
import com.bca.medisync.R;

import java.util.ArrayList;
import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.ViewHolder> {

  public interface OnItemClickListener {
    void onItemClick(Hospital hospital);
  }

  private final Context context;
  private List<Hospital> hospitals;
  private final List<Hospital> allHospitals;
  private final OnItemClickListener listener;

  public HospitalAdapter(Context context, List<Hospital> hospitals, OnItemClickListener listener) {
    this.context = context;
    this.hospitals = hospitals;
    this.allHospitals = new java.util.ArrayList<>(hospitals);
    this.listener = listener;
  }

  public void updateData(List<Hospital> newHospitals) {
    this.hospitals = newHospitals;
    this.allHospitals.clear();
    this.allHospitals.addAll(newHospitals);
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_hospital, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Hospital hospital = hospitals.get(position);
    holder.txtName.setText(hospital.getName());
    holder.txtAddress.setText(hospital.getAddress());
    holder.txtRating.setText(hospital.getRating() > 0 ? "0" + hospital.getRating() : "");
    holder.btnViewMore.setOnClickListener(
        v -> {
          listener.onItemClick(hospital);
        });
  }

  @Override
  public int getItemCount() {
    return hospitals.size();
  }
  ;

  public void filter(String query) {
    if (query.isEmpty()) {
      hospitals = new java.util.ArrayList<>(allHospitals);
    } else {
      List<Hospital> filtered = new ArrayList<>();
      for (Hospital h : allHospitals) {
        if (h.getName().toLowerCase().contains(query.toLowerCase())
            || h.getAddress().toLowerCase().contains(query.toLowerCase())) {
          filtered.add(h);
        }
      }
      hospitals = filtered;
    }
    notifyDataSetChanged();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView txtName, txtAddress, txtRating;
    android.widget.Button btnViewMore;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      txtName = itemView.findViewById(R.id.txtHospitalName);
      txtAddress = itemView.findViewById(R.id.txtHospitalAddress);
      txtRating = itemView.findViewById(R.id.txtHospitalRating);
      btnViewMore = itemView.findViewById(R.id.btnViewMore);
    }
  }
}

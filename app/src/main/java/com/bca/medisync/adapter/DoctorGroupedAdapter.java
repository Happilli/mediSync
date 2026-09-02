package com.bca.medisync.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bca.medisync.R;
import com.bca.medisync.data.model.Doctor;
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.RoundedListStyler;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DoctorGroupedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int TYPE_HEADER = 0;
  private static final int TYPE_ITEM = 1;

  public interface Callbacks {
    void onToggleExpand(Doctor doctor);

    void onBookClicked(Doctor doctor);

    String getHospitalName(int hospitalId, TextView target);
  }

  private static class Row {
    final boolean isHeader;
    final String headerLabel;
    final Doctor doctor;
    final int positionInGroup;
    final int groupSize;

    Row(String headerLabel) {
      this.isHeader = true;
      this.headerLabel = headerLabel;
      this.doctor = null;
      this.positionInGroup = 0;
      this.groupSize = 0;
    }

    Row(Doctor doctor, int positionInGroup, int groupSize) {
      this.isHeader = false;
      this.headerLabel = null;
      this.doctor = doctor;
      this.positionInGroup = positionInGroup;
      this.groupSize = groupSize;
    }
  }

  private final List<Row> rows = new ArrayList<>();
  private final Set<String> expandedIds;
  private final Callbacks callbacks;
  private final androidx.fragment.app.Fragment fragment;

  public DoctorGroupedAdapter(
      androidx.fragment.app.Fragment fragment, Set<String> expandedIds, Callbacks callbacks) {
    this.fragment = fragment;
    this.expandedIds = expandedIds;
    this.callbacks = callbacks;
  }

  public void submitList(List<Doctor> doctors) {
    rows.clear();
    Map<String, List<Doctor>> grouped = new LinkedHashMap<>();
    for (Doctor d : doctors) {
      String key =
          (d.getDepartment() == null || d.getDepartment().isEmpty())
              ? "General"
              : d.getDepartment();
      grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(d);
    }
    for (Map.Entry<String, List<Doctor>> entry : grouped.entrySet()) {
      rows.add(new Row(entry.getKey()));
      List<Doctor> group = entry.getValue();
      for (int i = 0; i < group.size(); i++) {
        rows.add(new Row(group.get(i), i, group.size()));
      }
    }
    notifyDataSetChanged();
  }

  @Override
  public int getItemViewType(int position) {
    return rows.get(position).isHeader ? TYPE_HEADER : TYPE_ITEM;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == TYPE_HEADER) {
      View v =
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.item_medication_group_header, parent, false);
      return new HeaderVH(v);
    }
    View v =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor_row, parent, false);
    return new ItemVH(v);
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    Row row = rows.get(position);
    if (row.isHeader) {
      ((HeaderVH) holder).txtHeader.setText(row.headerLabel);
      return;
    }

    ItemVH h = (ItemVH) holder;
    Doctor doctor = row.doctor;

    h.txtDoctorName.setText(doctor.getName());
    h.txtSpeciality.setText(doctor.getSpeciality());
    h.txtInfo.setText(doctor.getInfo());
    ImageLoader.loadDoctorImage(fragment, h.imgDoctor, doctor.getImageUrl());

    boolean expanded = expandedIds.contains(doctor.getId());
    h.dividerExpand.setVisibility(expanded ? View.VISIBLE : View.GONE);
    h.detailContainer.setVisibility(expanded ? View.VISIBLE : View.GONE);
    h.imgExpandArrow.setRotation(expanded ? 90f : 0f);

    if (expanded) {
      boolean hasBio = doctor.getBio() != null && !doctor.getBio().isEmpty();
      h.txtBio.setVisibility(hasBio ? View.VISIBLE : View.GONE);
      h.txtBio.setText(doctor.getBio());
      h.txtDoctorAddress.setText(doctor.getAddress());
      h.txtDoctorPhone.setText(doctor.getPhone());
      h.txtHospitalName.setText(
          callbacks.getHospitalName(doctor.getHospitalId(), h.txtHospitalName));
      h.btnBook.setOnClickListener(v -> callbacks.onBookClicked(doctor));
    }

    h.itemView.setOnClickListener(v -> callbacks.onToggleExpand(doctor));

    RoundedListStyler.apply(h.itemView, row.positionInGroup, row.groupSize);
  }

  @Override
  public int getItemCount() {
    return rows.size();
  }

  static class HeaderVH extends RecyclerView.ViewHolder {
    TextView txtHeader;

    HeaderVH(View v) {
      super(v);
      txtHeader = v.findViewById(R.id.txtGroupHeader);
    }
  }

  static class ItemVH extends RecyclerView.ViewHolder {
    TextView txtDoctorName,
        txtSpeciality,
        txtInfo,
        txtBio,
        txtDoctorAddress,
        txtHospitalName,
        txtDoctorPhone;
    ImageView imgDoctor, imgExpandArrow;
    View dividerExpand, detailContainer;
    com.google.android.material.button.MaterialButton btnBook;

    ItemVH(View v) {
      super(v);
      txtDoctorName = v.findViewById(R.id.txtDoctorName);
      txtSpeciality = v.findViewById(R.id.txtSpeciality);
      txtInfo = v.findViewById(R.id.txtInfo);
      imgDoctor = v.findViewById(R.id.imgDoctor);
      imgExpandArrow = v.findViewById(R.id.imgExpandArrow);
      dividerExpand = v.findViewById(R.id.dividerExpand);
      detailContainer = v.findViewById(R.id.detailContainer);
      txtBio = v.findViewById(R.id.txtBio);
      txtDoctorAddress = v.findViewById(R.id.txtDoctorAddress);
      txtHospitalName = v.findViewById(R.id.txtHospitalName);
      txtDoctorPhone = v.findViewById(R.id.txtDoctorPhone);
      btnBook = v.findViewById(R.id.btnBook);
    }
  }
}

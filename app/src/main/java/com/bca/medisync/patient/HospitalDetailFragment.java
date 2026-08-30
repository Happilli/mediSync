package com.bca.medisync.patient;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bca.medisync.R;
import com.bca.medisync.util.ImageLoader;
import com.google.android.material.button.MaterialButton;

public class HospitalDetailFragment extends Fragment {
  private TextView txtName, txtRating, txtDescription, txtAddress;
  private MaterialButton btnBack, btnCall, btnWebsite, btnDirection, btnSeeDoctors;

  private String hospitalId;
  private String hospitalName;
  private String hospitalPhone;
  private String hospitalWebsite;
  private String hospitalAddress;
  private String hospitalDescription;

  private ImageView imgHospital;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_hospital_detail, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    loadData();
    setupListeners();
  }

  private void initViews(View view) {
    txtName = view.findViewById(R.id.txtHospitalName);
    txtRating = view.findViewById(R.id.txtRating);
    txtDescription = view.findViewById(R.id.txtDescription);
    txtAddress = view.findViewById(R.id.txtAddress);
    btnBack = view.findViewById(R.id.btnBack);
    btnCall = view.findViewById(R.id.btnCall);
    btnWebsite = view.findViewById(R.id.btnWebsite);
    btnDirection = view.findViewById(R.id.btnDirection);
    btnSeeDoctors = view.findViewById(R.id.btnSeeDoctors);
    imgHospital = view.findViewById(R.id.imgHospital);
  }

  private void loadData() {
    Bundle args = getArguments();
    if (args == null) return;

    hospitalId = args.getString("hospital_id");
    hospitalName = args.getString("hospital_name");
    hospitalPhone = args.getString("hospital_phone");
    hospitalWebsite = args.getString("hospital_website");
    hospitalAddress = args.getString("hospital_address");
    hospitalDescription = args.getString("hospital_description");
    double hospitalRating = args.getDouble("hospital_rating", 0.0);

    txtName.setText(hospitalName);
    txtRating.setText("+ " + hospitalRating);
    txtDescription.setText(hospitalDescription);
    txtAddress.setText(hospitalAddress);
    String imageUrl = args.getString("hospital_image_url");
    ImageLoader.loadHospitalImage(this, imgHospital, imageUrl);
  }

  private void setupListeners() {
    btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

    btnCall.setOnClickListener(
        v -> {
          Intent intent = new Intent(Intent.ACTION_DIAL);
          intent.setData(Uri.parse("tel:" + hospitalPhone));
          startActivity(intent);
        });

    btnWebsite.setOnClickListener(
        v -> {
          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(hospitalWebsite));
          startActivity(intent);
        });

    btnDirection.setOnClickListener(
        v -> {
          Intent intent =
              new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + hospitalAddress));
          startActivity(intent);
        });

    btnSeeDoctors.setOnClickListener(
        v -> {
          Bundle args = new Bundle();
          args.putString("hospital_id", hospitalId);

          DoctorFragment fragment = new DoctorFragment();
          fragment.setArguments(args);
          ((MainTabActivity) requireActivity()).pushFragment(fragment);
        });
  }
}

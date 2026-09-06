package com.bca.medisync.patient;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bca.medisync.BaseBindingFragment;
import com.bca.medisync.databinding.FragmentHospitalDetailBinding;
import com.bca.medisync.util.ImageLoader;
import com.bca.medisync.util.ViewUtils;

public class HospitalDetailFragment extends BaseBindingFragment<FragmentHospitalDetailBinding> {
  private String hospitalId;
  private String hospitalName;
  private String hospitalPhone;
  private String hospitalWebsite;
  private String hospitalAddress;
  private String hospitalDescription;

  @Override
  protected FragmentHospitalDetailBinding inflateBinding(
      LayoutInflater inflater, ViewGroup container) {
    return FragmentHospitalDetailBinding.inflate(inflater, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    loadData();
    setupListeners();
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
    binding.txtHospitalName.setText(hospitalName);
    binding.txtRating.setText("+ " + hospitalRating);
    binding.txtDescription.setText(hospitalDescription);
    binding.txtAddress.setText(hospitalAddress);
    String imageUrl = args.getString("hospital_image_url");
    ImageLoader.loadHospitalImage(this, binding.imgHospital, imageUrl);
  }

  private void setupListeners() {
    ViewUtils.setupBackNav(this, binding.btnBack);
    binding.btnCall.setOnClickListener(
        v -> {
          Intent intent = new Intent(Intent.ACTION_DIAL);
          intent.setData(Uri.parse("tel:" + hospitalPhone));
          startActivity(intent);
        });
    binding.btnWebsite.setOnClickListener(
        v -> {
          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(hospitalWebsite));
          startActivity(intent);
        });
    binding.btnDirection.setOnClickListener(
        v -> {
          Intent intent =
              new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + hospitalAddress));
          startActivity(intent);
        });
    binding.btnSeeDoctors.setOnClickListener(
        v -> {
          Bundle args = new Bundle();
          args.putString("hospital_id", hospitalId);
          DoctorFragment fragment = new DoctorFragment();
          fragment.setArguments(args);
          ((MainTabActivity) requireActivity()).pushFragment(fragment);
        });
  }
}

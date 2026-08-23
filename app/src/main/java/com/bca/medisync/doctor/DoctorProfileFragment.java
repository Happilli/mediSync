package com.bca.medisync.doctor;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bca.medisync.R;
import com.bca.medisync.data.model.AvailabilityDay;
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.DoctorProfile;
import com.google.android.material.card.MaterialCardView;

public class DoctorProfileFragment extends Fragment {

  private LinearLayout availabilityContainer;
  private DoctorProfile profile;

  public DoctorProfileFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_doctor_profile, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    availabilityContainer = view.findViewById(R.id.availabilityContainer);

    profile = DataProvider.getCurrentDoctorProfile();
    bindHeader(view);
    bindInfoGrid(view);
    bindAvailability();
    bindStatistics(view);
  }

  private void bindHeader(View view) {
    ((TextView) view.findViewById(R.id.txtDoctorName)).setText(profile.getName());
    ((TextView) view.findViewById(R.id.txtRole)).setText(profile.getRole());
    ((TextView) view.findViewById(R.id.txtRegistrationBadge)).setText(profile.getRegistrationNo());
    ((TextView) view.findViewById(R.id.txtQualification)).setText(profile.getQualification());
    ((TextView) view.findViewById(R.id.txtExperience))
        .setText(profile.getExperienceYears() + " Years Experience");
    ((TextView) view.findViewById(R.id.txtPhoneHeader)).setText(profile.getPhone());
    ((TextView) view.findViewById(R.id.txtEmailHeader)).setText(profile.getEmail());
    ((TextView) view.findViewById(R.id.txtHospitalName)).setText(profile.getHospitalRole());
    ((TextView) view.findViewById(R.id.txtHospitalRole)).setText(profile.getHospitalRole());
  }

  private void bindInfoGrid(View view) {
    setInfoCard(view, R.id.cardSpecialization, "SPECIALIZATION", profile.getSpecialization());
    setInfoCard(view, R.id.cardExperience, "EXPERIENCE", profile.getSpecialization());
    setInfoCard(view, R.id.cardQualification, "QUALIFICATION", profile.getQualification());
    setInfoCard(view, R.id.cardRegistration, "REGISTRATION NO:", profile.getRegistrationNo());
    setInfoCard(view, R.id.cardEmail, "EMAIL", profile.getEmail());
    setInfoCard(view, R.id.cardPhone, "PHONE", profile.getPhone());
  }

  private void setInfoCard(View root, int cardId, String title, String value) {
    MaterialCardView card = root.findViewById(cardId);
    ((TextView) card.findViewById(R.id.lblTitle)).setText(title);
    ((TextView) card.findViewById(R.id.lblValue)).setText(value);
  }

  private void bindAvailability() {
    for (AvailabilityDay a : profile.getAvailability()) {
      LinearLayout item = new LinearLayout(requireContext());
      item.setOrientation(LinearLayout.VERTICAL);
      item.setGravity(Gravity.CENTER);
      item.setBackgroundColor(requireContext().getColor(R.color.tertiary_container));
      item.setPadding(dp(14), dp(12), dp(14), dp(12));

      LinearLayout.LayoutParams lp =
          new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
      lp.setMarginEnd(dp(8));
      item.setLayoutParams(lp);

      TextView day = new TextView(requireContext());
      day.setText(a.getDay());
      day.setTextSize(13);
      day.setTypeface(null, android.graphics.Typeface.BOLD);
      day.setTextColor(requireContext().getColor(R.color.on_tertiary_container));
      day.setGravity(Gravity.CENTER);

      TextView time = new TextView(requireContext());
      time.setText(a.getStartTime() + "\n-\n" + a.getEndtime());
      time.setTextSize(11);
      time.setTextColor(requireContext().getColor(R.color.on_tertiary_container));
      time.setGravity(Gravity.CENTER);

      item.addView(day);
      item.addView(time);
      availabilityContainer.addView(item);
    }
  }

  private void bindStatistics(View view) {
    setStat(
        view, R.id.statPatientsMonth, profile.getPatientsThisMonth() + "+", "patients this month");
    setStat(
        view, R.id.statFeedback, profile.getPositiveFeedbackPercent() + "%", "positive feedback");
    setStat(view, R.id.statPatientsTotal, profile.getTotalPatients() + "+", "Patients Total");
    setStat(view, R.id.statRating, profile.getRating() + "+", "Rating");
  }

  private void setStat(View root, int cardId, String value, String label) {
    MaterialCardView card = root.findViewById(cardId);
    ((TextView) card.findViewById(R.id.statValue)).setText(value);
    ((TextView) card.findViewById(R.id.statLabel)).setText(label);
  }

  private int dp(int v) {
    return (int) (v * getResources().getDisplayMetrics().density);
  }
}

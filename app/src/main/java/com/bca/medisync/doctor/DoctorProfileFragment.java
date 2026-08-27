package com.bca.medisync.doctor;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.MainActivity;
import com.bca.medisync.R;
import com.bca.medisync.data.local.SessionManager;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.api.HospitalApi;
import com.bca.medisync.data.remote.dto.TimeSlotResponse;
import com.bca.medisync.data.remote.dto.doctor.DoctorProfileResponse;
import com.bca.medisync.data.remote.dto.doctor.TimeSlotCreateRequest;
import com.bca.medisync.data.remote.dto.hospital.HospitalResponse;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorProfileFragment extends Fragment {

  private RecyclerView rvAvailability;
  private TextView txtNoAvailability;
  private SessionManager sessionManager;
  private DoctorProfileResponse currentProfile;

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
    sessionManager = new SessionManager(requireContext());

    rvAvailability = view.findViewById(R.id.rvAvailability);
    txtNoAvailability = view.findViewById(R.id.txtNoAvailability);

    rvAvailability.setLayoutManager(new LinearLayoutManager(requireContext()));

    setupListeners(view);
    loadProfile();
  }

  @Override
  public void onResume() {
    super.onResume();
    loadProfile();
  }

  private void setupListeners(View view) {
    view.findViewById(R.id.btnLogoutDoctor)
        .setOnClickListener(
            v -> {
              sessionManager.clearSession();
              Intent intent = new Intent(requireContext(), MainActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
              startActivity(intent);
              requireActivity().finish();
            });

    view.findViewById(R.id.btnEditProfile)
        .setOnClickListener(
            v -> {
              Intent intent = new Intent(requireContext(), DoctorEditProfileActivity.class);
              startActivity(intent);
            });

    view.findViewById(R.id.btnAddAvailability).setOnClickListener(v -> showAddAvailabilityDialog());
  }

  private void showAddAvailabilityDialog() {
    Calendar calendar = Calendar.getInstance();
    new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
              calendar.set(Calendar.YEAR, year);
              calendar.set(Calendar.MONTH, month);
              calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

              new TimePickerDialog(
                      requireContext(),
                      (view1, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        calendar.set(Calendar.SECOND, 0);

                        SimpleDateFormat sdf =
                            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                        addTimeSlot(sdf.format(calendar.getTime()));
                      },
                      calendar.get(Calendar.HOUR_OF_DAY),
                      calendar.get(Calendar.MINUTE),
                      false)
                  .show();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH))
        .show();
  }

  private void addTimeSlot(String isoDate) {
    DoctorApi api = ApiClient.getRetrofit().create(DoctorApi.class);
    api.addTimeSlot(new TimeSlotCreateRequest(isoDate))
        .enqueue(
            new Callback<TimeSlotResponse>() {
              @Override
              public void onResponse(
                  Call<TimeSlotResponse> call, Response<TimeSlotResponse> response) {
                if (response.isSuccessful()) {
                  Toast.makeText(requireContext(), "Availability added.", Toast.LENGTH_SHORT)
                      .show();
                  if (currentProfile != null) loadAvailability(currentProfile.getId());
                } else {
                  Toast.makeText(requireContext(), "Failed to add availability.", Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<TimeSlotResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
              }
            });
  }

  private void loadProfile() {
    DoctorApi api = ApiClient.getRetrofit().create(DoctorApi.class);
    api.getMyProfile()
        .enqueue(
            new Callback<DoctorProfileResponse>() {
              @Override
              public void onResponse(
                  Call<DoctorProfileResponse> call, Response<DoctorProfileResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                  currentProfile = response.body();
                  bindProfile(currentProfile);
                  loadHospitalName(currentProfile.getHospital_id());
                  loadAvailability(currentProfile.getId());
                } else {
                  Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<DoctorProfileResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(
                        requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG)
                    .show();
              }
            });
  }

  private void bindProfile(DoctorProfileResponse p) {
    View view = getView();
    if (view == null) return;

    int years = p.getYears_experience() != null ? p.getYears_experience() : 0;

    ((TextView) view.findViewById(R.id.txtDoctorName)).setText(p.getName());
    ((TextView) view.findViewById(R.id.txtRole)).setText(p.getSpeciality());
    ((TextView) view.findViewById(R.id.txtRegistrationBadge))
        .setText(p.isIs_verified() ? "Verified" : "Pending");
    ((TextView) view.findViewById(R.id.txtQualification)).setText(p.getDepartment());
    ((TextView) view.findViewById(R.id.txtExperience)).setText(years + " Years Experience");
    ((TextView) view.findViewById(R.id.txtPhoneHeader)).setText(p.getPhone());
    ((TextView) view.findViewById(R.id.txtEmailHeader)).setText(sessionManager.getEmail());
    ((TextView) view.findViewById(R.id.txtHospitalRole)).setText(p.getDepartment());

    ImageView profileImageView = view.findViewById(R.id.imgDoctorProfile);
    if (profileImageView != null && p.getProfile_pic_url() != null) {
        Glide.with(this)
            .load(ApiClient.BASE_URL.replaceAll("/$", "") + "/api/v1" + p.getProfile_pic_url())
            .placeholder(R.drawable.ic_nav_profile)
            .error(R.drawable.ic_nav_profile)
            .centerCrop()
            .into(profileImageView);
    }

    setInfoCard(view, R.id.cardSpecialization, "SPECIALIZATION", p.getSpeciality());
    setInfoCard(view, R.id.cardExperience, "EXPERIENCE", years + " yrs");
    setInfoCard(view, R.id.cardQualification, "BIO", p.getBio() != null ? p.getBio() : "—");
    setInfoCard(view, R.id.cardRegistration, "ADDRESS", p.getAddress());
    setInfoCard(view, R.id.cardEmail, "EMAIL", sessionManager.getEmail());
    setInfoCard(view, R.id.cardPhone, "PHONE", p.getPhone());

    bindStatistics(view, p);
  }

  private void setInfoCard(View root, int cardId, String title, String value) {
    MaterialCardView card = root.findViewById(cardId);
    ((TextView) card.findViewById(R.id.lblTitle)).setText(title);
    ((TextView) card.findViewById(R.id.lblValue)).setText(value);
  }

  private void bindStatistics(View view, DoctorProfileResponse p) {
    setStat(view, R.id.statPatientsMonth, (p.getPatients_this_month() > 0 ? p.getPatients_this_month() + "+" : "0"), "patients this month");
    setStat(view, R.id.statPatientsTotal, (p.getTotal_patients() > 0 ? p.getTotal_patients() + "+" : "0"), "Patients Total");

    if (p.getPositive_feedback() != null) {
      view.findViewById(R.id.statFeedback).setVisibility(View.VISIBLE);
      setStat(view, R.id.statFeedback, p.getPositive_feedback(), "Positive Feedback");
    } else {
      view.findViewById(R.id.statFeedback).setVisibility(View.GONE);
    }

    if (p.getRating() != null) {
      view.findViewById(R.id.statRating).setVisibility(View.VISIBLE);
      setStat(view, R.id.statRating, p.getRating(), "Rating");
    } else {
      view.findViewById(R.id.statRating).setVisibility(View.GONE);
    }
  }

  private void loadAvailability(int doctorId) {
    DoctorApi api = ApiClient.getRetrofit().create(DoctorApi.class);
    api.getDoctorTimeslots(doctorId, false)
        .enqueue(
            new Callback<List<TimeSlotResponse>>() {
              @Override
              public void onResponse(
                  Call<List<TimeSlotResponse>> call, Response<List<TimeSlotResponse>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                  updateAvailabilityUI(response.body());
                }
              }

              @Override
              public void onFailure(Call<List<TimeSlotResponse>> call, Throwable t) {}
            });
  }

  private void updateAvailabilityUI(List<TimeSlotResponse> slots) {
    if (slots.isEmpty()) {
      txtNoAvailability.setVisibility(View.VISIBLE);
      rvAvailability.setVisibility(View.GONE);
    } else {
      txtNoAvailability.setVisibility(View.GONE);
      rvAvailability.setVisibility(View.VISIBLE);
      rvAvailability.setAdapter(
          new DoctorAvailabilityAdapter(requireContext(), slots, this::deleteAvailability, this::showEditAvailabilityDialog));
    }
  }

  private void showEditAvailabilityDialog(TimeSlotResponse slot) {
    Calendar calendar = Calendar.getInstance();
    Date existing = com.bca.medisync.util.DateTimeUtils.parseIsoToDate(slot.getAppointment_at());
    if (existing != null) calendar.setTime(existing);

    new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
              calendar.set(Calendar.YEAR, year);
              calendar.set(Calendar.MONTH, month);
              calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

              new TimePickerDialog(
                      requireContext(),
                      (view1, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        calendar.set(Calendar.SECOND, 0);

                        SimpleDateFormat sdf =
                            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                        updateTimeSlot(slot.getId(), sdf.format(calendar.getTime()));
                      },
                      calendar.get(Calendar.HOUR_OF_DAY),
                      calendar.get(Calendar.MINUTE),
                      false)
                  .show();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH))
        .show();
  }

  private void updateTimeSlot(int slotId, String isoDate) {
    DoctorApi api = ApiClient.getRetrofit().create(DoctorApi.class);
    api.updateTimeSlot(slotId, new TimeSlotCreateRequest(isoDate))
        .enqueue(
            new Callback<TimeSlotResponse>() {
              @Override
              public void onResponse(
                  Call<TimeSlotResponse> call, Response<TimeSlotResponse> response) {
                if (response.isSuccessful()) {
                  Toast.makeText(requireContext(), "Availability updated.", Toast.LENGTH_SHORT)
                      .show();
                  if (currentProfile != null) loadAvailability(currentProfile.getId());
                }
              }

              @Override
              public void onFailure(Call<TimeSlotResponse> call, Throwable t) {}
            });
  }

  private void deleteAvailability(TimeSlotResponse slot) {
    DoctorApi api = ApiClient.getRetrofit().create(DoctorApi.class);
    api.deleteTimeSlot(slot.getId())
        .enqueue(
            new Callback<Void>() {
              @Override
              public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                  Toast.makeText(requireContext(), "Availability deleted.", Toast.LENGTH_SHORT)
                      .show();
                  if (currentProfile != null) loadAvailability(currentProfile.getId());
                }
              }

              @Override
              public void onFailure(Call<Void> call, Throwable t) {}
            });
  }

  private void setStat(View root, int cardId, String value, String label) {
    MaterialCardView card = root.findViewById(cardId);
    ((TextView) card.findViewById(R.id.statValue)).setText(value);
    ((TextView) card.findViewById(R.id.statLabel)).setText(label);
  }

  private void loadHospitalName(int hospitalId) {
    HospitalApi api = ApiClient.getRetrofit().create(HospitalApi.class);
    api.getHospitalDetail(hospitalId)
        .enqueue(
            new Callback<HospitalResponse>() {
              @Override
              public void onResponse(
                  Call<HospitalResponse> call, Response<HospitalResponse> response) {
                if (!isAdded()) return;
                View view = getView();
                if (view == null) return;
                String name =
                    response.isSuccessful() && response.body() != null
                        ? response.body().getName()
                        : "Hospital #" + hospitalId;
                ((TextView) view.findViewById(R.id.txtHospitalName)).setText(name);
              }

              @Override
              public void onFailure(Call<HospitalResponse> call, Throwable t) {}
            });
  }
}

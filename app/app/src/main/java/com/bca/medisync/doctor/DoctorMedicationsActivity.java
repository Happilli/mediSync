package com.bca.medisync.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.DoctorMedicationAdapter;
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.Medication;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class DoctorMedicationsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rvActiveMedications, rvCompletedMedications;
    private ExtendedFloatingActionButton fabAddMedication;
    private String patientName;

    private List<Medication> medicationList;
    private DoctorMedicationAdapter activeAdapter, completedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_medications);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            findViewById(R.id.appBarLayout).setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        initViews();
        setupToolbar();
        setupRecyclerViews();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvActiveMedications = findViewById(R.id.rvActiveMedications);
        rvCompletedMedications = findViewById(R.id.rvCompletedMedications);
        fabAddMedication = findViewById(R.id.fabAddMedication);
    }

    private void setupToolbar() {
        patientName = getIntent().getStringExtra("patient_name");
        if (patientName != null) {
            ((TextView) findViewById(R.id.tvToolbarTitle)).setText(patientName + " - Medications");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        medicationList = new ArrayList<>(DataProvider.getMedications());

        DoctorMedicationAdapter.OnMedicationActionListener actionListener = new DoctorMedicationAdapter.OnMedicationActionListener() {
            @Override
            public void onEdit(Medication medication) {
                showAddEditMedicationDialog(medication);
            }

            @Override
            public void onDelete(Medication medication) {
                showDeleteConfirmation(medication);
            }
        };

        activeAdapter = new DoctorMedicationAdapter(medicationList, actionListener);
        rvActiveMedications.setLayoutManager(new LinearLayoutManager(this));
        rvActiveMedications.setAdapter(activeAdapter);

        // For demonstration, using empty list for completed
        completedAdapter = new DoctorMedicationAdapter(new ArrayList<>(), actionListener);
        rvCompletedMedications.setLayoutManager(new LinearLayoutManager(this));
        rvCompletedMedications.setAdapter(completedAdapter);
    }

    private void setupListeners() {
        fabAddMedication.setOnClickListener(v -> showAddEditMedicationDialog(null));
    }

    private void showAddEditMedicationDialog(Medication medication) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_medication, null);
        dialog.setContentView(view);

        TextView txtTitle = view.findViewById(R.id.txtDialogTitle);
        TextInputEditText etMedName = view.findViewById(R.id.etMedName);
        TextInputEditText etDosage = view.findViewById(R.id.etDosage);
        TextInputEditText etFrequency = view.findViewById(R.id.etFrequency);
        TextInputEditText etDuration = view.findViewById(R.id.etDuration);
        TextInputEditText etInstructions = view.findViewById(R.id.etInstructions);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveMedication);

        if (medication != null) {
            txtTitle.setText("Edit Medication");
            etMedName.setText(medication.getName());
            etDosage.setText(medication.getDosage());
            etFrequency.setText(medication.getFrequency());
            etDuration.setText(medication.getDuration());
            etInstructions.setText(medication.getTime());
            btnSave.setText("Update Medication");
        }

        btnSave.setOnClickListener(v -> {
            String name = etMedName.getText().toString().trim();
            if (name.isEmpty()) {
                etMedName.setError("Required");
                return;
            }

            // Logic to add/update locally for now
            if (medication == null) {
                Medication newMed = new Medication(
                        "m" + System.currentTimeMillis(),
                        name,
                        etDosage.getText().toString().trim(),
                        etFrequency.getText().toString().trim(),
                        etInstructions.getText().toString().trim(),
                        etDuration.getText().toString().trim(),
                        false
                );
                medicationList.add(0, newMed);
                Toast.makeText(this, "Medication Added", Toast.LENGTH_SHORT).show();
            } else {
                int index = medicationList.indexOf(medication);
                if (index != -1) {
                    Medication updatedMed = new Medication(
                            medication.getId(),
                            name,
                            etDosage.getText().toString().trim(),
                            etFrequency.getText().toString().trim(),
                            etInstructions.getText().toString().trim(),
                            etDuration.getText().toString().trim(),
                            medication.isTaken()
                    );
                    medicationList.set(index, updatedMed);
                    Toast.makeText(this, "Medication Updated", Toast.LENGTH_SHORT).show();
                }
            }
            activeAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDeleteConfirmation(Medication medication) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Medication")
                .setMessage("Are you sure you want to remove " + medication.getName() + "?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    medicationList.remove(medication);
                    activeAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Medication Removed", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
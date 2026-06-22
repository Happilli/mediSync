package com.bca.medisync;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bca.medisync.doctor.DoctorHomeActivity;
import com.bca.medisync.patient.HomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

public class MainActivity extends AppCompatActivity {
    private MaterialButton btnLogin;
    private ChipGroup chipGroupRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainStuff), (v, i)->{
            Insets systemBars  = i.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return i;
        });

        btnLogin  = findViewById(R.id.btnLogin);
        chipGroupRole = findViewById(R.id.chipGroupRole);
        btnLogin.setOnClickListener(v-> {
            Intent intent;
            if(chipGroupRole.getCheckedChipId() ==R.id.chipDoctor){
                intent = new Intent(MainActivity.this, DoctorHomeActivity.class);
            }else {
                intent = new Intent(MainActivity.this, HomeActivity.class);
            }
            startActivity(intent);
            finish();
        });
    }
}
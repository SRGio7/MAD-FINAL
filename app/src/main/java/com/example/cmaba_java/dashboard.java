package com.example.cmaba_java;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class dashboard extends AppCompatActivity {

    private TextView tvUserName, tvCardName, tvCardRole, tvDate;
    private TextView navHome, navUser, navHasil;
    private ImageView ivUserPhoto;
    private String userEmailKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Inisialisasi view
        tvUserName = findViewById(R.id.tvUserName);
        tvCardName = findViewById(R.id.tvCardName);
        tvCardRole = findViewById(R.id.tvCardRole);
        tvDate = findViewById(R.id.tvDate);
        ivUserPhoto = findViewById(R.id.ivUserPhoto);

        // Inisialisasi navigation
        navHome = findViewById(R.id.navHome);
        navUser = findViewById(R.id.navUser);
        navHasil = findViewById(R.id.navHasil);

        // Setup navigation click listeners
        navHome.setOnClickListener(v -> {
            setActiveNav(navHome);
            Toast.makeText(dashboard.this, "Home clicked", Toast.LENGTH_SHORT).show();
        });

        navUser.setOnClickListener(v -> {
            setActiveNav(navUser);
            Toast.makeText(dashboard.this, "User clicked", Toast.LENGTH_SHORT).show();
        });

        navHasil.setOnClickListener(v -> {
            setActiveNav(navHasil);
            Toast.makeText(dashboard.this, "Hasil clicked", Toast.LENGTH_SHORT).show();
        });

        // Setup tanggal saat ini
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        tvDate.setText(currentDate);

        // Setup click listeners for menu buttons
        View btnInfo = findViewById(R.id.btnInfo);
        View btnRegister = findViewById(R.id.btnRegister);
        View btnResult = findViewById(R.id.btnResult);

        btnInfo.setOnClickListener(v -> Toast.makeText(this, "Info clicked", Toast.LENGTH_SHORT).show());
        btnRegister.setOnClickListener(v -> Toast.makeText(this, "Register clicked", Toast.LENGTH_SHORT).show());
        btnResult.setOnClickListener(v -> Toast.makeText(this, "Result clicked", Toast.LENGTH_SHORT).show());

        // Dapatkan user data dari intent atau shared preferences
        if (getIntent().hasExtra("USER_EMAIL_KEY")) {
            userEmailKey = getIntent().getStringExtra("USER_EMAIL_KEY");
            getUserDataFromFirebase(userEmailKey);
        } else {
            // Cek dari SharedPreferences untuk session login
            userEmailKey = getSharedPreferences("LoginSession", MODE_PRIVATE)
                    .getString("emailKey", null);

            if (userEmailKey != null) {
                getUserDataFromFirebase(userEmailKey);
            } else {
                // Jika tidak ada user yang login, kembali ke LoginActivity
                finish();
            }
        }
    }

    // Method untuk mengubah warna teks navigasi yang aktif
    private void setActiveNav(TextView activeNav) {
        // Reset semua nav ke warna default
        navHome.setTextColor(getResources().getColor(android.R.color.darker_gray));
        navUser.setTextColor(getResources().getColor(android.R.color.darker_gray));
        navHasil.setTextColor(getResources().getColor(android.R.color.darker_gray));

        // Set nav yang aktif ke warna biru
        activeNav.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
    }

    private void getUserDataFromFirebase(String emailKey) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users").child(emailKey);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Ambil data user
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String role = dataSnapshot.child("role").getValue(String.class);

                    // Update UI
                    tvUserName.setText(name);
                    tvCardName.setText(name);
                    tvCardRole.setText(role);

                    // Di sini Anda bisa menambahkan kode untuk mengambil dan menampilkan foto user
                    // jika ada di database
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(dashboard.this,
                        "Failed to load user data: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        View btnInfo = findViewById(R.id.btnInfo);
        View btnRegister = findViewById(R.id.btnRegister);
        View btnResult = findViewById(R.id.btnResult);


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(dashboard.this, DaftarCekKesehatan.class);
                startActivity(intent);
            }
        });

        // In the onCreate method of dashboard.java
        View btnReg = findViewById(R.id.btnInfo);
        View btnRegister1 = findViewById(R.id.btnRegister);
        View btnResult1 = findViewById(R.id.btnResult);

        btnReg.setOnClickListener(v -> Toast.makeText(this, "Info clicked", Toast.LENGTH_SHORT).show());
        btnRegister1.setOnClickListener(v -> {
            Intent intent = new Intent(dashboard.this, DaftarCekKesehatan.class);
            startActivity(intent);
        });
        btnResult1.setOnClickListener(v -> {
            Intent intent = new Intent(dashboard.this, HasilCekKesehatan.class);
            startActivity(intent);
        });
    }
}
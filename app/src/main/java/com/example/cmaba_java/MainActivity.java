package com.example.cmaba_java;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private Button buttonGoToLogin;
    private String userEmailKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inisialisasi view
        textView = findViewById(R.id.textView);
        buttonGoToLogin = findViewById(R.id.buttonGoToLogin);

        // Cek apakah ada data user dari intent
        if (getIntent().hasExtra("USER_EMAIL_KEY")) {
            userEmailKey = getIntent().getStringExtra("USER_EMAIL_KEY");
            getUserDataFromFirebase(userEmailKey);
            buttonGoToLogin.setText("Logout");
        } else {
            // Cek dari SharedPreferences untuk session login
            SharedPreferences pref = getSharedPreferences("LoginSession", MODE_PRIVATE);
            userEmailKey = pref.getString("emailKey", null);

            if (userEmailKey != null) {
                // Masih ada sesi login
                getUserDataFromFirebase(userEmailKey);
                buttonGoToLogin.setText("Logout");
            }
        }

        // Kasih event klik button
        buttonGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userEmailKey != null) {
                    // Logout
                    getSharedPreferences("LoginSession", MODE_PRIVATE)
                            .edit()
                            .clear()
                            .apply();

                    userEmailKey = null;
                    textView.setText("Hello World!");
                    buttonGoToLogin.setText("Go to Login");
                } else {
                    // Ke halaman login
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void getUserDataFromFirebase(String emailKey) {
        // Gunakan cara sederhana untuk membaca data dari Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users").child(emailKey);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Ambil data user
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String role = dataSnapshot.child("role").getValue(String.class);
                    String nomorCamaba = dataSnapshot.child("nomorCamaba").getValue(String.class);

                    // Tampilkan data user
                    StringBuilder userInfo = new StringBuilder();
                    userInfo.append("Selamat datang, ").append(name).append("!\n\n");
                    userInfo.append("Email: ").append(email).append("\n");
                    userInfo.append("Role: ").append(role).append("\n");
                    userInfo.append("Nomor Camaba: ").append(nomorCamaba);

                    textView.setText(userInfo.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                textView.setText("Error loading user data: " + databaseError.getMessage());
            }
        });
    }
}
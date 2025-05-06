package com.example.cmaba_java;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private Spinner roleSpinner;
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView signUpText, roleHeaderText;
    private String selectedRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inisialisasi view
        roleSpinner = findViewById(R.id.roleSpinner);
        roleHeaderText = findViewById(R.id.roleHeaderText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpText = findViewById(R.id.signUpText);

        // Setup adapter untuk spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.user_roles,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        // Default role ke CAMABA
        selectedRole = "CAMABA";

        // Listener ketika role dipilih
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Update header text dengan role yang dipilih
                selectedRole = parent.getItemAtPosition(position).toString();
                roleHeaderText.setText(selectedRole);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                roleHeaderText.setText(R.string.role_hint);
            }
        });

        // Tombol login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateLogin()) {
                    loginUser();
                }
            }
        });

        // Text Sign Up
        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean validateLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validasi email
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email tidak boleh kosong");
            emailEditText.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Format email tidak valid");
            emailEditText.requestFocus();
            return false;
        }

        // Validasi password
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password tidak boleh kosong");
            passwordEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Konversi email menjadi key yang valid untuk Firebase
        String emailKey = email.replace(".", "_").replace("@", "_at_");

        // Tampilkan progress
        Toast.makeText(LoginActivity.this, "Mencoba masuk...", Toast.LENGTH_SHORT).show();

        // Gunakan cara sederhana untuk membaca data dari Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users").child(emailKey);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Periksa password
                    String savedPassword = dataSnapshot.child("password").getValue(String.class);
                    String savedRole = dataSnapshot.child("role").getValue(String.class);

                    if (password.equals(savedPassword)) {
                        // Password benar, periksa role
                        if (savedRole.equals(selectedRole)) {
                            // Login berhasil
                            Toast.makeText(LoginActivity.this,
                                    "Login berhasil sebagai " + selectedRole,
                                    Toast.LENGTH_SHORT).show();

                            // Simpan data login ke SharedPreferences sebagai penanda session
                            getSharedPreferences("LoginSession", MODE_PRIVATE)
                                    .edit()
                                    .putString("emailKey", emailKey)
                                    .putString("role", savedRole)
                                    .apply();

                            // Navigasi ke dashboard
                            Intent intent = new Intent(LoginActivity.this, dashboard.class);
                            intent.putExtra("USER_EMAIL_KEY", emailKey);
                            startActivity(intent);
                            finish();
                        } else {
                            // Role tidak sesuai
                            Toast.makeText(LoginActivity.this,
                                    "Role tidak sesuai! Anda terdaftar sebagai " + savedRole,
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Password salah
                        Toast.makeText(LoginActivity.this,
                                "Password salah!",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // User tidak ditemukan
                    Toast.makeText(LoginActivity.this,
                            "Email tidak terdaftar!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this,
                        "Error: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
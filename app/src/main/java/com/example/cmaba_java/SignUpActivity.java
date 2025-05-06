package com.example.cmaba_java;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText etName, etEmail, etBirth, etNik, etPassword;
    private Button btnRegister;
    private TextView tvSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Inisialisasi view
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etBirth = findViewById(R.id.etBirth);
        etNik = findViewById(R.id.etNik);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvSignIn = findViewById(R.id.tvSignIn);

        // Klik Register
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    saveUserData();
                }
            }
        });

        // Klik 'Login' TextView
        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean validateInputs() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String birth = etBirth.getText().toString().trim();
        String nomorCamaba = etNik.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validasi nama
        if (TextUtils.isEmpty(name)) {
            etName.setError("Nama tidak boleh kosong");
            etName.requestFocus();
            return false;
        }

        // Validasi email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email tidak boleh kosong");
            etEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Format email tidak valid");
            etEmail.requestFocus();
            return false;
        }

        // Validasi tanggal lahir
        if (TextUtils.isEmpty(birth)) {
            etBirth.setError("Tempat, tanggal lahir tidak boleh kosong");
            etBirth.requestFocus();
            return false;
        }

        // Validasi nomor camaba
        if (TextUtils.isEmpty(nomorCamaba)) {
            etNik.setError("Nomor camaba tidak boleh kosong");
            etNik.requestFocus();
            return false;
        }

        // Validasi password
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password tidak boleh kosong");
            etPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void saveUserData() {
        // Ambil data dari form
        String email = etEmail.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String birth = etBirth.getText().toString().trim();
        String nomorCamaba = etNik.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Konversi email menjadi key yang valid untuk Firebase Database
        // (mengganti karakter yang tidak diperbolehkan)
        String emailKey = email.replace(".", "_").replace("@", "_at_");

        // Gunakan cara sederhana untuk menulis data ke Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");

        // Simpan data user di node berdasarkan email
        DatabaseReference currentUserRef = usersRef.child(emailKey);

        // Simpan masing-masing field
        currentUserRef.child("name").setValue(name);
        currentUserRef.child("email").setValue(email);
        currentUserRef.child("birth").setValue(birth);
        currentUserRef.child("nomorCamaba").setValue(nomorCamaba);
        currentUserRef.child("password").setValue(password); // Dalam praktik nyata, jangan simpan plain password
        currentUserRef.child("role").setValue("CAMABA");

        Toast.makeText(SignUpActivity.this, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show();

        // Arahkan ke LoginActivity
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
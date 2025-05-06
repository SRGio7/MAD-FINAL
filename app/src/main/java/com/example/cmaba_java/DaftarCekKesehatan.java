package com.example.cmaba_java;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;

public class DaftarCekKesehatan extends AppCompatActivity {

    private EditText editTextNama;
    private EditText editTextDateOfBirth;
    private EditText editTextAdditionalNotes;
    private Spinner mcuDateSpinner;
    private Button btnSubmit;
    private Button btnTakePhoto;
    private ImageView imagePreview;

    private Uri imageUri;
    private String base64Image = "";
    private static final int REQUEST_PHOTO_CODE = 100;
    private String selectedMcuDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daftar_cek_kesehatan);

        // Initialize views
        editTextNama = findViewById(R.id.etNama);
        editTextDateOfBirth = findViewById(R.id.etDateOfBirth);
        editTextAdditionalNotes = findViewById(R.id.etAdditionalNotes);
        mcuDateSpinner = findViewById(R.id.mcuDateSpinner);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        imagePreview = findViewById(R.id.imagePreview);

        // Get user information from shared preferences if available
        SharedPreferences pref = getSharedPreferences("LoginSession", MODE_PRIVATE);
        String emailKey = pref.getString("emailKey", null);

        if (emailKey != null) {
            // Get user data from Firebase to pre-fill the form
            getUserDataFromFirebase(emailKey);
        }

        // Setup date picker for Date of Birth
        editTextDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Setup MCU Date spinner
        setupMcuDateSpinner();

        // Listener untuk tombol ambil foto
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Buka PhotoActivity untuk mengambil foto
                Intent photoIntent = new Intent(DaftarCekKesehatan.this, PhotoActivity.class);
                startActivityForResult(photoIntent, REQUEST_PHOTO_CODE);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextNama.getText().toString();
                String dateOfBirth = editTextDateOfBirth.getText().toString();
                String additionalNotes = editTextAdditionalNotes.getText().toString();

                if(name.isEmpty()){
                    editTextNama.setError(getString(R.string.msg_required_field));
                    return;
                }

                if(dateOfBirth.isEmpty()){
                    editTextDateOfBirth.setError(getString(R.string.msg_required_field));
                    return;
                }

                if(selectedMcuDate.isEmpty()){
                    Toast.makeText(DaftarCekKesehatan.this, getString(R.string.msg_pick_mcu_date), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Cek apakah foto sudah dipilih
                if (base64Image.isEmpty()) {
                    Toast.makeText(DaftarCekKesehatan.this, getString(R.string.msg_take_photo_first), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Simpan data termasuk foto base64
                addToDB(name, dateOfBirth, selectedMcuDate, additionalNotes, base64Image);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.daftar_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void getUserDataFromFirebase(String emailKey) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users").child(emailKey);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Pre-fill name if available
                    String name = dataSnapshot.child("name").getValue(String.class);
                    if (name != null && !name.isEmpty()) {
                        editTextNama.setText(name);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DaftarCekKesehatan.this,
                        "Error loading user data: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMcuDateSpinner() {
        // Create an array of MCU date options from string resources
        String[] mcuDates = new String[]{
                getString(R.string.mcu_date_prompt),
                getString(R.string.mcu_date_1),
                getString(R.string.mcu_date_2),
                getString(R.string.mcu_date_3)
        };

        // Create adapter for spinner with custom layouts
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_layout,
                mcuDates
        );
        adapter.setDropDownViewResource(R.layout.spinner_item_layout);
        mcuDateSpinner.setAdapter(adapter);

        // Set listener for item selection
        mcuDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedMcuDate = mcuDates[position];
                } else {
                    selectedMcuDate = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedMcuDate = "";
            }
        });
    }

    private void showDatePickerDialog() {
        // Get current date for initial DatePicker values
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a new DatePickerDialog instance
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                DaftarCekKesehatan.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Set the selected date in the EditText
                        String selectedDate = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year;
                        editTextDateOfBirth.setText(selectedDate);
                    }
                }, year, month, day);

        // Show the dialog
        datePickerDialog.show();
    }

    // Terima foto dari PhotoActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PHOTO_CODE && resultCode == RESULT_OK && data != null) {
            // Ambil data gambar
            if (data.hasExtra("imageBase64")) {
                // Jika foto sudah dalam format Base64
                base64Image = data.getStringExtra("imageBase64");

                // Tampilkan gambar di preview
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imagePreview.setImageBitmap(decodedBitmap);
                imagePreview.setVisibility(View.VISIBLE);
            } else if (data.hasExtra("imageUri")) {
                // Jika foto dalam format Uri, konversi ke Base64
                imageUri = data.getParcelableExtra("imageUri");
                if (imageUri != null) {
                    try {
                        // Convert Uri to Base64
                        InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        base64Image = bitmapToBase64(selectedImage);

                        // Tampilkan gambar di preview
                        imagePreview.setImageBitmap(selectedImage);
                        imagePreview.setVisibility(View.VISIBLE);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    // Konversi Bitmap ke Base64
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Kompresi gambar untuk mengurangi ukuran data
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void addToDB(String name, String dateOfBirth, String mcuDate, String additionalNotes, String photoBase64) {
        HashMap<String, Object> dataCekHashMap = new HashMap<>();
        dataCekHashMap.put("nama", name);
        dataCekHashMap.put("dateOfBirth", dateOfBirth);
        dataCekHashMap.put("mcuDate", mcuDate);
        dataCekHashMap.put("additionalNotes", additionalNotes);
        dataCekHashMap.put("foto", photoBase64); // Tambahkan foto dalam format Base64

        // Add user email if logged in
        SharedPreferences pref = getSharedPreferences("LoginSession", MODE_PRIVATE);
        String emailKey = pref.getString("emailKey", null);
        if (emailKey != null) {
            dataCekHashMap.put("userEmailKey", emailKey);
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dataCek = database.getReference("dataCekKesehatan");

        String key = dataCek.push().getKey();
        dataCekHashMap.put("key", key);

        dataCek.child(key).setValue(dataCekHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(DaftarCekKesehatan.this, getString(R.string.msg_form_success), Toast.LENGTH_SHORT).show();
                    // Reset form
                    editTextNama.getText().clear();
                    editTextDateOfBirth.getText().clear();
                    editTextAdditionalNotes.getText().clear();
                    mcuDateSpinner.setSelection(0);
                    selectedMcuDate = "";
                    // Reset gambar
                    base64Image = "";
                    imagePreview.setImageBitmap(null);
                    imagePreview.setVisibility(View.GONE);
                } else {
                    String errorMsg = String.format(getString(R.string.msg_form_failed),
                            task.getException().getMessage());
                    Toast.makeText(DaftarCekKesehatan.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });



    }
}
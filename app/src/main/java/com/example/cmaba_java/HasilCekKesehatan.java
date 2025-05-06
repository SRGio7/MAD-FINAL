package com.example.cmaba_java;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HasilCekKesehatan extends AppCompatActivity implements HealthCheckResultAdapter.OnItemClickListener {

    private static final int PERMISSION_REQUEST_CODE = 200;

    private RecyclerView recyclerViewResults;
    private HealthCheckResultAdapter adapter;
    private List<HealthCheckResult> healthCheckResults;
    private ProgressBar progressBar;
    private LinearLayout noDataLayout;
    private Button btnRegisterNew;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hasil_cek_kesehatan);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recyclerViewResults = findViewById(R.id.recyclerViewResults);
        progressBar = findViewById(R.id.progressBar);
        noDataLayout = findViewById(R.id.noDataLayout);
        btnRegisterNew = findViewById(R.id.btnRegisterNew);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        healthCheckResults = new ArrayList<>();
        adapter = new HealthCheckResultAdapter(this, healthCheckResults, this);

        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewResults.setAdapter(adapter);

        toolbar.setNavigationOnClickListener(v -> finish());

        btnRegisterNew.setOnClickListener(v -> {
            Intent intent = new Intent(HasilCekKesehatan.this, DaftarCekKesehatan.class);
            startActivity(intent);
        });

        swipeRefreshLayout.setOnRefreshListener(this::loadHealthCheckData);

        // Load health check data
        loadHealthCheckData();
    }

    private void loadHealthCheckData() {
        progressBar.setVisibility(View.VISIBLE);

        // Get user email key from SharedPreferences
        SharedPreferences pref = getSharedPreferences("LoginSession", MODE_PRIVATE);
        String userEmailKey = pref.getString("emailKey", null);

        if (userEmailKey == null) {
            showNoData();
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference healthCheckRef = database.getReference("dataCekKesehatan");

        // Query to get only this user's health check results
        Query userHealthChecks = healthCheckRef.orderByChild("userEmailKey").equalTo(userEmailKey);

        userHealthChecks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                healthCheckResults.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HealthCheckResult result = snapshot.getValue(HealthCheckResult.class);
                    healthCheckResults.add(result);
                }

                adapter.updateData(healthCheckResults);

                if (healthCheckResults.isEmpty()) {
                    showNoData();
                } else {
                    showResults();
                }

                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HasilCekKesehatan.this,
                        "Error loading data: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showNoData() {
        recyclerViewResults.setVisibility(View.GONE);
        noDataLayout.setVisibility(View.VISIBLE);
    }

    private void showResults() {
        recyclerViewResults.setVisibility(View.VISIBLE);
        noDataLayout.setVisibility(View.GONE);
    }

    @Override
    public void onDownloadClick(HealthCheckResult result) {
        // First check permissions
        if (checkPermission()) {
            // Generate and download PDF
            generatePDF(result);
        } else {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int write = ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE);

            return write == PackageManager.PERMISSION_GRANTED &&
                    read == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
            }, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void generatePDF(HealthCheckResult result) {
        progressBar.setVisibility(View.VISIBLE);

        // Create directory for PDF files if it doesn't exist
        File pdfDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "HealthCheck");

        if (!pdfDir.exists()) {
            pdfDir.mkdirs();
        }

        // Create file with timestamp to avoid overwriting
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "HealthCheck_" + timeStamp + ".pdf";
        File pdfFile = new File(pdfDir, fileName);

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Health Check Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add patient information table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10f);
            infoTable.setSpacingAfter(10f);

            // Set column widths
            float[] columnWidths = {1f, 3f};
            infoTable.setWidths(columnWidths);

            // Add cells to the table
            addTableCell(infoTable, "Name:", result.getNama());
            addTableCell(infoTable, "Date of Birth:", result.getDateOfBirth());
            addTableCell(infoTable, "MCU Date:", result.getMcuDate());

            String notes = result.getAdditionalNotes();
            if (notes == null || notes.isEmpty()) {
                notes = "No additional notes";
            }
            addTableCell(infoTable, "Additional Notes:", notes);

            document.add(infoTable);

            // Add photo if available
            if (result.getFoto() != null && !result.getFoto().isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(result.getFoto(), Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    decodedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                    // Add photo title
                    Paragraph photoTitle = new Paragraph("Photo:",
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
                    photoTitle.setSpacingBefore(10f);
                    document.add(photoTitle);

                    // Add the image
                    Image image = Image.getInstance(stream.toByteArray());

                    // Scale image if needed
                    float maxWidth = document.getPageSize().getWidth() - 80;
                    float maxHeight = 200f;

                    if (image.getWidth() > maxWidth || image.getHeight() > maxHeight) {
                        image.scaleToFit(maxWidth, maxHeight);
                    }

                    image.setAlignment(Element.ALIGN_CENTER);
                    document.add(image);
                } catch (Exception e) {
                    document.add(new Paragraph("Error displaying photo: " + e.getMessage()));
                }
            }

            // Add footer
            Paragraph footer = new Paragraph("This document was generated on " +
                    new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(new Date()));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20);
            document.add(footer);

            document.close();

            // Show the PDF
            viewPDF(pdfFile);

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        progressBar.setVisibility(View.GONE);
    }

    private void addTableCell(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.BOTTOM);
        labelCell.setPaddingBottom(8f);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.BOTTOM);
        valueCell.setPaddingBottom(8f);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(valueCell);
    }

    private void viewPDF(File pdfFile) {
        Uri fileUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider",
                    pdfFile);
        } else {
            fileUri = Uri.fromFile(pdfFile);
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
            Toast.makeText(this, "PDF saved: " + pdfFile.getName(), Toast.LENGTH_SHORT).show();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No PDF viewer application found", Toast.LENGTH_SHORT).show();

            // Provide option to open with other apps
            Intent openIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            openIntent.addCategory(Intent.CATEGORY_OPENABLE);
            openIntent.setType("*/*");
            startActivity(openIntent);
        }
    }
}
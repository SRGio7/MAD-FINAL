package com.example.cmaba_java;

public class HealthCheckResult {
    private String key;
    private String nama;
    private String dateOfBirth;
    private String mcuDate;
    private String additionalNotes;
    private String foto;
    private String userEmailKey;

    // Empty constructor needed for Firebase
    public HealthCheckResult() {
    }

    public HealthCheckResult(String key, String nama, String dateOfBirth, String mcuDate,
                             String additionalNotes, String foto, String userEmailKey) {
        this.key = key;
        this.nama = nama;
        this.dateOfBirth = dateOfBirth;
        this.mcuDate = mcuDate;
        this.additionalNotes = additionalNotes;
        this.foto = foto;
        this.userEmailKey = userEmailKey;
    }

    // Getters and setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getMcuDate() {
        return mcuDate;
    }

    public void setMcuDate(String mcuDate) {
        this.mcuDate = mcuDate;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getUserEmailKey() {
        return userEmailKey;
    }

    public void setUserEmailKey(String userEmailKey) {
        this.userEmailKey = userEmailKey;
    }
}
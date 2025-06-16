package com.example.projectakhir.model;

import java.io.Serializable;

public class MenuItem implements Serializable {

    // PROPERTI: NAMA INI HARUS SAMA PERSIS DENGAN NAMA FIELD DI FIREBASE DAILY_MENUS ANDA
    // BERDASARKAN LOGIKA TambahPackageActivity, asumsi nama field di Firebase adalah:
    // "nama", "harga", "deskripsi", "imageUrl"
    // Saya sarankan Anda menggunakan nama yang lebih deskriptif seperti menuName, menuPrice, dst.
    // Tapi jika AddMenuActivity menyimpan sebagai "nama", maka di sini juga "nama".

    private String key; // Ini untuk Firebase key, penting untuk edit/delete
    private String nama; // Contoh: Nama menu
    private String deskripsi; // Contoh: Deskripsi menu
    private String harga; // Contoh: Harga menu (string karena di TambahPackageActivity etHarga adalah EditText)
    private String imageUrl; // Base64 String atau URL gambar

    // KONSTRUKTOR KOSONG (WAJIB UNTUK FIREBASE)
    public MenuItem() {
        // Default constructor required for calls to DataSnapshot.getValue(MenuItem.class)
    }

    // KONSTRUKTOR DENGAN ARGUMEN
    public MenuItem(String key, String nama, String deskripsi, String harga, String imageUrl) {
        this.key = key;
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.harga = harga; // Ini String
        this.imageUrl = imageUrl;
    }
    // GETTERS (WAJIB UNTUK FIREBASE)
    public String getKey() {
        return key;
    }

    public String getNama() {
        return nama;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public String getHarga() { // Jika harga disimpan sebagai string di Firebase
        return harga;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // SETTERS (Opsional, tapi bagus untuk fleksibilitas)
    public void setKey(String key) {
        this.key = key;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public void setHarga(String harga) { // Jika harga disimpan sebagai string
        this.harga = harga;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
package com.example.projectakhir.model;

import java.io.Serializable;

public class MenuItem implements Serializable {

    private String key;
    private String nama;
    private String deskripsi;
    private String harga;
    private String imageUrl;
    public MenuItem() {
    }
    public MenuItem(String key, String nama, String deskripsi, String harga, String imageUrl) {
        this.key = key;
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.harga = harga;
        this.imageUrl = imageUrl;
    }
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
package com.example.projectakhir;

import java.io.Serializable;

public class Pesanan implements Serializable {

    private String key;
    private String namaPelanggan;
    private String namaMenu;
    private String totalHarga;
    private String status;
    private String imageBase64;
    public Pesanan() {
    }

    public Pesanan(String namaPelanggan, String namaMenu, String totalHarga, String status) {
        this.namaPelanggan = namaPelanggan;
        this.namaMenu = namaMenu;
        this.totalHarga = totalHarga;
        this.status = status;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getNamaPelanggan() {
        return namaPelanggan;
    }

    public void setNamaPelanggan(String namaPelanggan) {
        this.namaPelanggan = namaPelanggan;
    }

    public String getNamaMenu() {
        return namaMenu;
    }

    public void setNamaMenu(String namaMenu) {
        this.namaMenu = namaMenu;
    }

    public String getTotalHarga() {
        return totalHarga;
    }

    public void setTotalHarga(String totalHarga) {
        this.totalHarga = totalHarga;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

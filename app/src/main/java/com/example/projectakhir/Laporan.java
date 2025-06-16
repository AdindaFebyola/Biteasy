package com.example.projectakhir;

import java.io.Serializable;

// Implementasikan Serializable agar objek bisa dikirim via Intent
public class Laporan implements Serializable {
    private String kategori;
    private String tanggal;
    private String jumlahPesanan;
    private String totalPendapatan;
    private String id; // <--- MAKE SURE THIS IS HERE!

    // Default constructor needed for Firebase
    public Laporan() {
    }

    public Laporan(String kategori, String tanggal, String jumlahPesanan, String totalPendapatan) {
        this.kategori = kategori;
        this.tanggal = tanggal;
        this.jumlahPesanan = jumlahPesanan;
        this.totalPendapatan = totalPendapatan;
    }

    // --- Getter dan Setter ---
    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public String getTanggal() { return tanggal; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }

    public String getJumlahPesanan() { return jumlahPesanan; }
    public void setJumlahPesanan(String jumlahPesanan) { this.jumlahPesanan = jumlahPesanan; }

    public String getTotalPendapatan() { return totalPendapatan; }
    public void setTotalPendapatan(String totalPendapatan) { this.totalPendapatan = totalPendapatan; }

    public String getId() { // <--- MAKE SURE THIS IS HERE!
        return id;
    }

    public void setId(String id) { // <--- MAKE SURE THIS IS HERE!
        this.id = id;
    }
}
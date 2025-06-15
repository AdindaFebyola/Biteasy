package com.example.projectakhir;

public class PackageItem {

    public String packageId;
    public String nama;
    public String deskripsi;
    public String harga;
    public String imageUrl;

    public PackageItem() {
    }

    public PackageItem(String packageId, String nama, String deskripsi, String harga, String imageUrl) {
        this.packageId = packageId;
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.harga = harga;
        this.imageUrl = imageUrl;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getHarga() {
        return harga;
    }

    public void setHarga(String harga) {
        this.harga = harga;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

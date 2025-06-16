package com.example.projectakhir.activity; // Sesuaikan dengan package Anda

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.projectakhir.R;
import com.example.projectakhir.model.MenuItem; // Pastikan ini MenuItem yang sudah diupdate
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat; // Untuk format harga

public class EditMenuActivity extends AppCompatActivity {

    private EditText etNamaMenu, etDeskripsiMenu, etHargaMenu; // Sesuaikan dengan properti baru
    private ImageView ivMenuImageEdit, ivBack;
    private Button btnSaveChanges;

    private DatabaseReference databaseReference;
    private MenuItem currentMenuItem;
    private String menuKey;
    private Uri imageUri;
    private String newBase64Image;

    // ActivityResultLaunchers untuk memilih/mengambil gambar
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    Glide.with(this).load(imageUri).into(ivMenuImageEdit);
                    newBase64Image = convertImageToBase64(uri); // Konversi langsung saat dipilih
                } else {
                    Toast.makeText(this, "Tidak ada gambar yang dipilih.", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    Glide.with(this).load(imageUri).into(ivMenuImageEdit);
                    newBase64Image = convertImageToBase64(imageUri); // Konversi langsung saat diambil
                } else {
                    Toast.makeText(this, "Pengambilan gambar dibatalkan.", Toast.LENGTH_SHORT).show();
                    imageUri = null;
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_menu); // Pastikan nama layout ini benar

        // Inisialisasi Views
        etNamaMenu = findViewById(R.id.et_nama_menu_edit); // ID harus sesuai dengan XML Anda
        etDeskripsiMenu = findViewById(R.id.et_deskripsi_menu_edit); // ID harus sesuai dengan XML Anda
        etHargaMenu = findViewById(R.id.et_harga_menu_edit); // ID harus sesuai dengan XML Anda
        ivMenuImageEdit = findViewById(R.id.iv_menu_image_edit); // ID harus sesuai dengan XML Anda
        ivBack = findViewById(R.id.iv_back_button_edit); // ID harus sesuai dengan XML Anda
        btnSaveChanges = findViewById(R.id.btn_save_changes); // ID harus sesuai dengan XML Anda
        Button btnChangeImage = findViewById(R.id.btn_change_image); // Asumsi Anda punya tombol ini

        databaseReference = FirebaseDatabase.getInstance().getReference("daily_menus");

        // Ambil data menu dari Intent
        currentMenuItem = (MenuItem) getIntent().getSerializableExtra("menu_item");
        menuKey = getIntent().getStringExtra("menu_key");

        if (currentMenuItem != null && menuKey != null) {
            // Mengisi data yang sudah ada ke EditTexts
            etNamaMenu.setText(currentMenuItem.getNama()); // Ganti getMenuName()
            etDeskripsiMenu.setText(currentMenuItem.getDeskripsi()); // Ganti getDescription()
            etHargaMenu.setText(currentMenuItem.getHarga()); // Ganti getPrice() (ini sekarang string)

            // Memuat gambar jika ada
            if (currentMenuItem.getImageUrl() != null && !currentMenuItem.getImageUrl().isEmpty()) {
                try {
                    // Coba decode sebagai Base64
                    byte[] decodedString = Base64.decode(currentMenuItem.getImageUrl(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    Glide.with(this).load(decodedByte).into(ivMenuImageEdit);
                } catch (IllegalArgumentException e) {
                    // Jika bukan Base64, coba load sebagai URL biasa
                    Glide.with(this).load(currentMenuItem.getImageUrl()).into(ivMenuImageEdit);
                    Log.e("EditMenuActivity", "Error decoding Base64, trying as URL: " + e.getMessage());
                }
            } else {
                ivMenuImageEdit.setImageResource(R.drawable.ic_image_placeholder);
            }
            newBase64Image = currentMenuItem.getImageUrl(); // Set gambar awal sebagai currentBase64Image
        } else {
            Toast.makeText(this, "Gagal memuat data menu.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Listener untuk tombol kembali
        ivBack.setOnClickListener(v -> onBackPressed());

        // Listener untuk tombol Ganti Gambar
        btnChangeImage.setOnClickListener(v -> {
            // Bisa menampilkan dialog untuk pilih dari galeri atau kamera
            showImagePickerDialog();
        });


        // Listener untuk tombol Simpan Perubahan
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void showImagePickerDialog() {
        // Implementasi dialog sederhana atau langsung ke galeri/kamera
        // Contoh sederhana: langsung ke galeri
        pickImageLauncher.launch("image/*");
        // Atau untuk kamera:
        // createImageFileAndLaunchCamera();
    }

    private String convertImageToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream); // Kompresi 70%
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal mengkonversi gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    private void saveChanges() {
        String nama = etNamaMenu.getText().toString().trim();
        String deskripsi = etDeskripsiMenu.getText().toString().trim();
        String harga = etHargaMenu.getText().toString().trim(); // Tetap string

        if (nama.isEmpty() || deskripsi.isEmpty() || harga.isEmpty()) {
            Toast.makeText(this, "Harap isi semua kolom!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (menuKey != null) {
            // Buat objek MenuItem baru dengan data yang diupdate
            // Gunakan newBase64Image yang mungkin sudah diupdate atau currentMenuItem.getImageUrl()
            MenuItem updatedMenu = new MenuItem(menuKey, nama, deskripsi, harga, newBase64Image != null ? newBase64Image : currentMenuItem.getImageUrl());

            databaseReference.child(menuKey).setValue(updatedMenu)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditMenuActivity.this, "Perubahan berhasil disimpan!", Toast.LENGTH_SHORT).show();
                        finish(); // Kembali ke DailyPackageActivity
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditMenuActivity.this, "Gagal menyimpan perubahan: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("EditMenuActivity", "Firebase update error: " + e.getMessage(), e);
                    });
        }
    }
}
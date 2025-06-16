package com.example.projectakhir.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log; // Pastikan ini ada
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.projectakhir.R;
import com.example.projectakhir.model.MenuItem; // Pastikan ini MenuItem yang sudah diupdate
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddMenuActivity extends AppCompatActivity {

    // Sesuaikan ID dengan yang ada di activity_add_menu.xml Anda
    EditText etNamaMenu, etDeskripsiMenu, etHargaMenu; // Sesuaikan dengan properti MenuItem yang baru
    ImageView ivPreviewImage;
    Button btnSelectImage, btnCaptureImage, btnBatal, btnTambah; // Pastikan ID ini ada di XML

    private DatabaseReference databaseReference;
    private Uri imageUri;
    private String currentPhotoPath; // Untuk menyimpan path sementara dari foto yang diambil

    // ActivityResultLaunchers
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    Glide.with(this).load(imageUri).into(ivPreviewImage);
                } else {
                    Toast.makeText(this, "Tidak ada gambar yang dipilih.", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    Glide.with(this).load(imageUri).into(ivPreviewImage);
                } else {
                    Toast.makeText(this, "Pengambilan gambar dibatalkan.", Toast.LENGTH_SHORT).show();
                    imageUri = null; // Reset imageUri jika dibatalkan
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Izin diberikan.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Izin ditolak. Anda tidak bisa menggunakan fitur ini.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu); // Pastikan nama layout ini benar

        // Inisialisasi Views - PASTIKAN ID SUDAH BENAR SESUAI XML Anda
        etNamaMenu = findViewById(R.id.et_nama_menu); // Contoh ID
        etDeskripsiMenu = findViewById(R.id.et_deskripsi_menu); // Contoh ID
        etHargaMenu = findViewById(R.id.et_harga_menu); // Contoh ID
        ivPreviewImage = findViewById(R.id.iv_preview_image); // Contoh ID
        btnSelectImage = findViewById(R.id.btn_select_image); // Contoh ID
        btnCaptureImage = findViewById(R.id.btn_capture_image); // Contoh ID
        btnBatal = findViewById(R.id.btn_batal); // Contoh ID
        btnTambah = findViewById(R.id.btn_tambah); // Contoh ID

        // Inisialisasi Firebase Database Reference
        // Ini adalah path untuk daily menus
        databaseReference = FirebaseDatabase.getInstance().getReference("daily_menus");

        // Set Listeners
        btnSelectImage.setOnClickListener(v -> {
            String permission;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permission = Manifest.permission.READ_MEDIA_IMAGES;
            } else {
                permission = Manifest.permission.READ_EXTERNAL_STORAGE;
            }
            checkAndRequestPermission(permission, () -> pickImageLauncher.launch("image/*"));
        });

        btnCaptureImage.setOnClickListener(v -> {
            checkAndRequestPermission(Manifest.permission.CAMERA, this::dispatchTakePictureIntent);
        });

        btnBatal.setOnClickListener(v -> {
            finish();
        });

        btnTambah.setOnClickListener(v -> {
            String nama = etNamaMenu.getText().toString().trim();
            String deskripsi = etDeskripsiMenu.getText().toString().trim();
            String harga = etHargaMenu.getText().toString().trim(); // Ambil sebagai String

            Log.d("AddMenuActivity", "Nama: " + nama);
            Log.d("AddMenuActivity", "Deskripsi: " + deskripsi);
            Log.d("AddMenuActivity", "Harga: " + harga);

            if (nama.isEmpty() || deskripsi.isEmpty() || harga.isEmpty()) {
                Toast.makeText(this, "Harap isi semua kolom!", Toast.LENGTH_SHORT).show();
            } else if (imageUri == null) {
                Toast.makeText(this, "Mohon pilih atau ambil gambar terlebih dahulu.", Toast.LENGTH_SHORT).show();
            } else {
                convertImageToBase64AndSave(nama, deskripsi, harga); // Kirim semua data
            }
        });
    }

    private void checkAndRequestPermission(String permission, Runnable callback) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            callback.run();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Gagal membuat file gambar sementara: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".provider",
                        photoFile
                );
                takePictureLauncher.launch(imageUri);
            }
        } else {
            Toast.makeText(this, "Tidak ada aplikasi kamera yang tersedia.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            throw new IOException("External storage directory not found.");
        }
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath(); // Simpan path sementara
        return image;
    }


    private void convertImageToBase64AndSave(String nama, String deskripsi, String harga) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream); // Kompresi gambar 70%
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

            saveMenuData(nama, deskripsi, harga, base64Image); // Panggil fungsi save data

        } catch (IOException e) {
            Toast.makeText(this, "Gagal mengkonversi gambar ke Base64: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void saveMenuData(String nama, String deskripsi, String harga, String base64Image) {
        String menuId = databaseReference.push().getKey(); // Dapatkan unique key dari Firebase

        if (menuId != null) {
            // MENGGUNAKAN KONSTRUKTOR YANG BARU DENGAN FIELD YANG SAMA
            MenuItem newMenu = new MenuItem(menuId, nama, deskripsi, harga, base64Image); // SESUAI DENGAN MenuItem.java

            databaseReference.child(menuId).setValue(newMenu)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddMenuActivity.this, "Menu berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                        finish(); // Kembali ke DailyPackageActivity
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddMenuActivity.this, "Gagal menambahkan menu ke database: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("AddMenuActivity", "Firebase Save Error: " + e.getMessage(), e);
                    });
        } else {
            Toast.makeText(this, "Gagal membuat ID menu.", Toast.LENGTH_SHORT).show();
        }
    }
}
package com.example.projectakhir;

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

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailMenuActivity extends AppCompatActivity {

    private ImageView btnBackFromDetail;
    private ImageView ivPackageImageDetail;
    private EditText etNamaPackageDetail, etHargaPackageDetail, etDeskripsiPackageDetail;
    private Button btnSelectImageDetail, btnCaptureImageDetail, btnSaveChanges;

    private String packageId;
    private Uri newImageUri;
    private String currentPhotoPath;
    private String existingBase64Image;

    private DatabaseReference databaseReference;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    newImageUri = uri;
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), newImageUri);
                        ivPackageImageDetail.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Gagal memuat gambar dari galeri.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Tidak ada gambar baru yang dipilih.", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), newImageUri);
                        ivPackageImageDetail.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Gagal memuat gambar dari kamera.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Pengambilan gambar dibatalkan.", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Izin ditolak. Anda tidak bisa menggunakan fitur ini.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_menu);

        btnBackFromDetail = findViewById(R.id.btnBackFromDetail);
        ivPackageImageDetail = findViewById(R.id.ivPackageImageDetail);
        etNamaPackageDetail = findViewById(R.id.etNamaPackageDetail);
        etHargaPackageDetail = findViewById(R.id.etHargaPackageDetail);
        etDeskripsiPackageDetail = findViewById(R.id.etDeskripsiPackageDetail);
        btnSelectImageDetail = findViewById(R.id.btnSelectImageDetail);
        btnCaptureImageDetail = findViewById(R.id.btnCaptureImageDetail);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        databaseReference = FirebaseDatabase.getInstance().getReference("packages");

        Intent intent = getIntent();
        if (intent != null) {
            packageId = intent.getStringExtra("packageId");
            String nama = intent.getStringExtra("title");
            String deskripsi = intent.getStringExtra("description");
            String harga = intent.getStringExtra("price");
            existingBase64Image = intent.getStringExtra("imageUrl");

            etNamaPackageDetail.setText(nama);
            etHargaPackageDetail.setText(harga);
            etDeskripsiPackageDetail.setText(deskripsi);

            if (existingBase64Image != null && !existingBase64Image.isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(existingBase64Image, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    ivPackageImageDetail.setImageBitmap(decodedBitmap);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error decoding existing image.", Toast.LENGTH_SHORT).show();
                    ivPackageImageDetail.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                ivPackageImageDetail.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            Toast.makeText(this, "Data paket tidak ditemukan.", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBackFromDetail.setOnClickListener(v -> finish());

        btnSelectImageDetail.setOnClickListener(v -> {
            String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    ? Manifest.permission.READ_MEDIA_IMAGES
                    : Manifest.permission.READ_EXTERNAL_STORAGE;
            checkAndRequestPermission(permission, () -> pickImageLauncher.launch("image/*"));
        });

        btnCaptureImageDetail.setOnClickListener(v -> {
            checkAndRequestPermission(Manifest.permission.CAMERA, this::dispatchTakePictureIntent);
        });

        btnSaveChanges.setOnClickListener(v -> {
            String updatedNama = etNamaPackageDetail.getText().toString().trim();
            String updatedHarga = etHargaPackageDetail.getText().toString().trim();
            String updatedDeskripsi = etDeskripsiPackageDetail.getText().toString().trim();

            if (updatedNama.isEmpty() || updatedHarga.isEmpty() || updatedDeskripsi.isEmpty()) {
                Toast.makeText(DetailMenuActivity.this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            String finalImageUrlBase64 = existingBase64Image;
            if (newImageUri != null) {
                try {
                    Bitmap newBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), newImageUri);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();
                    finalImageUrlBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Gagal mengonversi gambar baru ke Base64.", Toast.LENGTH_SHORT).show();
                    finalImageUrlBase64 = existingBase64Image;
                }
            }

            updatePackageData(packageId, updatedNama, updatedDeskripsi, updatedHarga, finalImageUrlBase64);
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
                newImageUri = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".provider",
                        photoFile
                );
                takePictureLauncher.launch(newImageUri);
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
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void updatePackageData(String id, String nama, String deskripsi, String harga, String imageUrlBase64) {
        if (id != null) {
            PackageItem updatedPackage = new PackageItem(id, nama, deskripsi, harga, imageUrlBase64);
            databaseReference.child(id).setValue(updatedPackage)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(DetailMenuActivity.this, "Paket berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(DetailMenuActivity.this, "Gagal memperbarui paket: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Toast.makeText(this, "ID Paket tidak ditemukan untuk update.", Toast.LENGTH_SHORT).show();
        }
    }
}

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
import android.util.Log;
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

public class TambahPackageActivity extends AppCompatActivity {

    EditText etNama, etHarga, etDeskripsi;
    ImageView ivPreviewImage;
    Button btnSelectImage, btnCaptureImage, btnBatal, btnTambah;

    private DatabaseReference databaseReference;
    private Uri imageUri;
    private String currentPhotoPath;

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
                    imageUri = null;
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
        setContentView(R.layout.activity_tambah_package);

        etNama = findViewById(R.id.etNamaPackage);
        etHarga = findViewById(R.id.etHargaPackage);
        etDeskripsi = findViewById(R.id.etDeskripsiPackage);
        ivPreviewImage = findViewById(R.id.ivPreviewImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnCaptureImage = findViewById(R.id.btnCaptureImage);
        btnBatal = findViewById(R.id.btnBatal);
        btnTambah = findViewById(R.id.btnTambah);

        databaseReference = FirebaseDatabase.getInstance().getReference("packages");

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
            String nama = etNama.getText().toString().trim();
            String harga = etHarga.getText().toString().trim();
            String deskripsi = etDeskripsi.getText().toString().trim();

            Log.d("TambahPackageActivity", "Nama dari EditText: " + nama);
            Log.d("TambahPackageActivity", "Harga dari EditText: " + harga);
            Log.d("TambahPackageActivity", "Deskripsi dari EditText: " + deskripsi);

            if (nama.isEmpty() || harga.isEmpty() || deskripsi.isEmpty()) {
                Toast.makeText(this, "Harap isi semua data!", Toast.LENGTH_SHORT).show();
            } else if (imageUri == null) {
                Toast.makeText(this, "Mohon pilih atau ambil gambar terlebih dahulu.", Toast.LENGTH_SHORT).show();
            } else {
                convertImageToBase64AndSave(nama, harga, deskripsi);
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
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void convertImageToBase64AndSave(String nama, String harga, String deskripsi) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

            savePackageData(nama, harga, deskripsi, base64Image);

        } catch (IOException e) {
            Toast.makeText(this, "Gagal mengkonversi gambar ke Base64: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void savePackageData(String nama, String harga, String deskripsi, String base64Image) {
        String packageId = databaseReference.push().getKey();

        if (packageId != null) {
            PackageItem newPackage = new PackageItem(packageId, nama, deskripsi, harga, base64Image);

            databaseReference.child(packageId).setValue(newPackage)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(TambahPackageActivity.this, "Paket berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(TambahPackageActivity.this, "Gagal menambahkan paket ke database: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("TambahPackageActivity", "Firebase Save Error: " + e.getMessage(), e);
                    });
        } else {
            Toast.makeText(this, "Gagal membuat ID paket.", Toast.LENGTH_SHORT).show();
        }
    }
}

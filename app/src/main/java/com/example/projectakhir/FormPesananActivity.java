package com.example.projectakhir;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormPesananActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private EditText inputNamaMenu, inputHarga, etNamaPemesan;
    private AutoCompleteTextView spinnerStatus;
    private Button btnSimpan, btnPilihGaleri, btnAmbilKamera, btnBatal; // btnBatal sekarang adalah Button
    private ImageView ivGambarPreview;
    private ImageView btnBack; // btnBack tetap ImageView

    private Uri imageUri;
    private String currentPhotoPath;
    private DatabaseReference databaseReference;

    private FirebaseAuth mAuth;
    private ImageView ivLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_pesanan);

        databaseReference = FirebaseDatabase.getInstance().getReference("pesanan");

        etNamaPemesan = findViewById(R.id.etNamaPemesan);
        inputHarga = findViewById(R.id.etHarga);
        inputNamaMenu = findViewById(R.id.inputNamaMenu);
        spinnerStatus = findViewById(R.id.spinnerStatus); // Cast tidak wajib, tapi lebih aman
        btnSimpan = findViewById(R.id.btnTambah);
        ivGambarPreview = findViewById(R.id.ivGambarPreview);
        btnPilihGaleri = findViewById(R.id.btnPilihGaleri);
        btnAmbilKamera = findViewById(R.id.btnAmbilKamera);
        btnBack = findViewById(R.id.btnBack);
        btnBatal = findViewById(R.id.btnBatal);
        mAuth = FirebaseAuth.getInstance();
        ivLogout = findViewById(R.id.ivLogout);

        String[] daftarStatus = {"Baru", "Diproses", "Selesai", "Dibatalkan"};
        StatusAdapter adapterStatus = new StatusAdapter(this, daftarStatus);
        spinnerStatus.setAdapter(adapterStatus);

        btnBack.setOnClickListener(v -> onBackPressed());
        btnBatal.setOnClickListener(v -> onBackPressed());
        btnPilihGaleri.setOnClickListener(v -> openGallery());
        btnAmbilKamera.setOnClickListener(v -> checkCameraPermissionAndOpenCamera());
        btnSimpan.setOnClickListener(v -> savePesanan());
        ivLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }


    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    private void checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Gagal membuat file gambar", Toast.LENGTH_SHORT).show();
        }
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    // Pastikan authorities sama dengan di AndroidManifest
                    // Jika package name Anda com.example.projekakhir, ganti di bawah ini
                    "com.example.projekakhir.provider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GALLERY && data != null && data.getData() != null) {
                imageUri = data.getData();
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                File f = new File(currentPhotoPath);
                imageUri = Uri.fromFile(f);
            }
            ivGambarPreview.setImageURI(imageUri);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void savePesanan() {
        String namaPemesan = etNamaPemesan.getText().toString().trim();
        String namaMenu = inputNamaMenu.getText().toString().trim();
        String harga = inputHarga.getText().toString().trim();
        String status = spinnerStatus.getText().toString();

        if (namaPemesan.isEmpty() || namaMenu.isEmpty() || harga.isEmpty() || status.equals("Pilih Status")) {
            Toast.makeText(this, "Semua data harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageBase64 = null;
        if (imageUri != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageBase64 = bitmapToBase64(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        Pesanan pesananBaru = new Pesanan(namaPemesan, namaMenu, harga, status);
        pesananBaru.setImageBase64(imageBase64);

        databaseReference.push().setValue(pesananBaru)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(FormPesananActivity.this, "Pesanan berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FormPesananActivity.this, "Gagal menambahkan pesanan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditPesananActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private EditText editNamaPemesan, editNamaMenu, editHarga;
    private AutoCompleteTextView spinnerStatus;
    private Button btnSimpan, btnPilihGaleriEdit, btnAmbilKameraEdit;
    private ImageView ivGambarPreviewEdit;
    private ImageView btnBack;
    private ImageView ivLogout;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String pesananKey;
    private Uri imageUri;
    private String currentPhotoPath;
    private String imageBase64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pesanan);

        mAuth = FirebaseAuth.getInstance();
        editNamaPemesan = findViewById(R.id.etNama);
        editNamaMenu = findViewById(R.id.etMenu);
        editHarga = findViewById(R.id.etHarga);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnSimpan = findViewById(R.id.btnSaveChanges);
        ivGambarPreviewEdit = findViewById(R.id.ivGambarPreviewEdit);
        btnPilihGaleriEdit = findViewById(R.id.btnPilihGaleriEdit);
        btnAmbilKameraEdit = findViewById(R.id.btnAmbilKameraEdit);
        btnBack = findViewById(R.id.btnBack);
        ivLogout = findViewById(R.id.ivLogout);

        btnBack.setOnClickListener(v -> onBackPressed());

        ivLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(EditPesananActivity.this, "Berhasil logout", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(EditPesananActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        pesananKey = getIntent().getStringExtra("PESANAN_KEY");
        if (pesananKey == null) {
            Toast.makeText(this, "Error: Key pesanan tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("pesanan").child(pesananKey);

        loadPesananData();

        btnPilihGaleriEdit.setOnClickListener(v -> openGallery());
        btnAmbilKameraEdit.setOnClickListener(v -> checkCameraPermissionAndOpenCamera());
        btnSimpan.setOnClickListener(v -> saveChanges());
    }

    private void loadPesananData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Pesanan pesanan = snapshot.getValue(Pesanan.class);
                if (pesanan != null) {
                    editNamaPemesan.setText(pesanan.getNamaPelanggan());
                    editNamaMenu.setText(pesanan.getNamaMenu());
                    editHarga.setText(pesanan.getTotalHarga());

                    String[] daftarStatus = new String[]{"Baru", "Diproses", "Selesai", "Dibatalkan"};

                    StatusAdapter statusAdapter = new StatusAdapter(EditPesananActivity.this, daftarStatus);

                    spinnerStatus.setAdapter(statusAdapter);

                    spinnerStatus.setText(pesanan.getStatus(), false);

                    imageBase64 = pesanan.getImageBase64();
                    if (imageBase64 != null && !imageBase64.isEmpty()) {
                        byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        ivGambarPreviewEdit.setImageBitmap(decodedByte);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditPesananActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
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
                    "com.example.biteasy.provider",
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
            ivGambarPreviewEdit.setImageURI(imageUri);
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

    private void saveChanges() {
        if (imageUri != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageBase64 = bitmapToBase64(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        String updatedNamaPemesan = editNamaPemesan.getText().toString();
        String updatedNamaMenu = editNamaMenu.getText().toString();
        String updatedHarga = editHarga.getText().toString();
        String updatedStatus = spinnerStatus.getText().toString();

        Pesanan updatedPesanan = new Pesanan(updatedNamaPemesan, updatedNamaMenu, updatedHarga, updatedStatus);
        updatedPesanan.setImageBase64(imageBase64);

        databaseReference.setValue(updatedPesanan)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditPesananActivity.this, "Perubahan berhasil disimpan", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(EditPesananActivity.this, "Gagal menyimpan perubahan", Toast.LENGTH_SHORT).show());
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
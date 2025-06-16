package com.example.projectakhir; // Sesuaikan dengan nama paket utama Anda

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView; // Tambahkan ini untuk headerTitle dan btnBack
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// Nama kelas sudah diganti dari MainActivity menjadi EventPackageActivity
public class EventPackageActivity extends AppCompatActivity { // UBAH INI

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private ImageView btnLogout;
    private ImageView btnBack; // Tambahkan inisialisasi untuk tombol kembali
    private TextView headerTitle; // Tambahkan inisialisasi untuk judul header
    private List<PackageItem> packageList;
    private PackageAdapter adapter;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_package); // UBAH INI (dari activity_main)

        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack); // Inisialisasi
        headerTitle = findViewById(R.id.headerTitle); // Inisialisasi

        // Set title sesuai konteks Event Package
        headerTitle.setText("Event Package");

        packageList = new ArrayList<>();
        adapter = new PackageAdapter(this, packageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("packages");

        fabAdd.setOnClickListener(view -> {
            startActivity(new Intent(EventPackageActivity.this, TambahPackageActivity.class)); // UBAH INI
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(EventPackageActivity.this, "Anda telah logout.", Toast.LENGTH_SHORT).show(); // UBAH INI
            Intent intent = new Intent(EventPackageActivity.this, LoginActivity.class); // UBAH INI
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnBack.setOnClickListener(v -> {
            onBackPressed(); // Kembali ke aktivitas sebelumnya (DashboardActivity)
        });


        adapter.setOnItemActionListener(new PackageAdapter.OnItemActionListener() {
            @Override
            public void onEditClick(PackageItem item) {
                Toast.makeText(EventPackageActivity.this, "Mengedit: " + item.getNama(), Toast.LENGTH_SHORT).show(); // UBAH INI
                Intent editIntent = new Intent(EventPackageActivity.this, DetailMenuActivity.class); // UBAH INI
                editIntent.putExtra("packageId", item.getPackageId());
                editIntent.putExtra("nama", item.getNama());
                editIntent.putExtra("deskripsi", item.getDeskripsi());
                editIntent.putExtra("harga", item.getHarga());
                editIntent.putExtra("imageUrl", item.getImageUrl());
                startActivity(editIntent);
            }

            @Override
            public void onDeleteClick(PackageItem item, int position) {
                if (item.getPackageId() != null) {
                    databaseReference.child(item.getPackageId()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(EventPackageActivity.this, "Paket '" + item.getNama() + "' berhasil dihapus!", Toast.LENGTH_SHORT).show(); // UBAH INI
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(EventPackageActivity.this, "Gagal menghapus paket: " + e.getMessage(), Toast.LENGTH_LONG).show(); // UBAH INI
                            });
                }
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                packageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PackageItem item = snapshot.getValue(PackageItem.class);
                    if (item != null) {
                        packageList.add(item);
                    }
                }
                adapter.updateData(packageList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EventPackageActivity.this, "Gagal memuat data: " + databaseError.getMessage(), Toast.LENGTH_LONG).show(); // UBAH INI
            }
        });
    }

    public static Bitmap decodeBase64(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }
}
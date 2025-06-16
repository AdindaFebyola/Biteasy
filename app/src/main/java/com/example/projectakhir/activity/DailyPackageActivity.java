package com.example.projectakhir.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectakhir.R;
import com.example.projectakhir.adapter.MenuAdapter;
import com.example.projectakhir.model.MenuItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// Kelas ini mengimplementasikan OnItemActionListener dari MenuAdapter
public class DailyPackageActivity extends AppCompatActivity implements MenuAdapter.OnItemActionListener {

    private static final String TAG = "DailyPackageActivity";

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private ImageView backButton;
    private TextView headerTitle;

    private List<MenuItem> menuList;
    private MenuAdapter adapter; // Nama variabel adapter
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_package);

        // Inisialisasi Views - PASTIKAN ID SUDAH BENAR SESUAI XML
        recyclerView = findViewById(R.id.recycler_view_menu);
        fabAdd = findViewById(R.id.fab_add_menu);
        backButton = findViewById(R.id.iv_back_button);
        headerTitle = findViewById(R.id.headerTitle);

        headerTitle.setText(R.string.daily_package_title); // Mengatur teks dari string resource

        menuList = new ArrayList<>();
        // >>>>>>> PERBAIKAN DI SINI <<<<<<<
        // Teruskan 'this' sebagai listener karena DailyPackageActivity mengimplementasikan OnItemActionListener
        adapter = new MenuAdapter(this, menuList, this); // Tambahkan 'this' sebagai argumen ketiga
        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        // adapter.setOnItemActionListener(this); // Baris ini tidak lagi diperlukan jika listener sudah diteruskan di konstruktor

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("daily_menus");
        Log.d(TAG, "Firebase Database Reference initialized to: " + databaseReference.toString());

        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(DailyPackageActivity.this, AddMenuActivity.class));
        });

        backButton.setOnClickListener(v -> onBackPressed());

        loadMenusFromFirebase();
    }

    private void loadMenusFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange triggered. Raw snapshot: " + snapshot.getValue());
                Log.d(TAG, "Number of children detected: " + snapshot.getChildrenCount());

                List<MenuItem> fetchedMenus = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MenuItem menu = dataSnapshot.getValue(MenuItem.class);
                    if (menu != null) {
                        menu.setKey(dataSnapshot.getKey());
                        fetchedMenus.add(menu);
                        Log.d(TAG, "Successfully added menu: " + menu.getNama() + " (Key: " + menu.getKey() + ") to list.");
                    } else {
                        Log.e(TAG, "FAILED TO PARSE MenuItem for key: " + dataSnapshot.getKey() + ". Check MenuItem.java (constructors/getters) and Firebase data structure.");
                        Log.e(TAG, "Raw data for failed parse: " + dataSnapshot.getValue());
                    }
                }
                adapter.updateData(fetchedMenus);
                Log.d(TAG, "Adapter notified. Current menuList size after update: " + adapter.getItemCount());

                if (fetchedMenus.isEmpty()) {
                    Toast.makeText(DailyPackageActivity.this, "Tidak ada menu yang tersedia.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase data load cancelled: " + error.getMessage(), error.toException());
                Toast.makeText(DailyPackageActivity.this, "Gagal memuat menu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Implementasi metode dari MenuAdapter.OnItemActionListener
    @Override
    public void onEditClick(MenuItem item) {
        Toast.makeText(this, "Mengedit: " + item.getNama(), Toast.LENGTH_SHORT).show();
        Intent editIntent = new Intent(this, EditMenuActivity.class);
        editIntent.putExtra("menu_item", item); // Kirim objek MenuItem (pastikan Serializable)
        editIntent.putExtra("menu_key", item.getKey());
        // Tambahkan pengiriman data lainnya jika EditMenuActivity membutuhkan parameter terpisah
        editIntent.putExtra("menu_name", item.getNama());
        editIntent.putExtra("menu_description", item.getDeskripsi());
        editIntent.putExtra("menu_price", item.getHarga());
        editIntent.putExtra("menu_image_url", item.getImageUrl());
        startActivity(editIntent);
    }

    @Override
    public void onDeleteClick(MenuItem item, int position) { // Perhatikan: onDeleteClick di adapter punya 'position'
        if (item.getKey() != null) {
            databaseReference.child(item.getKey()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Menu '" + item.getNama() + "' berhasil dihapus!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal menghapus menu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to delete menu: " + e.getMessage(), e);
                    });
        }
    }
}
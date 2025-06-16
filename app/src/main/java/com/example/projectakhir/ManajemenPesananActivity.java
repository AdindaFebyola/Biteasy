package com.example.projectakhir;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManajemenPesananActivity extends AppCompatActivity {

    // Variabel untuk UI
    private RecyclerView recyclerView;
    private Button btnTambah;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    // Variabel untuk data dan Adapter
    private PesananAdapter adapter;
    private ArrayList<Pesanan> pesananList;

    // Variabel untuk Firebase
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private ImageView ivLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manajemen_pesanan);

        mAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.recyclerViewPesanan);
        btnTambah = findViewById(R.id.btnTambah);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        ivLogout = findViewById(R.id.ivLogout);

        databaseReference = FirebaseDatabase.getInstance().getReference("pesanan");

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pesananList = new ArrayList<>();
        adapter = new PesananAdapter(this, pesananList);
        recyclerView.setAdapter(adapter);

        btnTambah.setOnClickListener(v -> {
            startActivity(new Intent(ManajemenPesananActivity.this, FormPesananActivity.class));
        });

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        ivLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(ManajemenPesananActivity.this, "Berhasil logout", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ManajemenPesananActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Tutup activity ini
        });

        setupFirebaseListener();
    }

    private void setupFirebaseListener() {
        showLoading(true);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pesananList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Pesanan pesanan = snapshot.getValue(Pesanan.class);
                    if (pesanan != null) {
                        pesanan.setKey(snapshot.getKey());
                        pesananList.add(pesanan);
                    }
                }

                adapter.notifyDataSetChanged();
                showLoading(false);

                if (pesananList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showLoading(false);
                Log.e("ManajemenPesanan", "Gagal membaca data.", databaseError.toException());
                Toast.makeText(ManajemenPesananActivity.this, "Gagal memuat data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}

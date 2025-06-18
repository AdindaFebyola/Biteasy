package com.example.projectakhir;



import android.content.Intent;

import android.os.Bundle;

import android.widget.ImageView;

import android.widget.TextView;

import android.widget.Toast;



import androidx.appcompat.app.AppCompatActivity;

import androidx.cardview.widget.CardView;



import com.example.projectakhir.activity.DailyPackageActivity;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;



public class DashboardActivity extends AppCompatActivity {

    private TextView tvGreeting;

    private ImageView ivLogout;

    private CardView cardDailyPackage;

    private CardView cardLaporan;

    private CardView cardEventPackage; // Deklarasi CardView untuk Event Package


    private CardView cardListPesanan;
    // private CardView cardLaporan;

    private FirebaseAuth mAuth;



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);



        mAuth = FirebaseAuth.getInstance();



        tvGreeting = findViewById(R.id.tvGreeting);

        ivLogout = findViewById(R.id.ivLogout);

        cardDailyPackage = findViewById(R.id.card_daily_package);

        cardEventPackage = findViewById(R.id.card_event_package);

        cardListPesanan = findViewById(R.id.card_list_pesanan);

        cardLaporan = findViewById(R.id.card_laporan);



        displayGreeting();



        cardDailyPackage.setOnClickListener(v -> {

            Intent intent = new Intent(DashboardActivity.this, DailyPackageActivity.class);

            startActivity(intent);

            Toast.makeText(DashboardActivity.this, "Membuka Daily Package", Toast.LENGTH_SHORT).show();

        });



// Set listener untuk CardView Event Package

        cardEventPackage.setOnClickListener(v -> {

            Intent intent = new Intent(DashboardActivity.this, EventPackageActivity.class); // Arahkan ke EventPackageActivity

            startActivity(intent);

            Toast.makeText(DashboardActivity.this, "Membuka Event Package", Toast.LENGTH_SHORT).show();

        });

        cardLaporan.setOnClickListener(v -> {
            // Membuat Intent untuk membuka MainActivity (halaman laporan Anda)
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            startActivity(intent);
        });

        cardListPesanan.setOnClickListener(v -> {
            // Membuat Intent untuk membuka halaman Manajemen Pesanan Anda
            Intent intent = new Intent(DashboardActivity.this, ManajemenPesananActivity.class);
            startActivity(intent);
            Toast.makeText(DashboardActivity.this, "Membuka Manajemen Pesanan", Toast.LENGTH_SHORT).show();
        });



        ivLogout.setOnClickListener(v -> {

            mAuth.signOut();

            Toast.makeText(DashboardActivity.this, "Berhasil logout", Toast.LENGTH_SHORT).show();



            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            finish();

        });

    }



    private void displayGreeting() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {

            String userName;

            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {

                userName = user.getDisplayName();

            } else if (user.getEmail() != null && !user.getEmail().isEmpty()) {

                userName = user.getEmail().split("@")[0];

            } else if (user.isAnonymous()) {

                userName = "Guest";

            } else {

                userName = "User";

            }

            tvGreeting.setText("Good Morning, " + userName);

        } else {

            tvGreeting.setText("Good Morning, Guest!");

        }

    }

}


package com.example.projectakhir;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout; // <-- Import LinearLayout
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class FinancialReportActivity extends AppCompatActivity {

    // --- Deklarasi untuk komponen yang ada di layout ---
    private Button btnUploadJoko;
    private Button btnUploadSintya;
    private ImageView ivBack;
    private EditText etSearch;
    private LinearLayout cardJoko;
    private LinearLayout cardSintya;

    // Variabel untuk "mengingat" tombol mana yang diklik saat memilih gambar
    private Button clickedButton;

    // Launcher untuk membuka galeri gambar
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial_report);

        // --- Inisialisasi semua komponen UI ---
        btnUploadJoko = findViewById(R.id.btnUploadJoko);
        btnUploadSintya = findViewById(R.id.btnUploadSintya);
        ivBack = findViewById(R.id.ivBack);
        etSearch = findViewById(R.id.etSearch);
        cardJoko = findViewById(R.id.cardJoko);
        cardSintya = findViewById(R.id.cardSintya);


        // --- Siapkan launcher untuk membuka galeri ---
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    // Ini akan dijalankan setelah pengguna memilih gambar
                    if (uri != null && clickedButton != null) {
                        // Ubah tombol yang sebelumnya sudah kita "simpan" di variabel clickedButton
                        clickedButton.setText("Upload Berhasil ✓");
                        clickedButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                        clickedButton.setBackgroundColor(0xFF4CAF50); // Kode hex untuk warna hijau
                        clickedButton.setEnabled(false);

                        // Reset variabel setelah selesai agar siap untuk aksi berikutnya
                        clickedButton = null;
                    }
                }
        );

        // --- Atur listener untuk semua tombol ---
        setupListeners();
    }

    private void setupListeners() {
        // Atur listener untuk tombol kembali
        ivBack.setOnClickListener(v -> finish());

        // Atur listener untuk tombol upload Joko
        btnUploadJoko.setOnClickListener(v -> {
            clickedButton = btnUploadJoko; // "Ingat" bahwa tombol Joko yang diklik
            pickImageLauncher.launch("image/*"); // Buka galeri
        });

        // Atur listener untuk tombol upload Sintya
        btnUploadSintya.setOnClickListener(v -> {
            clickedButton = btnUploadSintya; // "Ingat" bahwa tombol Sintya yang diklik
            pickImageLauncher.launch("image/*"); // Buka galeri
        });

        // Atur listener untuk kolom pencarian
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                // Ambil teks pencarian dan ubah ke huruf kecil
                String query = s.toString().toLowerCase();

                // Cek kartu Joko
                if ("joko walid".contains(query)) {
                    cardJoko.setVisibility(View.VISIBLE);
                } else {
                    cardJoko.setVisibility(View.GONE);
                }

                // Cek kartu Sintya
                if ("sintya dwija".contains(query)) {
                    cardSintya.setVisibility(View.VISIBLE);
                } else {
                    cardSintya.setVisibility(View.GONE);
                }
            }
        });
    }
}
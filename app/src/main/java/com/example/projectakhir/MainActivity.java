package com.example.projectakhir;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull; // Penting: Pastikan import ini ada!
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Import Log untuk debugging
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException; // Import untuk DateFormat
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LaporanAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private LaporanAdapter laporanAdapter;

    // "Dokumen Asli" yang menyimpan semua data secara permanen (selama aplikasi berjalan)
    // List ini akan disinkronkan langsung dengan Firebase
    private List<Laporan> masterLaporanList = new ArrayList<>();
    // "Fotokopian" yang hanya digunakan untuk ditampilkan, isinya bisa berubah (difilter)
    private List<Laporan> displayedLaporanList = new ArrayList<>();

    private Spinner spinnerCategory;
    private EditText editTextDate, editJumlahPesanan, editTotalPendapatan;
    private Button btnSimpan;

    private TextView tvHeaderTitle;
    private ImageButton btnBack;
    private ChipGroup chipGroupFilter;
    private Chip chipFilterSemua, chipFilterDaily, chipFilterEvent; // Ditambahkan untuk akses langsung
    private ExtendedFloatingActionButton fabLaporanKeuangan;

    private ActivityResultLauncher<Intent> editLaporanLauncher;

    // Firebase
    private DatabaseReference mDatabase;
    private static final String TAG = "MainActivity"; // Untuk logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi Firebase Database reference
        // Pastikan URL database Anda benar, termasuk region (asia-southeast1)
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://projectakhir-914fe-default-rtdb.asia-southeast1.firebasedatabase.app/");
        mDatabase = database.getReference("laporan"); // "laporan" akan menjadi node root untuk data Anda

        initViews();
        setupRecyclerView();
        setupSpinner();
        setupListeners();
        setupActivityLaunchers();
        readLaporanFromFirebase(); // Baca data dari Firebase saat Activity dimulai
    }

    private void initViews() {
        tvHeaderTitle = findViewById(R.id.tvWelcomeMessage);
        btnBack = findViewById(R.id.btnLogout);
        fabLaporanKeuangan = findViewById(R.id.fabLaporanKeuangan);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        chipFilterSemua = findViewById(R.id.chipFilterSemua);
        chipFilterDaily = findViewById(R.id.chipFilterDaily); // Inisialisasi
        chipFilterEvent = findViewById(R.id.chipFilterEvent); // Inisialisasi

        spinnerCategory = findViewById(R.id.spinnerCategoryTop);
        editTextDate = findViewById(R.id.editTextDateTop);
        editJumlahPesanan = findViewById(R.id.editTextJumlahPesanan);
        editTotalPendapatan = findViewById(R.id.editTextTotalPendapatan);
        btnSimpan = findViewById(R.id.btnSimpan);
        recyclerView = findViewById(R.id.recyclerViewLaporan);
    }

    private void setupRecyclerView() {
        // PENTING: Adapter sekarang menggunakan displayedLaporanList
        laporanAdapter = new LaporanAdapter(displayedLaporanList);
        laporanAdapter.setOnItemClickListener(this); // Mengatur listener untuk klik item (hapus/edit)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(laporanAdapter);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.kategori_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupActivityLaunchers() {
        // Launcher untuk menerima hasil dari EditLaporanActivity
        editLaporanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Laporan laporanHasilEdit = (Laporan) result.getData().getSerializableExtra("laporan_hasil_edit");

                        // Pastikan objek laporan yang diedit tidak null dan memiliki ID Firebase
                        if (laporanHasilEdit != null && laporanHasilEdit.getId() != null) {
                            Log.d(TAG, "Mencoba memperbarui Laporan dengan ID: " + laporanHasilEdit.getId());
                            // Perbarui item di Firebase menggunakan ID-nya sebagai kunci child
                            mDatabase.child(laporanHasilEdit.getId()).setValue(laporanHasilEdit)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show();
                                        // Tidak perlu memperbarui masterLaporanList atau displayedLaporanList secara manual di sini.
                                        // ValueEventListener (di readLaporanFromFirebase) akan otomatis memperbaruinya.
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Gagal memperbarui data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Gagal memperbarui data untuk ID: " + laporanHasilEdit.getId(), e);
                                    });
                        } else {
                            Toast.makeText(this, "Gagal memperbarui data: Laporan atau ID tidak ditemukan.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Pembaruan gagal: Objek Laporan atau ID-nya null.");
                        }
                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        Toast.makeText(this, "Pembaruan laporan dibatalkan.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupListeners() {
        // Listener untuk tombol kembali/logout (jika ada)
        btnBack.setOnClickListener(v -> finish());

        // Listener untuk perubahan pilihan chip filter
        chipGroupFilter.setOnCheckedChangeListener((group, checkedIds) -> {
            filterCurrentlySelectedChip(); // Panggil fungsi filter saat chip berubah
        });

        // Listener untuk EditText Tanggal (membuka DatePickerDialog)
        editTextDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            // Jika ada tanggal di EditText, gunakan itu sebagai tanggal awal DatePicker
            Calendar initialDate = Calendar.getInstance();
            if (!editTextDate.getText().toString().isEmpty()) {
                try {
                    // Pastikan format ini cocok dengan format yang Anda simpan
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("id", "ID"));
                    initialDate.setTime(sdf.parse(editTextDate.getText().toString()));
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date for DatePicker: " + e.getMessage());
                    // Fallback ke tanggal saat ini jika parsing gagal
                    initialDate = Calendar.getInstance();
                }
            }

            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth);
                // Format tanggal harus konsisten untuk ditampilkan dan disimpan
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("id", "ID"));
                editTextDate.setText(sdf.format(selected.getTime()));
            }, initialDate.get(Calendar.YEAR), initialDate.get(Calendar.MONTH), initialDate.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Listener untuk tombol simpan
        btnSimpan.setOnClickListener(v -> {
            String kategori = spinnerCategory.getSelectedItem().toString();
            String tanggal = editTextDate.getText().toString();
            String jumlahPesanan = editJumlahPesanan.getText().toString();
            String totalPendapatan = editTotalPendapatan.getText().toString();

            if (tanggal.isEmpty() || jumlahPesanan.isEmpty() || totalPendapatan.isEmpty()) {
                Toast.makeText(this, "Mohon isi semua data", Toast.LENGTH_SHORT).show();
                return;
            }
            addLaporanToFirebase(kategori, tanggal, jumlahPesanan, totalPendapatan);
            clearInputFields(); // Bersihkan input setelah data disimpan
        });

        // Listener untuk Floating Action Button Laporan Keuangan
        fabLaporanKeuangan.setOnClickListener(v -> {
            // Asumsi FinancialReportActivity sudah ada dan terhubung
            startActivity(new Intent(MainActivity.this, FinancialReportActivity.class));
        });
    }

    // Metode untuk membersihkan input fields
    private void clearInputFields() {
        editTextDate.setText("");
        editJumlahPesanan.setText("");
        editTotalPendapatan.setText("");
        spinnerCategory.setSelection(0); // Reset spinner ke item pertama
    }

    // Metode untuk memfilter laporan berdasarkan chip yang sedang terpilih
    private void filterCurrentlySelectedChip() {
        int checkedId = chipGroupFilter.getCheckedChipId();
        if (checkedId == -1 || checkedId == chipFilterSemua.getId()) { // Cek berdasarkan ID chip
            filterLaporan("Semua");
        } else if (checkedId == chipFilterDaily.getId()) {
            filterLaporan("Daily Package");
        } else if (checkedId == chipFilterEvent.getId()) {
            filterLaporan("Event Package");
        }
    }

    // Metode untuk menambahkan laporan baru ke Firebase
    private void addLaporanToFirebase(String kategori, String tanggal, String jumlahPesanan, String totalPendapatan) {
        // Hasilkan kunci unik untuk entri laporan baru di Firebase
        String laporanId = mDatabase.push().getKey();
        Laporan laporan = new Laporan(kategori, tanggal, jumlahPesanan, totalPendapatan);
        laporan.setId(laporanId); // Set ID yang dihasilkan ke objek Laporan

        if (laporanId != null) {
            mDatabase.child(laporanId).setValue(laporan) // Simpan objek Laporan ke Firebase
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Laporan berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                        chipFilterSemua.setChecked(true); // Ini akan otomatis memicu filter untuk menampilkan semua
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal menambahkan laporan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Gagal menambahkan laporan", e);
                    });
        } else {
            Toast.makeText(this, "Gagal menambahkan laporan: ID tidak dapat dibuat.", Toast.LENGTH_SHORT).show();
        }
    }

    // Metode untuk membaca data laporan dari Firebase
    private void readLaporanFromFirebase() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                masterLaporanList.clear(); // Bersihkan daftar master sebelum mengisi ulang
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Laporan laporan = snapshot.getValue(Laporan.class);
                    if (laporan != null) {
                        laporan.setId(snapshot.getKey()); // Set ID dari kunci Firebase
                        masterLaporanList.add(laporan);
                    }
                }
                // Urutkan daftar master berdasarkan tanggal dalam urutan menurun (terbaru pertama)
                // Ini penting agar daftar selalu terurut setelah data diperbarui dari Firebase
                Collections.sort(masterLaporanList, (o1, o2) -> {
                    try {
                        // Pastikan format tanggal di sini cocok dengan format yang Anda simpan
                        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("id", "ID"));
                        return sdf.parse(o2.getTanggal()).compareTo(sdf.parse(o1.getTanggal()));
                    } catch (ParseException e) {
                        Log.e(TAG, "Kesalahan parsing tanggal saat sorting: " + e.getMessage());
                        return 0; // Atau tangani kesalahan dengan cara yang sesuai
                    }
                });

                filterCurrentlySelectedChip(); // Terapkan kembali filter yang sedang dipilih setelah data diperbarui
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Gagal membaca nilai dari Firebase.", databaseError.toException());
                Toast.makeText(MainActivity.this, "Gagal memuat data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Metode untuk memfilter dan menampilkan laporan
    private void filterLaporan(String kategori) {
        displayedLaporanList.clear(); // Hapus isi "fotokopian"

        if (kategori.equalsIgnoreCase("Semua")) {
            displayedLaporanList.addAll(masterLaporanList); // Tampilkan semua dari master list
        } else {
            for (Laporan item : masterLaporanList) {
                if (item.getKategori().equalsIgnoreCase(kategori)) {
                    displayedLaporanList.add(item); // Tambahkan yang cocok dengan filter
                }
            }
        }
        laporanAdapter.notifyDataSetChanged(); // Beri tahu adapter bahwa data telah berubah
    }

    // Callback saat tombol Hapus pada item RecyclerView diklik
    @Override
    public void onHapusClick(int position) {
        Laporan laporanToDelete = displayedLaporanList.get(position);
        if (laporanToDelete.getId() != null) {
            // Hapus item dari Firebase menggunakan ID-nya
            mDatabase.child(laporanToDelete.getId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Laporan berhasil dihapus", Toast.LENGTH_SHORT).show();
                        // Tidak perlu menghapus dari list secara manual; ValueEventListener akan menanganinya.
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal menghapus laporan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Gagal menghapus laporan", e);
                    });
        } else {
            Toast.makeText(this, "Gagal menghapus: ID laporan tidak ditemukan", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Percobaan menghapus laporan dengan ID null.");
        }
    }

    // Callback saat tombol Edit pada item RecyclerView diklik
    @Override
    public void onEditClick(int position) {
        Laporan laporanToEdit = displayedLaporanList.get(position);
        Log.d(TAG, "Mengarahkan ke EditLaporanActivity untuk ID: " + laporanToEdit.getId()); // Log ID yang akan diedit
        Intent intent = new Intent(MainActivity.this, EditLaporanActivity.class);
        intent.putExtra("laporan_data", laporanToEdit); // Kirim objek Laporan lengkap (termasuk ID)
        editLaporanLauncher.launch(intent); // Luncurkan activity untuk edit
    }
}
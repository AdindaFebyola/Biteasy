package com.example.projectakhir;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Import Log for debugging
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditLaporanActivity extends AppCompatActivity {

    private Spinner spinnerEditKategori;
    private EditText etEditJumlahPesanan, etEditTotalPendapatan, etEditTanggal;
    private Button btnSimpanPerubahan;
    private Toolbar toolbar;

    private Laporan laporan; // This will hold the Laporan object with its Firebase ID
    // private int position; // No longer needed as we use Laporan's ID for updates

    private static final String TAG = "EditLaporanActivity"; // For logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_laporan);

        toolbar = findViewById(R.id.toolbarEdit);
        spinnerEditKategori = findViewById(R.id.spinnerEditKategori);
        etEditTanggal = findViewById(R.id.etEditTanggal);
        etEditJumlahPesanan = findViewById(R.id.etEditJumlahPesanan);
        etEditTotalPendapatan = findViewById(R.id.etEditTotalPendapatan);
        btnSimpanPerubahan = findViewById(R.id.btnSimpanPerubahan);

        setupToolbar();
        setupSpinner();
        setupDatePicker();

        // Retrieve the Laporan object that was passed from MainActivity
        laporan = (Laporan) getIntent().getSerializableExtra("laporan_data");
        // position = getIntent().getIntExtra("laporan_posisi", -1); // Removed as it's not used for Firebase updates

        if (laporan != null) {
            // Log the ID to verify it's received correctly
            Log.d(TAG, "Received Laporan for editing. ID: " + laporan.getId() + ", Kategori: " + laporan.getKategori());

            etEditTanggal.setText(laporan.getTanggal());
            etEditJumlahPesanan.setText(laporan.getJumlahPesanan());
            etEditTotalPendapatan.setText(laporan.getTotalPendapatan());
            setSpinnerSelection(laporan.getKategori());
        } else {
            // Handle case where no Laporan object was passed (shouldn't happen if called correctly)
            Toast.makeText(this, "Kesalahan: Data laporan tidak ditemukan.", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if no data
        }

        btnSimpanPerubahan.setOnClickListener(v -> saveChanges());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Laporan"); // Set a title for the toolbar
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.kategori_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditKategori.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etEditTanggal.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            // Using current year, month, day of existing date if available
            // This is a more robust way to initialize the date picker
            Calendar initialDate = Calendar.getInstance();
            if (!etEditTanggal.getText().toString().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("id", "ID")); // Added yyyy
                    initialDate.setTime(sdf.parse(etEditTanggal.getText().toString()));
                } catch (java.text.ParseException e) {
                    Log.e(TAG, "Error parsing date for DatePicker: " + e.getMessage());
                    // Fallback to current date if parsing fails
                    initialDate = Calendar.getInstance();
                }
            }


            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth);
                // Ensure the date format is consistent (including year)
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("id", "ID")); // Changed to yyyy
                etEditTanggal.setText(sdf.format(selected.getTime()));
            }, initialDate.get(Calendar.YEAR), initialDate.get(Calendar.MONTH), initialDate.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setSpinnerSelection(String kategori) {
        ArrayAdapter adapter = (ArrayAdapter) spinnerEditKategori.getAdapter();
        if (adapter != null) { // Add null check for adapter
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equalsIgnoreCase(kategori)) {
                    spinnerEditKategori.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveChanges() {
        String kategoriBaru = spinnerEditKategori.getSelectedItem().toString();
        String tanggalBaru = etEditTanggal.getText().toString();
        String jumlahBaru = etEditJumlahPesanan.getText().toString();
        String pendapatanBaru = etEditTotalPendapatan.getText().toString();

        if (jumlahBaru.isEmpty() || pendapatanBaru.isEmpty()) {
            Toast.makeText(this, "Data tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the 'laporan' object is not null. It should always be if onCreate worked.
        if (laporan != null) {
            // 1. UPDATE THE EXISTING 'laporan' OBJECT WITH NEW DATA
            laporan.setKategori(kategoriBaru);
            laporan.setTanggal(tanggalBaru);
            laporan.setJumlahPesanan(jumlahBaru);
            laporan.setTotalPendapatan(pendapatanBaru);

            // Log the ID before sending back to verify it's still there
            Log.d(TAG, "Sending back updated Laporan. ID: " + laporan.getId());

            // 2. Prepare Intent to send the UPDATED 'laporan' object back to MainActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("laporan_hasil_edit", laporan); // Send the object that HAS its ID
            // resultIntent.putExtra("laporan_posisi", position); // This line is no longer needed
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Kesalahan: Laporan tidak dapat diperbarui (objek null).", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Attempted to save changes but 'laporan' object was null.");
            setResult(RESULT_CANCELED); // Indicate failure to MainActivity
            finish();
        }
    }
}
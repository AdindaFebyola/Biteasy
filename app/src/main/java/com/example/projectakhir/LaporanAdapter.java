package com.example.projectakhir;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LaporanAdapter extends RecyclerView.Adapter<LaporanAdapter.ViewHolder> {

    private List<Laporan> listToDisplay;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onHapusClick(int position);
        void onEditClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public LaporanAdapter(List<Laporan> laporanList) {
        this.listToDisplay = laporanList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategory, txtDate, txtJumlahPesanan, txtTotalPendapatan;
        Button btnHapus, btnEdit; // Menggunakan Button agar cocok dengan layout Anda

        public ViewHolder(View itemView) {
            super(itemView);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtJumlahPesanan = itemView.findViewById(R.id.txtJumlahPesanan);
            txtTotalPendapatan = itemView.findViewById(R.id.txtTotalPendapatan);
            btnHapus = itemView.findViewById(R.id.btnHapusCard);
            btnEdit = itemView.findViewById(R.id.btnEditCard);

            btnHapus.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) listener.onHapusClick(position);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) listener.onEditClick(position);
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Laporan laporan = listToDisplay.get(position);
        holder.txtCategory.setText(laporan.getKategori());
        holder.txtDate.setText(laporan.getTanggal());
        holder.txtJumlahPesanan.setText("Jumlah Pesanan : " + laporan.getJumlahPesanan());
        holder.txtTotalPendapatan.setText("Total Pendapatan : Rp " + laporan.getTotalPendapatan());
    }

    @Override
    public int getItemCount() {
        return listToDisplay.size();
    }

    // Method untuk memberitahu adapter agar me-refresh tampilannya dengan data baru
    public void updateList(List<Laporan> newList) {
        this.listToDisplay = newList;
        notifyDataSetChanged();
    }
}
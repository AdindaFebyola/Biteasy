package com.example.projectakhir;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class PesananAdapter extends RecyclerView.Adapter<PesananAdapter.ViewHolder> {

    private Activity context;
    private List<Pesanan> pesananList;

    public PesananAdapter(Activity context, List<Pesanan> pesananList) {
        this.context = context;
        this.pesananList = pesananList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pesanan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pesanan pesanan = pesananList.get(position);

        holder.tvNamaPemesan.setText(pesanan.getNamaPelanggan());
        holder.tvMenu.setText(pesanan.getNamaMenu());
        holder.tvHarga.setText("Rp " + pesanan.getTotalHarga());

        String imageBase64 = pesanan.getImageBase64();
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.imgPesanan.setImageBitmap(decodedByte);
        } else {
            holder.imgPesanan.setImageResource(R.drawable.img_food);
        }

        String status = pesanan.getStatus();
        holder.tvStatus.setText(status); // Set teks status

        switch (status.trim().toLowerCase()) {
            case "baru":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_dropdown_baru);
                break;
            case "diproses":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_dropdown_diproses);
                break;
            case "selesai":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_dropdown_selesai);
                break;
            case "dibatalkan":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_dropdown_dibatalkan);
                break;
            default:
                holder.tvStatus.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray));
                break;
        }

        holder.btnEditPesanan.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditPesananActivity.class);
            intent.putExtra("PESANAN_KEY", pesanan.getKey());
            context.startActivity(intent);
        });

        holder.btnDeletePesanan.setOnClickListener(v -> {
            DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("pesanan").child(pesanan.getKey());
            itemRef.removeValue().addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Pesanan dihapus", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Gagal menghapus pesanan", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public int getItemCount() {
        return pesananList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaPemesan, tvMenu, tvStatus, tvHarga;
        ImageView imgPesanan, btnEditPesanan, btnDeletePesanan;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaPemesan = itemView.findViewById(R.id.tvNamaPemesan);
            tvMenu = itemView.findViewById(R.id.tvMenu);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvHarga = itemView.findViewById(R.id.tvHarga);
            imgPesanan = itemView.findViewById(R.id.imgPesanan);
            btnEditPesanan = itemView.findViewById(R.id.btnEditPesanan);
            btnDeletePesanan = itemView.findViewById(R.id.btnDeletePesanan);
        }
    }
}

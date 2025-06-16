package com.example.projectakhir.adapter; // Pastikan package ini benar

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Jika Anda menggunakan Glide
import com.example.projectakhir.R;
import com.example.projectakhir.model.MenuItem; // Pastikan ini mengacu pada model yang benar

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private final Context context; // Gunakan final jika tidak diubah setelah konstruktor
    private List<MenuItem> menuList;
    private OnItemActionListener listener; // Interface untuk klik edit/delete

    // INTERFACE SAMA SEPERTI DI PackageAdapter
    public interface OnItemActionListener {
        void onEditClick(MenuItem item); // Mengirim objek MenuItem
        void onDeleteClick(MenuItem item, int position); // Mengirim objek MenuItem dan posisinya
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    // Ubah konstruktor agar menerima listener
    public MenuAdapter(Context context, List<MenuItem> menuList, OnItemActionListener listener) {
        this.context = context;
        this.menuList = menuList;
        this.listener = listener; // Set listener di konstruktor
    }

    // Metode updateData() agar mirip PackageAdapter
    public void updateData(List<MenuItem> newMenuList) {
        this.menuList = newMenuList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem menu = menuList.get(position);

        // SESUAIKAN DENGAN NAMA VARIABEL BARU DI VIEWHOLDER
        holder.packageTitle.setText(menu.getNama());
        if (menu.getDeskripsi() != null && !menu.getDeskripsi().isEmpty()) {
            holder.packageDesc.setText("Pesanan: " + menu.getDeskripsi());
            holder.packageDesc.setVisibility(View.VISIBLE);
        } else {
            holder.packageDesc.setVisibility(View.GONE);
        }
        holder.packagePrice.setText("Rp " + menu.getHarga());

        // Load gambar (jika itu base64 string)
        if (menu.getImageUrl() != null && !menu.getImageUrl().isEmpty()) {
            try {
                // Asumsi imageUrl adalah Base64 String seperti di TambahPackageActivity
                byte[] decodedString = Base64.decode(menu.getImageUrl(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Glide.with(context)
                        .load(decodedByte) // Load Bitmap
                        .placeholder(R.drawable.ic_image_placeholder)
                        // .error(R.drawable.ic_image_error) // Pastikan drawable ini ada atau hapus baris ini
                        .centerCrop()
                        .into(holder.packageImage); // SESUAIKAN DENGAN NAMA VARIABEL BARU
            } catch (IllegalArgumentException e) {
                // Jika bukan Base64 yang valid atau ada masalah dekode
                e.printStackTrace(); // Penting untuk debugging
                Glide.with(context)
                        .load(R.drawable.ic_image_placeholder) // Tampilkan placeholder
                        .centerCrop()
                        .into(holder.packageImage); // SESUAIKAN DENGAN NAMA VARIABEL BARU
            }
        } else {
            holder.packageImage.setImageResource(R.drawable.ic_image_placeholder); // SESUAIKAN DENGAN NAMA VARIABEL BARU
        }

        // Set listeners untuk tombol edit dan delete
        holder.ivEditItem.setOnClickListener(v -> { // SESUAIKAN DENGAN NAMA VARIABEL BARU
            if (listener != null) {
                listener.onEditClick(menuList.get(holder.getAdapterPosition()));
            }
        });

        holder.ivDeleteItem.setOnClickListener(v -> { // SESUAIKAN DENGAN NAMA VARIABEL BARU
            if (listener != null) {
                listener.onDeleteClick(menuList.get(holder.getAdapterPosition()), holder.getAdapterPosition());
            }
        });

        // Tambahan: Jika seluruh item bisa diklik untuk detail
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                // Jika ada method onItemClick di listener, panggil di sini
                // listener.onItemClick(menuList.get(holder.getAdapterPosition()));
                // Karena Anda tidak punya onItemClick di OnItemActionListener Anda,
                // baris ini mungkin tidak perlu atau perlu ditambahkan ke interface
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        // Deklarasi variabel harus sesuai dengan ID di item_menu.xml
        ImageView packageImage; // SESUAIKAN DENGAN ID: @id/packageImage
        TextView packageTitle; // SESUAIKAN DENGAN ID: @id/packageTitle
        TextView packageDesc; // SESUAIKAN DENGAN ID: @id/packageDesc
        TextView packagePrice; // SESUAIKAN DENGAN ID: @id/packagePrice
        ImageView ivEditItem; // SESUAIKAN DENGAN ID: @id/ivEditItem
        ImageView ivDeleteItem; // SESUAIKAN DENGAN ID: @id/ivDeleteItem

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inisialisasi variabel dengan ID yang sesuai dari item_menu.xml
            packageImage = itemView.findViewById(R.id.packageImage);
            packageTitle = itemView.findViewById(R.id.packageTitle);
            packageDesc = itemView.findViewById(R.id.packageDesc);
            packagePrice = itemView.findViewById(R.id.packagePrice);
            ivEditItem = itemView.findViewById(R.id.ivEditItem);
            ivDeleteItem = itemView.findViewById(R.id.ivDeleteItem);
        }
    }
}
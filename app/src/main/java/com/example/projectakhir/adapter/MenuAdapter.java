package com.example.projectakhir.adapter;

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

import com.bumptech.glide.Glide;
import com.example.projectakhir.R;
import com.example.projectakhir.model.MenuItem;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private final Context context;
    private List<MenuItem> menuList;
    private OnItemActionListener listener;
    public interface OnItemActionListener {
        void onEditClick(MenuItem item);
        void onDeleteClick(MenuItem item, int position);
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    public MenuAdapter(Context context, List<MenuItem> menuList, OnItemActionListener listener) {
        this.context = context;
        this.menuList = menuList;
        this.listener = listener;
    }
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
        holder.packageTitle.setText(menu.getNama());
        if (menu.getDeskripsi() != null && !menu.getDeskripsi().isEmpty()) {
            holder.packageDesc.setText("Pesanan: " + menu.getDeskripsi());
            holder.packageDesc.setVisibility(View.VISIBLE);
        } else {
            holder.packageDesc.setVisibility(View.GONE);
        }
        holder.packagePrice.setText("Rp " + menu.getHarga());
        if (menu.getImageUrl() != null && !menu.getImageUrl().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(menu.getImageUrl(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Glide.with(context)
                        .load(decodedByte)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .centerCrop()
                        .into(holder.packageImage);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Glide.with(context)
                        .load(R.drawable.ic_image_placeholder)
                        .centerCrop()
                        .into(holder.packageImage);
            }
        } else {
            holder.packageImage.setImageResource(R.drawable.ic_image_placeholder);
        }
        holder.ivEditItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(menuList.get(holder.getAdapterPosition()));
            }
        });

        holder.ivDeleteItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(menuList.get(holder.getAdapterPosition()), holder.getAdapterPosition());
            }
        });
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView packageImage;
        TextView packageTitle;
        TextView packageDesc;
        TextView packagePrice;
        ImageView ivEditItem;
        ImageView ivDeleteItem;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            packageImage = itemView.findViewById(R.id.packageImage);
            packageTitle = itemView.findViewById(R.id.packageTitle);
            packageDesc = itemView.findViewById(R.id.packageDesc);
            packagePrice = itemView.findViewById(R.id.packagePrice);
            ivEditItem = itemView.findViewById(R.id.ivEditItem);
            ivDeleteItem = itemView.findViewById(R.id.ivDeleteItem);
        }
    }
}
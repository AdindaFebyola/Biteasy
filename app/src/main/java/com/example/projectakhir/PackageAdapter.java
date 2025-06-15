package com.example.projectakhir;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {

    private List<PackageItem> packageList;
    private Context context;

    public interface OnItemActionListener {
        void onEditClick(PackageItem item);
        void onDeleteClick(PackageItem item, int position);
    }

    private OnItemActionListener listener;

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    public PackageAdapter(Context context, List<PackageItem> packageList) {
        this.context = context;
        this.packageList = packageList;
    }

    public void updateData(List<PackageItem> newPackageList) {
        this.packageList = newPackageList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, ivEditItem, ivDeleteItem;
        TextView title, description, price;

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.packageImage);
            title = view.findViewById(R.id.packageTitle);
            description = view.findViewById(R.id.packageDesc);
            price = view.findViewById(R.id.packagePrice);
            ivEditItem = view.findViewById(R.id.ivEditItem);
            ivDeleteItem = view.findViewById(R.id.ivDeleteItem);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_package, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PackageItem item = packageList.get(position);

        holder.title.setText(item.getNama());
        holder.description.setText(item.getDeskripsi());
        holder.price.setText(item.getHarga());

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(item.getImageUrl(), Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.imageView.setImageBitmap(decodedBitmap);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                holder.imageView.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            holder.imageView.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.ivEditItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(item);
            }
        });

        holder.ivDeleteItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return packageList.size();
    }
}

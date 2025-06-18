package com.example.projectakhir;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class StatusAdapter extends ArrayAdapter<String> {

    public StatusAdapter(@NonNull Context context, String[] statuses) {
        super(context, R.layout.dropdown_item_status, statuses);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = super.getDropDownView(position, convertView, parent);
        String status = getItem(position);

        if (status != null) {
            switch (status) {
                case "Baru":
                    row.setBackgroundResource(R.drawable.bg_status_dropdown_baru);
                    break;
                case "Diproses":
                    row.setBackgroundResource(R.drawable.bg_status_dropdown_diproses);
                    break;
                case "Selesai":
                    row.setBackgroundResource(R.drawable.bg_status_dropdown_selesai);
                    break;
                case "Dibatalkan":
                    row.setBackgroundResource(R.drawable.bg_status_dropdown_dibatalkan);
                    break;
                default:
                    row.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
                    break;
            }
        }
        return row;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = view.findViewById(android.R.id.text1);

        String status = getItem(position);
        int textColor = ContextCompat.getColor(getContext(), R.color.black); // Warna default

        if (status != null) {
            switch (status) {
                case "Baru":
                    textColor = ContextCompat.getColor(getContext(), R.color.status_baru);
                    break;
                case "Diproses":
                    textColor = ContextCompat.getColor(getContext(), R.color.status_diproses);
                    break;
                case "Selesai":
                    textColor = ContextCompat.getColor(getContext(), R.color.status_selesai);
                    break;
                case "Dibatalkan":
                    textColor = ContextCompat.getColor(getContext(), R.color.status_dibatalkan);
                    break;
            }
        }
        textView.setTextColor(textColor);
        return view;
    }
}
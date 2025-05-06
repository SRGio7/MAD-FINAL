package com.example.cmaba_java;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HealthCheckResultAdapter extends RecyclerView.Adapter<HealthCheckResultAdapter.HealthCheckViewHolder> {

    private List<HealthCheckResult> healthCheckResults;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDownloadClick(HealthCheckResult result);
    }

    public HealthCheckResultAdapter(Context context, List<HealthCheckResult> healthCheckResults, OnItemClickListener listener) {
        this.context = context;
        this.healthCheckResults = healthCheckResults;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HealthCheckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_health_check_result, parent, false);
        return new HealthCheckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HealthCheckViewHolder holder, int position) {
        HealthCheckResult result = healthCheckResults.get(position);

        holder.tvMcuDate.setText(result.getMcuDate());
        holder.tvName.setText(result.getNama());
        holder.tvDob.setText(result.getDateOfBirth());

        String notes = result.getAdditionalNotes();
        if (notes != null && !notes.isEmpty()) {
            holder.tvNotes.setText(notes);
        } else {
            holder.tvNotes.setText("No additional notes");
        }

        // Load and display photo
        if (result.getFoto() != null && !result.getFoto().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(result.getFoto(), Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivHealthCheckPhoto.setImageBitmap(decodedBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        holder.btnDownload.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDownloadClick(result);
            }
        });
    }

    @Override
    public int getItemCount() {
        return healthCheckResults.size();
    }

    public void updateData(List<HealthCheckResult> newData) {
        this.healthCheckResults = newData;
        notifyDataSetChanged();
    }

    public static class HealthCheckViewHolder extends RecyclerView.ViewHolder {
        TextView tvMcuDate, tvName, tvDob, tvNotes;
        ImageView ivHealthCheckPhoto;
        Button btnDownload;

        public HealthCheckViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMcuDate = itemView.findViewById(R.id.tvMcuDate);
            tvName = itemView.findViewById(R.id.tvName);
            tvDob = itemView.findViewById(R.id.tvDob);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            ivHealthCheckPhoto = itemView.findViewById(R.id.ivHealthCheckPhoto);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
    }
}
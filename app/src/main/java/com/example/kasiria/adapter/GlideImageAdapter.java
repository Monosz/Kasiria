package com.example.kasiria.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kasiria.MainActivity;
import com.example.kasiria.R;
import com.example.kasiria.ui.GlideImageActivity;

import java.util.List;

public class GlideImageAdapter extends RecyclerView.Adapter<GlideImageAdapter.GlideImageHolder> {
    private Context context;
    private List<Integer> images;

    public GlideImageAdapter(Context context, List<Integer> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public GlideImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_glide_image, parent, false);
        return new GlideImageHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull GlideImageHolder holder, int position) {
        int image = images.get(position);
        Glide.with(context).load(image).into(holder.ivIGlideImage);
        holder.ivIGlideImage.setOnClickListener(v -> {
            Intent intent = new Intent(context, GlideImageActivity.class);
            intent.putExtra("image", image);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                    (Activity) context, holder.ivIGlideImage, "image"
            );
            context.startActivity(intent, options.toBundle());
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class GlideImageHolder extends RecyclerView.ViewHolder {
        ImageView ivIGlideImage;

        public GlideImageHolder(@NonNull View itemView) {
            super(itemView);
            ivIGlideImage = itemView.findViewById(R.id.ivIGlideImage);
        }
    }
}

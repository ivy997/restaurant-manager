package com.example.restaurantmanager.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.restaurantmanager.R;
import com.example.restaurantmanager.activities.ListCategoriesActivity;
import com.example.restaurantmanager.activities.UpdateCategoryActivity;
import com.example.restaurantmanager.models.Category;

import java.util.List;

import util.Callback;
import util.FirebaseManager;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<Category> categories;
    private OnItemClickListener listener;
    private FirebaseManager firebaseManager;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public CategoryAdapter(List<Category> categories) {
        this.categories = categories;
        this.firebaseManager = new FirebaseManager();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        public TextView categoryNameTextView;
        public ImageView categoryImage;
        public View foregroundView;
        private Button editCategory;
        private Button deleteCategory;


        public CategoryViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.category_name_text_view);
            categoryImage = itemView.findViewById(R.id.categoryImg);
            foregroundView = itemView.findViewById(R.id.foreground_view);
            editCategory = itemView.findViewById(R.id.editCat);
            deleteCategory = itemView.findViewById(R.id.deleteCat);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        context = parent.getContext();
        return new CategoryViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String imageUrl;

        Category category = categories.get(position);
        holder.categoryNameTextView.setText(category.getName());

        imageUrl = category.getImageUrl();

        Glide.with(context)
                .load(imageUrl)
                //.placeholder()
                .fitCenter()
                .into(holder.categoryImage);

        holder.editCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UpdateCategoryActivity.class);
                intent.putExtra("id", category.getCategoryId());
                intent.putExtra("name", category.getName());
                intent.putExtra("image", category.getImageUrl());
                context.startActivity(intent);
            }
        });

        holder.deleteCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categories.remove(category);
                notifyDataSetChanged();
                deleteCategory(category.getCategoryId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    private void deleteCategory(String categoryId) {
        firebaseManager.deleteCategory(categoryId, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(context.getApplicationContext(), "Couldn't delete category.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
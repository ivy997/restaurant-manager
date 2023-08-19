package com.example.restaurantmanager.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.restaurantmanager.R;
import com.example.restaurantmanager.models.Dish;

import java.util.List;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.DishViewHolder>{
    private Context context;
    private List<Dish> dishes;
    private DishAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(DishAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public DishAdapter(List<Dish> dishes) {
        this.dishes = dishes;
    }

    public static class DishViewHolder extends RecyclerView.ViewHolder {
        public TextView dishNameTextView;
        public TextView dishDescTextView;
        public TextView dishPriceTextView;
        public ImageView dishImage;
        public ImageView deleteImageView;
        public View foregroundView;

        public DishViewHolder(@NonNull View itemView, final DishAdapter.OnItemClickListener listener) {
            super(itemView);
            dishNameTextView = itemView.findViewById(R.id.dishNameTV);
            dishDescTextView = itemView.findViewById(R.id.dishDescTV);
            dishPriceTextView = itemView.findViewById(R.id.dishPriceTV);
            dishImage = itemView.findViewById(R.id.dishImg);
            deleteImageView = itemView.findViewById(R.id.delete_dish_image_view);
            foregroundView = itemView.findViewById(R.id.foreground_view_dish);

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
    public DishAdapter.DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dish, parent, false);
        context = parent.getContext();
        return new DishAdapter.DishViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull DishAdapter.DishViewHolder holder, int position) {
        String imageUrl;

        Dish dish = dishes.get(position);
        holder.dishNameTextView.setText(dish.getName());
        holder.dishDescTextView.setText(dish.getDescription());
        holder.dishPriceTextView.setText(dish.getPrice() + " лв.");
        imageUrl = dish.getImageUrl();

        Glide.with(context)
                .load(imageUrl)
                //.placeholder()
                .fitCenter()
                .into(holder.dishImage);

        // Set the visibility of the delete image to GONE by default
        holder.deleteImageView.setVisibility(View.GONE);

        // Set up swipe-to-delete behavior
        ItemTouchHelper.SimpleCallback swipeToDeleteCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {


            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int adapterPosition = viewHolder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    dishes.remove(adapterPosition);
                    notifyItemRemoved(adapterPosition);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(holder.itemView.getRootView().findViewById(R.id.dish_recycler_view));

        // Set the foreground view for item animation
        holder.foregroundView.setBackgroundColor(Color.WHITE);
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
        notifyDataSetChanged();
    }

    // Helper method to resize a drawable
    private Drawable resizeDrawable(Drawable drawable, int width, int height) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        return new BitmapDrawable(context.getResources(), resizedBitmap);
    }
}

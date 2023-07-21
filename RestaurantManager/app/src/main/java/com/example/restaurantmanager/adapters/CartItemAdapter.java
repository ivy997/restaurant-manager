package com.example.restaurantmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantmanager.R;
import com.example.restaurantmanager.models.CartItem;
import com.example.restaurantmanager.models.Dish;

import java.util.List;

import util.Callback;
import util.FirebaseManager;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartViewHolder>{
    private Context context;
    private List<CartItem> items;
    private FirebaseManager firebaseManager;
    private CartItemAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void OnCartItemClick(int position);
    }

    public void setOnItemClickListener(CartItemAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public CartItemAdapter(List<CartItem> items) {
        this.items = items;
        this.firebaseManager = new FirebaseManager();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView note;
        public TextView quantity;
        public CheckBox isPrepared;

        public CartViewHolder(@NonNull View itemView, final CartItemAdapter.OnItemClickListener listener) {
            super(itemView);

            title = itemView.findViewById(R.id.dishName);
            note = itemView.findViewById(R.id.noteTV);
            quantity = itemView.findViewById(R.id.itemCount);
            isPrepared = itemView.findViewById(R.id.isPrepared);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.OnCartItemClick(position);
                        }
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public CartItemAdapter.CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        context = parent.getContext();
        return new CartItemAdapter.CartViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemAdapter.CartViewHolder holder, int position) {
        CartItem item = items.get(position);
        String dishTitle = item.getDish().getName();
        //String noteTxt = item.getNote();
        int itemCount = item.getQuantity();

        holder.title.setText(dishTitle);
        holder.note.setText("Test");
        holder.quantity.setText(String.format("Qty: " + itemCount));
        holder.isPrepared.setChecked(item.isPrepared());

        holder.isPrepared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update cartItem
                item.setPrepared(holder.isPrepared.isChecked());
                updateCartItem(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setCartItems(List<CartItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    private void updateCartItem(CartItem item) {
        firebaseManager.updateCartItem(item, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }
}

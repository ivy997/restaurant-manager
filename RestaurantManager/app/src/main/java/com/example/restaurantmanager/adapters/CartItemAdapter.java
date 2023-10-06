package com.example.restaurantmanager.adapters;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantmanager.MainActivity;
import com.example.restaurantmanager.R;
import com.example.restaurantmanager.activities.OrderDetailsActivity;
import com.example.restaurantmanager.models.CartItem;
import com.example.restaurantmanager.models.Dish;
import com.example.restaurantmanager.models.Order;

import java.util.List;

import util.Callback;
import util.FirebaseManager;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartViewHolder>{
    private Context context;
    private List<CartItem> items;
    private FirebaseManager firebaseManager;
    private CartItemAdapter.OnItemClickListener listener;
    private OrderDetailsActivity orderDetailsActivity;
    private Order order;

    public interface OnItemClickListener {
        void OnCartItemClick(int position);
    }

    public void setOnItemClickListener(OrderDetailsActivity activity, CartItemAdapter.OnItemClickListener listener, Order order) {
        this.listener = listener;
        this.orderDetailsActivity = activity;
        this.order = order;
    }

    public CartItemAdapter(List<CartItem> items) {
        this.items = items;
        this.firebaseManager = new FirebaseManager();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView quantity;
        public CheckBox isPrepared;
        public Button delete;
        public Button edit;
        public EditText editItemCount;

        public CartViewHolder(@NonNull View itemView, final CartItemAdapter.OnItemClickListener listener) {
            super(itemView);

            title = itemView.findViewById(R.id.dishName);
            quantity = itemView.findViewById(R.id.itemCount);
            isPrepared = itemView.findViewById(R.id.isPrepared);
            delete = itemView.findViewById(R.id.deleteItem);
            edit = itemView.findViewById(R.id.editItem);
            editItemCount = itemView.findViewById(R.id.editItemCount);

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
        int itemCount = item.getQuantity();

        holder.title.setText(dishTitle);
        holder.quantity.setText(String.format("Qty: " + itemCount));
        holder.isPrepared.setChecked(item.isPrepared());

        holder.isPrepared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update cartItem
                item.setPrepared(holder.isPrepared.isChecked());
                items.set(holder.getAdapterPosition(), item);
                order.setItems(items);
                updateCartItemInOrder(order);
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Delete cartItem
                items.remove(item);
                order.setItems(items);
                updateCartItemInOrder(order);
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.quantity.setVisibility(View.GONE);
                holder.editItemCount.setVisibility(View.VISIBLE);
            }
        });

        holder.editItemCount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Handle the "Done" action here
                    // This code will be executed when the user presses the "Done" button on the keyboard
                    String newItemCount = holder.editItemCount.getText().toString().trim();
                    if (!newItemCount.isEmpty()) {
                        item.setQuantity(Integer.parseInt(newItemCount));
                        items.set(holder.getAdapterPosition(), item);
                        order.setItems(items);
                        float updatedBill = calculateTotalPrice();
                        order.setBillPrice(updatedBill);
                        updateCartItemInOrder(order);
                        hideKeyboard(holder.editItemCount);
                        return true; // Return true to consume the event
                    }
                }
                holder.quantity.setVisibility(View.VISIBLE);
                holder.editItemCount.setVisibility(View.GONE);
                hideKeyboard(holder.editItemCount);
                return false; // Return false if you want to propagate the event further
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

    private void updateCartItemInOrder(Order order) {
        firebaseManager.updateCartItemInOrder(order, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                orderDetailsActivity.recreate();
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void hideKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private float calculateTotalPrice() {
        float totalPrice = 0.0f;
        for (CartItem item : items) {
            float dishPrice = item.getDish().getPrice();
            int itemCount = item.getQuantity();
            totalPrice += dishPrice * itemCount;
        }
        return totalPrice;
    }
}

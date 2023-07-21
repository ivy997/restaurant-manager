package com.example.restaurantmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantmanager.R;
import com.example.restaurantmanager.models.Dish;
import com.example.restaurantmanager.models.Order;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder>{
    private Context context;
    private List<Order> orders;
    private OrderAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onOrderClick(int position);
    }

    public void setOnItemClickListener(OrderAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public OrderAdapter(List<Order> orders) {
        this.orders = orders;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        public TextView time;
        public TextView orderNumber;
        public TextView tableNumber;
        public TextView totalPrice;
        public TextView orderStatus;

        public OrderViewHolder(@NonNull View itemView, final OrderAdapter.OnItemClickListener listener) {
            super(itemView);

            time = itemView.findViewById(R.id.timeTV);
            orderNumber = itemView.findViewById(R.id.orderNumber);
            tableNumber = itemView.findViewById(R.id.tableNumber);
            totalPrice = itemView.findViewById(R.id.totalPriceTV);
            orderStatus = itemView.findViewById(R.id.orderStatusTV);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onOrderClick(position);
                        }
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public OrderAdapter.OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        context = parent.getContext();
        return new OrderAdapter.OrderViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        Date date = order.getOrderDateAndTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String formattedTime = sdf.format(date);

        holder.time.setText(formattedTime);
        holder.orderNumber.setText("Order #1");
        //holder.tableNumber.setText();
        holder.totalPrice.setText(String.valueOf(order.getBillPrice()));
        holder.orderStatus.setText(String.valueOf(order.getOrderStatus()));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }
}

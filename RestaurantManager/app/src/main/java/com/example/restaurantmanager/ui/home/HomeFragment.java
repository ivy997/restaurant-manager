package com.example.restaurantmanager.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantmanager.R;
import com.example.restaurantmanager.activities.OrderDetailsActivity;
import com.example.restaurantmanager.adapters.OrderAdapter;
import com.example.restaurantmanager.databinding.FragmentHomeBinding;
import com.example.restaurantmanager.enums.OrderStatus;
import com.example.restaurantmanager.models.Order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import util.Callback;
import util.FirebaseManager;

public class HomeFragment extends Fragment implements OrderAdapter.OnItemClickListener{
    private FragmentHomeBinding binding;
    private FirebaseManager firebaseManager;
    private OrderAdapter orderAdapter;
    private List<Order> orders;
    private RecyclerView orderRecyclerView;
    private TextView ordersCount;
    private TextView ordersIncome;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseManager = new FirebaseManager();
        orders = new ArrayList<>();
        orderRecyclerView = root.findViewById(R.id.ordersRecyclerView);
        ordersCount = root.findViewById(R.id.todaysOrdersTV);
        ordersIncome = root.findViewById(R.id.todaysProfitTV);

        orderAdapter = new OrderAdapter(orders);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        orderAdapter.setOnItemClickListener(this);
        orderRecyclerView.setAdapter(orderAdapter);

        getOrders();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onOrderClick(int position) {
        Order order = orders.get(position);
        Intent intent = new Intent(getActivity(), OrderDetailsActivity.class);
        intent.putExtra("order", (Serializable) order);
        startActivity(intent);
    }

    private void getOrders() {
        firebaseManager.getOrders(new Callback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> result) {
                orders = result;
                orderAdapter.setOrders(orders);
                getOrdersData();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getOrdersData() {
        int totalOrders = 0;
        float totalIncome = 0.0f;

        for (Order order : this.orders) {
            totalOrders++;
            if (order.getOrderStatus() == OrderStatus.PAID){
                totalIncome += order.getBillPrice();
            }
        }

        ordersCount.setText(String.valueOf("Today's orders: " + totalOrders));
        ordersIncome.setText(String.valueOf("Today's profit: " + totalIncome + " лв."));
    }
}
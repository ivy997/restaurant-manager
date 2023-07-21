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
import com.example.restaurantmanager.models.Order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import util.Callback;
import util.FirebaseManager;

public class HomeFragment extends Fragment implements OrderAdapter.OnItemClickListener{

    private FragmentHomeBinding binding;
    private FirebaseManager firebaseManager;
    private RecyclerView orderRecyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orders;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        firebaseManager = new FirebaseManager();
        orders = new ArrayList<>();
        orderRecyclerView = root.findViewById(R.id.ordersRecyclerView);

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
        Toast.makeText(getContext(), "Clicked", Toast.LENGTH_SHORT).show();
    }

    private void getOrders() {
        firebaseManager.getOrders(new Callback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> result) {
                orders = result;
                orderAdapter.setOrders(orders);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
package com.example.restaurantmanager.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.restaurantmanager.MainActivity;
import com.example.restaurantmanager.R;
import com.example.restaurantmanager.adapters.CartItemAdapter;
import com.example.restaurantmanager.adapters.OrderAdapter;
import com.example.restaurantmanager.enums.OrderStatus;
import com.example.restaurantmanager.models.CartItem;
import com.example.restaurantmanager.models.Order;
import com.example.restaurantmanager.ui.home.HomeFragment;
import com.google.android.play.integrity.internal.x;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import util.Callback;
import util.FirebaseManager;

public class OrderDetailsActivity extends AppCompatActivity implements CartItemAdapter.OnItemClickListener{

    private FirebaseManager firebaseManager;
    private CartItemAdapter itemAdapter;
    private List<CartItem> items;
    private RecyclerView itemsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        firebaseManager = new FirebaseManager();

        // Enable the up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Order order = (Order) getIntent().getSerializableExtra("order");
        items = order.getItems();

        firebaseManager = new FirebaseManager();

        itemsRecyclerView = findViewById(R.id.cartItemRecyclerView);
        Spinner spinner = findViewById(R.id.spinner);
        Button update = findViewById(R.id.updateBtn);
        //Button delete = findViewById(R.id.deleteBtn);

        OrderStatus[] options = OrderStatus.values();

        itemAdapter = new CartItemAdapter(items);
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        itemAdapter.setOnItemClickListener(this, this, order);
        itemsRecyclerView.setAdapter(itemAdapter);

        ArrayAdapter<OrderStatus> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options);
        spinner.setAdapter(adapter);

        int statusPosition = getCurrentOrderStatusIndex(options, order);
        spinner.setSelection(statusPosition);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle the selected item change
                OrderStatus selectedItem = (OrderStatus) parent.getItemAtPosition(position);
                order.setOrderStatus(selectedItem);
                //updateOrder(order);
                // Perform actions based on the selected item (Update order)
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case where no item is selected
                // Do nothing
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOrder(order);
            }
        });
    }

    @Override
    public void OnCartItemClick(int position) {

    }

    private void updateOrder(Order order) {
        firebaseManager.updateOrder(order, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private int getCurrentOrderStatusIndex(OrderStatus[] options, Order currOrder) {
        OrderStatus itemToFind = currOrder.getOrderStatus();

        int position = 0; // Initialize with -1, which will be used if the item is not found

        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(itemToFind)) {
                position = i;
            }
        }

        return position;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
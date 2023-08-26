package com.example.restaurantmanager.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.restaurantmanager.R;
import com.example.restaurantmanager.adapters.CategoryAdapter;
import com.example.restaurantmanager.adapters.DishAdapter;
import com.example.restaurantmanager.models.Category;
import com.example.restaurantmanager.models.Dish;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import util.Callback;
import util.FirebaseManager;

public class ListDishesActivity extends AppCompatActivity implements DishAdapter.OnItemClickListener{

    private FirebaseManager firebaseManager;
    private RecyclerView dishRecyclerView;
    private List<Dish> dishes;
    private DishAdapter dishAdapter;
    private ColorDrawable background;
    private FloatingActionButton dishFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_dishes);

        // Enable the up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String categoryId = "";
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String className = extras.getString("className");
            if (className.equals(".activities.ListCategoriesActivity")) {
                // Perform actions specific to List Categories Activity
                Toast.makeText(getApplicationContext(), "Coming from List Categories Activity", Toast.LENGTH_SHORT).show();
                categoryId = extras.getString("categoryId");
            }
        } else {
            // Perform actions specific to Dashboard Fragment
            Toast.makeText(getApplicationContext(), "Coming from Dashboard fragment", Toast.LENGTH_SHORT).show();
        }

        // Initialize the RecyclerView and ArrayList
        dishRecyclerView = findViewById(R.id.dish_recycler_view);
        dishes = new ArrayList<>();
        firebaseManager = new FirebaseManager();
        dishFAB = findViewById(R.id.dishFAB);

        if (categoryId == "") {
            getDishes();
        } else {
            getDishesByCategory(categoryId);
        }

        // Create the CategoryAdapter and set it to the RecyclerView
        dishRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dishAdapter = new DishAdapter(dishes);
        dishAdapter.setOnItemClickListener(this);
        dishRecyclerView.setAdapter(dishAdapter);

        DeleteDish();
        onFABClicked(categoryId);
    }

    private void onFABClicked(String categoryId) {
        dishFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListDishesActivity.this, AddDishActivity.class);
                intent.putExtra("categoryId", categoryId);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onItemClick(int position) {
    }

    private void DeleteDish() {
        background = new ColorDrawable(Color.RED);

        ItemTouchHelper.SimpleCallback swipeToDeleteCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    int position = viewHolder.getAdapterPosition();
                    Dish deletedDish = dishes.get(position);
                    deleteDish(deletedDish.getDishId());
                    dishes.remove(position);
                    dishAdapter.notifyItemRemoved(position);
                    Toast.makeText(ListDishesActivity.this, "Deleted: " + deletedDish.getName(), Toast.LENGTH_SHORT).show();
                } else if (direction == ItemTouchHelper.RIGHT) {
                    int position = viewHolder.getAdapterPosition();
                    Dish currentDish = dishes.get(position);
                    Intent intent = new Intent(ListDishesActivity.this, UpdateDishActivity.class);
                    intent.putExtra("id", currentDish.getDishId());
                    intent.putExtra("name", currentDish.getName());
                    intent.putExtra("description", currentDish.getDescription());
                    intent.putExtra("price", currentDish.getPrice());
                    intent.putExtra("image", currentDish.getImageUrl());
                    startActivity(intent);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(dishRecyclerView);
    }

    private void getDishes() {
        firebaseManager.getDishes(new Callback<List<Dish>>() {
            @Override
            public void onSuccess(List<Dish> result) {
                dishes = result;
                dishAdapter.setDishes(dishes);
            }

            @Override
            public void onError(String errorMessage) {
                // Handle error
            }
        });
    }

    private void getDishesByCategory(String categoryId) {
        firebaseManager.getDishesByCategory(categoryId, new Callback<List<Dish>>() {
            @Override
            public void onSuccess(List<Dish> result) {
                dishes = result;
                dishAdapter.setDishes(dishes);
            }

            @Override
            public void onError(String errorMessage) {
                // Handle error
            }
        });
    }

    private void deleteDish(String dishId) {
        firebaseManager.deleteDish(dishId, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getApplicationContext(), "Couldn't delete category.", Toast.LENGTH_SHORT).show();
            }
        });
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
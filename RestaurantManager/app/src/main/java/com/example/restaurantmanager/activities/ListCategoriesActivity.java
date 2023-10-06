package com.example.restaurantmanager.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.restaurantmanager.MainActivity;
import com.example.restaurantmanager.R;
import com.example.restaurantmanager.adapters.CategoryAdapter;
import com.example.restaurantmanager.models.Category;
import com.example.restaurantmanager.ui.dashboard.DashboardFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import util.Callback;
import util.FirebaseManager;

public class ListCategoriesActivity extends AppCompatActivity implements CategoryAdapter.OnItemClickListener {
    private FirebaseManager firebaseManager;
    private RecyclerView categoryRecyclerView;
    private List<Category> categories;
    private CategoryAdapter categoryAdapter;
    private FloatingActionButton categoryFAB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_categories);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Categories");

        categoryRecyclerView = findViewById(R.id.category_recycler_view);
        categories = new ArrayList<>();
        firebaseManager = new FirebaseManager();
        categoryFAB = findViewById(R.id.categoryFAB);

        getCategoriesFromFirestore();

        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(categories);
        categoryAdapter.setOnItemClickListener(this);
        categoryRecyclerView.setAdapter(categoryAdapter);

        onFABClicked();
    }

    @Override
    public void onItemClick(int position) {
        Category category = categories.get(position);
        Intent intent = new Intent(ListCategoriesActivity.this, ListDishesActivity.class);
        intent.putExtra("categoryId", category.getCategoryId());
        intent.putExtra("className", getComponentName().getShortClassName());
        startActivity(intent);
    }

    private void onFABClicked() {
        categoryFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListCategoriesActivity.this, AddCategoryActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getCategoriesFromFirestore() {
        firebaseManager.getCategories(new Callback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                categories = result;
                categoryAdapter.setCategories(categories);
            }
            @Override
            public void onError(String errorMessage) {
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
    @Override
    protected void onRestart() {
        super.onRestart();
        // This code will be triggered when the activity is being restarted
        // due to navigation from another activity (e.g., coming back from a back press)
        recreate();
    }
}
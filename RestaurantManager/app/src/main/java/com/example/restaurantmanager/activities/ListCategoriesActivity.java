package com.example.restaurantmanager.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.restaurantmanager.R;
import com.example.restaurantmanager.adapters.CategoryAdapter;
import com.example.restaurantmanager.models.Category;
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
    private ColorDrawable background;

    private FloatingActionButton categoryFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_categories);

        // Initialize the RecyclerView and ArrayList
        categoryRecyclerView = findViewById(R.id.category_recycler_view);
        categories = new ArrayList<>();
        firebaseManager = new FirebaseManager();
        categoryFAB = findViewById(R.id.categoryFAB);

        getCategoriesFromFirestore();

        // Create the CategoryAdapter and set it to the RecyclerView
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(categories);
        categoryAdapter.setOnItemClickListener(this);
        categoryRecyclerView.setAdapter(categoryAdapter);

        DeleteCategory();
        onFABClicked();
    }

    @Override
    public void onItemClick(int position) {
        Category category = categories.get(position);
        Intent intent = new Intent(ListCategoriesActivity.this, ListDishesActivity.class);
        intent.putExtra("categoryId", category.getCategoryId());
        intent.putExtra("className", getComponentName().getShortClassName());
        startActivity(intent);
        //Toast.makeText(this, "Clicked: " + category.getName(), Toast.LENGTH_SHORT).show();
    }

    private void DeleteCategory() {
        //deleteIcon = ContextCompat.getDrawable(this, R.drawable.delete);
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
                    Category deletedCategory = categories.get(position);
                    deleteCategory(deletedCategory.getCategoryId());
                    categories.remove(position);
                    categoryAdapter.notifyItemRemoved(position);
                    Toast.makeText(ListCategoriesActivity.this, "Deleted: " + deletedCategory.getName(), Toast.LENGTH_SHORT).show();
                } else if (direction == ItemTouchHelper.RIGHT) {
                    int position = viewHolder.getAdapterPosition();
                    Category currentCategory = categories.get(position);
                    Intent intent = new Intent(ListCategoriesActivity.this, UpdateCategoryActivity.class);

                    intent.putExtra("id", currentCategory.getCategoryId());
                    intent.putExtra("name", currentCategory.getName());
                    intent.putExtra("image", currentCategory.getImageUrl());
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
        itemTouchHelper.attachToRecyclerView(categoryRecyclerView);
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
                // Handle error
            }
        });
    }

    private void deleteCategory(String categoryId) {
        firebaseManager.deleteCategory(categoryId, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                //Toast.makeText(getApplicationContext(), "Category deleted successfully.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getApplicationContext(), "Couldn't delete category.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
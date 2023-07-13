package util;

import androidx.annotation.NonNull;

import com.example.restaurantmanager.models.Category;
import com.example.restaurantmanager.models.Dish;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseManager {
    private FirebaseFirestore db;
    private CollectionReference categoriesRef;
    private CollectionReference dishesRef;
    private String userId;

    public FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        categoriesRef = db.collection("Categories");
        dishesRef = db.collection("Dishes");
        getUser();
    }

    private void getUser() {
        RestaurantUser restaurantUser = RestaurantUser.getInstance();
        if (restaurantUser != null) {
            userId = restaurantUser.getUserId();
        }
    }

    public void getCategory(String categoryId, Callback<Category> callback) {
        categoriesRef.document(categoryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Category category = documentSnapshot.toObject(Category.class);
                        callback.onSuccess(category);
                    } else {
                        callback.onError("Category not found");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void addCategory(Category category, Callback<String> callback) {
        categoriesRef.add(category)
                .addOnSuccessListener(documentReference -> {
                    // Category added successfully
                    String categoryId = documentReference.getId();
                    callback.onSuccess(categoryId);
                })
                .addOnFailureListener(e -> {
                    // Error adding category
                    callback.onError(e.getMessage());
                });
    }

    public void deleteCategory(String categoryId, Callback<Void> callback) {
        categoriesRef.document(categoryId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    public void updateCategory(Category category, Callback<Void> callback) {
        DocumentReference documentRef = categoriesRef.document(category.getCategoryId());

        Map<String, Object> updates = new HashMap<>();
        updates.put("categoryId", category.getCategoryId());
        updates.put("name", category.getName());
        updates.put("imageUrl", category.getImageUrl());

        documentRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void getCategories(Callback<List<Category>> callback) {
        categoriesRef
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Category> categories = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Category category = document.toObject(Category.class);
                        categories.add(category);
                    }
                    callback.onSuccess(categories);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void updateDish(Dish dish, Callback<Void> callback) {
        DocumentReference documentRef = dishesRef.document(dish.getDishId());

        Map<String, Object> updates = new HashMap<>();
        updates.put("dishId", dish.getDishId());
        updates.put("name", dish.getName());
        updates.put("description", dish.getDescription());
        updates.put("price", dish.getPrice());
        updates.put("imageUrl", dish.getImageUrl());

        documentRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void getDishes(Callback<List<Dish>> callback) {
        dishesRef
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Dish> dishes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Dish dish = document.toObject(Dish.class);
                        dishes.add(dish);
                    }
                    callback.onSuccess(dishes);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void getDishesByCategory(String categoryId, Callback<List<Dish>> callback) {
        dishesRef.whereEqualTo("categoryId", categoryId).whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Dish> dishList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Dish dish = document.toObject(Dish.class);
                                dishList.add(dish);
                            }
                            callback.onSuccess(dishList);
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    public void getDish(String dishId, Callback<Dish> callback) {
        dishesRef.document(dishId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Dish dish = documentSnapshot.toObject(Dish.class);
                        callback.onSuccess(dish);
                    } else {
                        callback.onError("Dish not found");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void deleteDish(String dishId, Callback<Void> callback) {
        dishesRef.document(dishId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }
}
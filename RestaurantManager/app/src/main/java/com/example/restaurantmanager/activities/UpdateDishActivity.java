package com.example.restaurantmanager.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.restaurantmanager.R;
import com.example.restaurantmanager.models.Category;
import com.example.restaurantmanager.models.Dish;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import util.Callback;
import util.FirebaseManager;

public class UpdateDishActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> galleryLauncher;
    private Dish dishToUpdate;
    // Widgets
    private Button saveButton;
    private ProgressBar progressBar;
    private ImageView addPhotoButton;
    private EditText dishNameET;
    private EditText descriptionET;
    private EditText priceET;
    private ImageView image;

    // Connection to Firestore
    private FirebaseManager firebaseManager;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection("Dishes");
    private Uri imageUri;
    private String dishId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_dish);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseManager = new FirebaseManager();
        progressBar = findViewById(R.id.dishProgressBar);
        dishNameET = findViewById(R.id.dishNameET);
        descriptionET = findViewById(R.id.dishDescET);
        priceET = findViewById(R.id.priceET);
        image = findViewById(R.id.dishIV);
        saveButton = findViewById(R.id.saveDishBtn);
        addPhotoButton = findViewById(R.id.dishCameraButton);

        progressBar.setVisibility(View.INVISIBLE);

        // Get the category object passed from previous activity
        Bundle extras = getIntent().getExtras();
        dishId = extras.getString("id");
        String name = extras.getString("name");
        String description = extras.getString("description");
        float price = extras.getFloat("price");
        String imageUrl = extras.getString("image");

        // Set initial values in the views
        dishNameET.setText(name);
        descriptionET.setText(description);
        priceET.setText(String.valueOf(price));
        Glide.with(this).load(imageUrl).into(image);
        updateDishActivityForResult();

        getDishById(dishId);

        // Set click listener for the update button
        saveButton.setOnClickListener(v -> {
            // Check if the image has been changed
            if (imageUri != null) {
                uploadImage(imageUri);
            } else {
                updateDish();
            }
        });

        // Set click listener for the category image view
        addPhotoButton.setOnClickListener(v -> openGallery());
    }

    private void uploadImage(Uri imageUri) {
        // Create a unique filename for the image
        String fileName = "image_"+ Timestamp.now().getSeconds();

        // Create a StorageReference with the filename
        StorageReference imageRef = storageReference.child(fileName);

        // Upload the image to Firebase Storage
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL of the uploaded image
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                // Update the image URL of the dish object
                                String imageUrl = uri.toString();
                                dishToUpdate.setImageUrl(imageUrl);

                                // Update the dish in Firestore
                                updateDish();
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure to get the download URL
                                Toast.makeText(UpdateDishActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle failure to upload the image
                    Toast.makeText(UpdateDishActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateDish() {
        // Get the updated text fields
        String newName = dishNameET.getText().toString().trim();
        String newDesc = descriptionET.getText().toString().trim();
        String newPrice = priceET.getText().toString().trim();

        // Update the dish object
        dishToUpdate.setName(newName);
        dishToUpdate.setDescription(newDesc);
        dishToUpdate.setPrice(Float.parseFloat(newPrice));

        // Update the dish in Firestore
        firebaseManager.updateDish(dishToUpdate, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Handle successful update
                Toast.makeText(UpdateDishActivity.this, "Dish updated successfully", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String errorMessage) {
                // Handle error
                Toast.makeText(UpdateDishActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        startActivity(new Intent(UpdateDishActivity.this,
                ListDishesActivity.class));
        finish();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    public void updateDishActivityForResult() {

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result != null) {
                        Intent data = result.getData();
                        if (data != null){
                            imageUri = data.getData();    // Getting the actual image path
                            image.setImageURI(imageUri);  // Showing the image
                        } else {
                        }
                    }
                }
        );
    }

    private void getDishById(String dishId) {
        firebaseManager.getDish(dishId, new Callback<Dish>() {
            @Override
            public void onSuccess(Dish result) {
                dishToUpdate = result;
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }
}
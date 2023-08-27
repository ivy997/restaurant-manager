package com.example.restaurantmanager.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.restaurantmanager.MainActivity;
import com.example.restaurantmanager.R;
import com.example.restaurantmanager.adapters.CategoryAdapter;
import com.example.restaurantmanager.models.Category;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

import util.Callback;
import util.FirebaseManager;

public class UpdateCategoryActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> galleryLauncher;
    private Category categoryToUpdate;
    private String id;
    // Widgets
    private Button saveButton;
    private ProgressBar progressBar;
    private ImageView addPhotoButton;
    private EditText titleEditText;
    private ImageView image;

    // Connection to Firestore
    private FirebaseManager firebaseManager;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection("Categories");
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_category);

        // Enable the up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize FirebaseManager and StorageReference
        firebaseManager = new FirebaseManager();
        storageReference = FirebaseStorage.getInstance().getReference("categories_images");

        // Get references to views
        progressBar = findViewById(R.id.categoryUpdateProgressBar);
        titleEditText = findViewById(R.id.categoryUpdateNameET);
        image = findViewById(R.id.categoryUpdateIV);
        saveButton = findViewById(R.id.updateCategoryBtn);
        addPhotoButton = findViewById(R.id.categoryUpdateCameraButton );

        progressBar.setVisibility(View.INVISIBLE);

        // Get the category object passed from previous activity
        Bundle extras = getIntent().getExtras();
        id = extras.getString("id");
        String name = extras.getString("name");
        String imageUrl = extras.getString("image");

        // Set initial values in the views
        titleEditText.setText(name);
        Glide.with(this).load(imageUrl).into(image);
        updateCategoryActivityForResult();

        getCategoryById(id);

        // Set click listener for the update button
        saveButton.setOnClickListener(v -> {
            // Check if the image has been changed
            if (imageUri != null) {
                uploadImage(imageUri);
            } else {
                updateCategory();
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
                                // Update the image URL of the category object
                                String imageUrl = uri.toString();
                                categoryToUpdate.setImageUrl(imageUrl);

                                // Update the category in Firestore
                                updateCategory();
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure to get the download URL
                                Toast.makeText(UpdateCategoryActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle failure to upload the image
                    Toast.makeText(UpdateCategoryActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCategory() {
        // Get the updated name and image URL
        String newName = titleEditText.getText().toString().trim();

        // Update the category object
        categoryToUpdate.setName(newName);

        // Update the category in Firestore
        firebaseManager.updateCategory(categoryToUpdate, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                finish();
                onBackPressed();
            }

            @Override
            public void onError(String errorMessage) {
                // Handle error
                Toast.makeText(UpdateCategoryActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    public void updateCategoryActivityForResult() {

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result != null) {
                        // Handle the selected image
                        // Use the result Uri to process the selected image
                        Intent data = result.getData();
                        if (data != null){
                            imageUri = data.getData();    // Getting the actual image path
                            image.setImageURI(imageUri);  // Showing the image
                            //Glide.with(this).load(imageUri).into(image);
                        } else {
                            // The activity returned a non-successful result or was cancelled
                            // Handle the failure or cancellation case
                        }
                    }
                }
        );
    }

    private void getCategoryById(String categoryId) {
        firebaseManager.getCategory(categoryId, new Callback<Category>() {

            @Override
            public void onSuccess(Category result) {
                categoryToUpdate = result;
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
}
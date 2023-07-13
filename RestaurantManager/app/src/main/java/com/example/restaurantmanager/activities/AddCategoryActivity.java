package com.example.restaurantmanager.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.restaurantmanager.R;
import com.example.restaurantmanager.adapters.CategoryAdapter;
import com.example.restaurantmanager.models.Category;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import util.Callback;
import util.FirebaseManager;
import util.RestaurantUser;

public class AddCategoryActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> galleryLauncher;

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

    // User id and Username
    private String currentUserId;
    private String restaurantName;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseManager = new FirebaseManager();
        progressBar = findViewById(R.id.categoryProgressBar);
        titleEditText = findViewById(R.id.categoryNameET);
        image = findViewById(R.id.categoryIV);
        saveButton = findViewById(R.id.saveCategoryBtn);
        addPhotoButton = findViewById(R.id.categoryCameraButton );

        progressBar.setVisibility(View.INVISIBLE);

        if (RestaurantUser.getInstance() != null) {
            currentUserId = RestaurantUser.getInstance().getUserId();
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // IDK
                }
            }
        };

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveCategory();
            }
        });

        saveCategoryActivityForResult();

        addPhotoButton.setOnClickListener(view -> openGallery());
    }

    private void SaveCategory() {
        final String title = titleEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(title)
                && imageUri != null) {

            // the saving path of the images in Storage Firebase:
            final StorageReference filepath = storageReference
                    .child("categories_images")
                    .child("image_"+ Timestamp.now().getSeconds());

            // Uploading the image
            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();

                                    // Creating object of Category
                                    Category category = new Category(title, imageUrl, currentUserId);

                                    // Invoking Collection Reference
                                    collectionReference.add(category)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    String categoryId = documentReference.getId();
                                                    category.setCategoryId(categoryId);
                                                    updateCategory(category);
                                                    /*startActivity(new Intent(AddCategoryActivity.this,
                                                            ListCategoriesActivity.class));*/
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getApplicationContext(),
                                                            "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });

        } else{
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void saveCategoryActivityForResult() {

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
                        } else {
                            // The activity returned a non-successful result or was cancelled
                            // Handle the failure or cancellation case
                        }
                    }
                }
        );
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void updateCategory(Category category) {
        firebaseManager.updateCategory(category, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }
}
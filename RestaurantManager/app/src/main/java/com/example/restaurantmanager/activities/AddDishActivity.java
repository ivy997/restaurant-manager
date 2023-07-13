package com.example.restaurantmanager.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.example.restaurantmanager.R;
import com.example.restaurantmanager.models.Category;
import com.example.restaurantmanager.models.Dish;
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

public class AddDishActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> galleryLauncher;

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
    private String categoryId;

    // User id and username
    private String currentUserId;
    private String restaurantName;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dish);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseManager = new  FirebaseManager();
        progressBar = findViewById(R.id.dishProgressBar);
        dishNameET = findViewById(R.id.dishNameET);
        descriptionET = findViewById(R.id.dishDescET);
        priceET = findViewById(R.id.priceET);
        image = findViewById(R.id.dishIV);
        saveButton = findViewById(R.id.saveDishBtn);
        addPhotoButton = findViewById(R.id.dishCameraButton );

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

        Bundle extras = getIntent().getExtras();
        categoryId = extras.getString("categoryId");

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveDish();
            }
        });

        saveDishActivityForResult();

        addPhotoButton.setOnClickListener(view -> openGallery());
    }

    private void SaveDish() {
        final String name = dishNameET.getText().toString().trim();
        final String description = descriptionET.getText().toString().trim();
        final String price = priceET.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(name)
                && !TextUtils.isEmpty(description)
                && !TextUtils.isEmpty(price)
                && imageUri != null) {

            // the saving path of the images in Storage Firebase:
            final StorageReference filepath = storageReference
                    .child("dishes_images")
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
                                    float priceNum = Float.parseFloat(price);
                                    Dish dish = new Dish(name, description, imageUrl, priceNum, false, categoryId, currentUserId);

                                    // Invoking Collection Reference
                                    collectionReference.add(dish)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    String dishId = documentReference.getId();
                                                    dish.setDishId(dishId);
                                                    updateDish(dish);
                                                    startActivity(new Intent(AddDishActivity.this,
                                                            ListDishesActivity.class));
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

    public void saveDishActivityForResult() {

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

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void updateDish(Dish dish) {
        firebaseManager.updateDish(dish, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }
}
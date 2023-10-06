package com.example.restaurantmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.RestaurantUser;

public class SignUpActivity extends AppCompatActivity {
    EditText restaurantName;
    EditText email;
    EditText password;
    Button signupBtn;
    Button loginBtn;

    // Firebase Authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currUser;

    // Firebase Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference =  db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();
        signupBtn = findViewById(R.id.signupBtn);
        loginBtn = findViewById(R.id.loginFromSignUpBtn);
        restaurantName = findViewById(R.id.name);
        email = findViewById(R.id.etEmailSignup);
        password = findViewById(R.id.etPasswordSignup);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currUser = firebaseAuth.getCurrentUser();
            }
        };

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(email.getText().toString()) &&
                        !TextUtils.isEmpty(password.getText().toString()) &&
                        !TextUtils.isEmpty(restaurantName.getText().toString())) {
                    String email_str = email.getText().toString().trim();
                    String pass_str = password.getText().toString().trim();
                    String name = restaurantName.getText().toString().trim();

                    CreateUser(email_str, pass_str, name);
                } else {
                    Toast.makeText(SignUpActivity.this, "Please fill in the required fields.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void CreateUser(String email_str, String pass_str, String name) {
        firebaseAuth.createUserWithEmailAndPassword(email_str, pass_str)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currUser = firebaseAuth.getCurrentUser();
                            assert currUser != null;
                            final String currUserId = currUser.getUid();

                            Map<String, String> user = new HashMap<>();
                            user.put("userId", currUserId);
                            user.put("restaurantName", name);

                            collectionReference.add(user)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (Objects.requireNonNull(task.getResult()).exists()) {
                                                        String res_name = task.getResult().getString("restaurantName");
                                                        String emailAddress = task.getResult().getString("email");
                                                        // Getting user of Global Restaurant user
                                                        RestaurantUser restaurantUser = RestaurantUser.getInstance();
                                                        restaurantUser.setUserId(currUserId);
                                                        restaurantUser.setEmail(emailAddress);
                                                        restaurantUser.setRestaurantName(res_name);

                                                        // If the user is registered successfully, move to MainActivity
                                                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                                        intent.putExtra("restaurantName", res_name);
                                                        intent.putExtra("userId", currUserId);
                                                        startActivity(intent);
                                                    } else {
                                                        //
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Display Fail message
                                                    Toast.makeText(SignUpActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}
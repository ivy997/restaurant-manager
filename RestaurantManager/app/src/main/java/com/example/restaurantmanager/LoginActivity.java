package com.example.restaurantmanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import util.RestaurantUser;

public class LoginActivity extends AppCompatActivity {

    // Widgets
    private Button login;
    private Button register;
    private EditText email;
    private EditText password;

    // Firebase Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Firebase Authentication
    private FirebaseAuth firebaseAuth;
    private CollectionReference collectionReference = db.collection("Users");
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = findViewById(R.id.loginBtn);
        register = findViewById(R.id.registerBtn);
        email = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);

        firebaseAuth = FirebaseAuth.getInstance();

        Bundle extras = getIntent().getExtras();
        String logout = "";
        if (extras != null) {
            logout = extras.getString("logout");
        }

        if (logout.isEmpty()) {
            autoLogin();
        } else {
            SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove("rememberMe");
            editor.remove("email");
            editor.remove("userId");
            editor.apply();
        }

        CheckBox rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        rememberMeCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                if (rememberMeCheckbox.isChecked()) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("rememberMe", true);
                    editor.apply();
                } else {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove("rememberMe");
                    editor.apply();
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginEmailPasswordUser(
                        email.getText().toString().trim(),
                        password.getText().toString().trim()
                );
            }
        });
    }

    private void LoginEmailPasswordUser(String email, String password) {
        // Checking for empty text
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            assert user != null;
                            final String currentUserId = user.getUid();
                            collectionReference.whereEqualTo("userId", currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            /*if (error != null) {

                                            }*/
                                            assert value != null;
                                            if (!value.isEmpty()) {
                                                for (QueryDocumentSnapshot snapshot : value) {
                                                    String restaurantName = snapshot.getString("restaurantName");
                                                    RestaurantUser restaurantUser = RestaurantUser.getInstance();
                                                    restaurantUser.setEmail(email);
                                                    restaurantUser.setUserId(currentUserId);
                                                    restaurantUser.setRestaurantName(restaurantName);

                                                    if (getRememberMe()) {
                                                        onLoginSuccess(user.getUid(), email);
                                                    }

                                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                }
                                            }
                                        }
                                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, "Something went wrong" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(LoginActivity.this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
        }
    }

    private void onLoginSuccess(String token, String email) {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userToken", token);
        editor.putString("email", email);
        editor.apply();
    }

    // When app is opened (e.g., in onCreate of your MainActivity)
    private void autoLogin() {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = preferences.getString("userToken", null);
        String email = preferences.getString("email", null);
        if (token != null && email != null) {
            RestaurantUser restaurantUser = RestaurantUser.getInstance();
            restaurantUser.setEmail(email);
            restaurantUser.setUserId(token);
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
    }

    private boolean getRememberMe() {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return preferences.getBoolean("rememberMe", false);
    }
}
package com.example.restaurantmanager.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.restaurantmanager.R;
import com.example.restaurantmanager.ui.notifications.NotificationsFragment;

import util.Callback;
import util.FirebaseManager;

public class ChangeEmailActivity extends AppCompatActivity {
    private FirebaseManager firebaseManager;
    private EditText newEmail;
    private EditText pass;
    private Button updateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        firebaseManager = new FirebaseManager();
        newEmail = findViewById(R.id.newEmailET);
        pass = findViewById(R.id.passET);
        updateBtn = findViewById(R.id.updateBtn);

        // Enable the up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = pass.getText().toString().trim();
                String email = newEmail.getText().toString().trim();
                if (!email.isEmpty() && !password.isEmpty()) {
                    changeEmail(password, email);
                }
            }
        });
    }

    private void changeEmail(String currentPassword, String newEmail) {
        firebaseManager.changeEmail(currentPassword, newEmail, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(ChangeEmailActivity.this, "Email changed successfully.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ChangeEmailActivity.this, NotificationsFragment.class);
                startActivity(intent);
            }

            @Override
            public void onError(String errorMessage) {
                // Handle error
                Toast.makeText(ChangeEmailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ChangeEmailActivity.this, NotificationsFragment.class);
                startActivity(intent);
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
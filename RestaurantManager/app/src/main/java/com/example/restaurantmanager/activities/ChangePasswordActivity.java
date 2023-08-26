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

public class ChangePasswordActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;
    private EditText oldPass;
    private EditText newPass;
    private EditText confirm;
    private Button updateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        firebaseManager = new FirebaseManager();
        oldPass = findViewById(R.id.oldPassET);
        newPass = findViewById(R.id.newPassET);
        confirm = findViewById(R.id.confirmPassET);
        updateBtn = findViewById(R.id.confirmBtn);

        // Enable the up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = oldPass.getText().toString().trim();
                String newPassword = newPass.getText().toString().trim();
                String confirmPassword = confirm.getText().toString().trim();

                if (!oldPassword.isEmpty() && !newPassword.isEmpty() && !confirmPassword.isEmpty()) {
                    if (newPassword.equals(confirmPassword)) {
                        changePassword(oldPassword, newPassword);
                    } else {
                        Toast.makeText(getApplicationContext(), "Passwords don't match", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void changePassword(String currentPassword, String newPassword) {
        firebaseManager.changePassword(currentPassword, newPassword, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(ChangePasswordActivity.this, "Password changed successfully.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ChangePasswordActivity.this, NotificationsFragment.class);
                startActivity(intent);
            }

            @Override
            public void onError(String errorMessage) {
                // Handle error
                Toast.makeText(ChangePasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ChangePasswordActivity.this, NotificationsFragment.class);
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
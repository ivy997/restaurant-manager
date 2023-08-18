package com.example.restaurantmanager.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.restaurantmanager.LoginActivity;
import com.example.restaurantmanager.R;
import com.example.restaurantmanager.activities.ChangeEmailActivity;
import com.example.restaurantmanager.activities.ChangePasswordActivity;
import com.example.restaurantmanager.activities.ListCategoriesActivity;
import com.example.restaurantmanager.databinding.FragmentNotificationsBinding;
import com.google.firebase.auth.FirebaseAuth;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private FirebaseAuth firebaseAuth;
    //private LinearLayout profile;
    private LinearLayout email;
    private LinearLayout password;
    private LinearLayout privacy;
    private LinearLayout about;
    private Button logoutBtn;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNotifications;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        logoutBtn = root.findViewById(R.id.logoutBtn);
        email = root.findViewById(R.id.editEmailLayout);
        password = root.findViewById(R.id.editPassLayout);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChangeEmailActivity.class);
                startActivity(intent);
            }
        });

        password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void logout() {
        // Sign out the current user
        firebaseAuth.signOut();

        // Redirect the user to the login screen (Assuming LoginActivity is the login screen)
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.putExtra("logout", "logout");
        startActivity(intent);

        // Finish the current activity to prevent the user from going back to it after logout
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

}
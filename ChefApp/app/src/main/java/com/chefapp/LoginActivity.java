package com.chefapp;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.chefapp.register.ForgotPasswordFragment;
import com.chefapp.register.LoginFragment;
import com.chefapp.register.RegisterFragment;

public class LoginActivity extends AppCompatActivity implements
        LoginFragment.OnRegisterButtonClickListener,
        LoginFragment.OnForgotPasswordClickListener {

    private OnBackPressedCallback onBackPressedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Display the login fragment by default
        displayFragment(new LoginFragment());

        // Set up back button handling
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fragmentManager = getSupportFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                } else {
                    // If there are no fragments in the back stack, finish the activity
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);


    }

    // Method to display a fragment in the container
    private void displayFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null); // Add to back stack
        fragmentTransaction.commit();
    }

    // Method to handle navigation to Register Fragment from Login Fragment
    @Override
    public void onRegisterButtonClicked() {
        displayFragment(new RegisterFragment());
    }

    // Method to handle navigation to Forgot Password Fragment from Login Fragment
    @Override
    public void onForgotPasswordClicked() {
        displayFragment(new ForgotPasswordFragment());
    }

}
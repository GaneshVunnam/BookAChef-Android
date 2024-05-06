package com.chefapp.register;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.chefapp.LoginActivity;
import com.chefapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private TextInputEditText editTextUsername, editTextEmail, editTextMobile, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TAG = "RegisterFragment";

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        editTextUsername = view.findViewById(R.id.usernameEditText);
        editTextEmail = view.findViewById(R.id.emailEditText);
        editTextMobile = view.findViewById(R.id.mobileEditText);
        editTextPassword = view.findViewById(R.id.passwordEditText);
        editTextConfirmPassword = view.findViewById(R.id.confirmPasswordEditText);
        buttonRegister = view.findViewById(R.id.registerButton);

        // Set click listener for register button
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        return view;
    }

    private void registerUser() {
        // Retrieve user input
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String mobile = editTextMobile.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Perform input validation
        if (username.isEmpty() || email.isEmpty() || mobile.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            // Show error message directly in text fields using setError()
            editTextUsername.setError("Username is required");
            editTextEmail.setError("Email is required");
            editTextMobile.setError("Mobile number is required");
            editTextPassword.setError("Password is required");
            editTextConfirmPassword.setError("Confirm Password is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter a valid email address");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.PHONE.matcher(mobile).matches()) {
            editTextMobile.setError("Enter a valid mobile number");
            editTextMobile.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password should be at least 6 characters");
            editTextPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            editTextConfirmPassword.requestFocus();
            return;
        }

        // If input is valid, create a new user account using Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Save user details to Firestore
                            saveUserToFirestore(username, email, mobile);

                            // Show a Snackbar message indicating successful account creation
                            Snackbar.make(requireView(), "Account created successfully. Please log in.", Snackbar.LENGTH_SHORT).show();

                        } else {
                            // If sign in fails, display a message to the user.
                            Snackbar.make(requireView(), "Registration failed. Please try again.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String username, String email, String mobile) {
        // Create a new user object
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("mobile", mobile);

        // Add a new document with a generated ID to the "customers" collection
        db.collection("chefs")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        Snackbar.make(requireView(), "Registration Success. You can login now.", Snackbar.LENGTH_SHORT).show();

                        // Registration successful, navigate to the next screen
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                                requireActivity().finish();
                            }
                        }, 1000); // 2000 milliseconds = 2 seconds

                        // You can add your navigation logic here
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        Snackbar.make(requireView(), "Registration failed. Please try again.", Snackbar.LENGTH_SHORT).show();
                    }
                });

    }
}


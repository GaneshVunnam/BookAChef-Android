package com.bookachef;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, mobileEditText, usernameEditText;
    private Button saveButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        mobileEditText = findViewById(R.id.mobileEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        saveButton = findViewById(R.id.saveButton);

        // Get user data from Intent extras
        Intent intent = getIntent();
        if (intent != null) {
            String email = intent.getStringExtra("email");
            String mobile = intent.getStringExtra("mobile");
            String username = intent.getStringExtra("username");

            // Set retrieved data to EditText fields
            emailEditText.setText(email);
            mobileEditText.setText(mobile);
            usernameEditText.setText(username);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the edited values from the TextInputEditText fields
                String newEmail = emailEditText.getText().toString().trim();
                String newMobile = mobileEditText.getText().toString().trim();
                String newUsername = usernameEditText.getText().toString().trim();

                // Update the user data in Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userEmail = currentUser.getEmail();

                    // Update the user data
                    db.collection("customers")
                            .whereEqualTo("email", userEmail)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    // Assuming there's only one document for the user
                                    String userId = queryDocumentSnapshots.getDocuments().get(0).getId();
                                    String userDocumentPath = "customers/" + userId;

                                    // Perform the update
                                    db.document(userDocumentPath)
                                            .update("email", newEmail, "mobile", newMobile, "username", newUsername)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Data update successful
                                                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                                    // Optionally, finish the activity or navigate back to the profile fragment
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Handle failure
                                                    Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(EditProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure
                                    Toast.makeText(EditProfileActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

    }
}

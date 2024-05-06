package com.bookachef.home;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bookachef.EditProfileActivity;
import com.bookachef.LoginActivity;
import com.bookachef.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private CircleImageView profileImageView;
    private Button editProfileButton, logoutButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView usernameTextView;
    private Uri imageUri; // To store the selected image URI

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        profileImageView = view.findViewById(R.id.profileImageView);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        usernameTextView = view.findViewById(R.id.username);

        // Load username from Firestore
        loadUserData();

        // Handle profile image click
        profileImageView.setOnClickListener(v -> {
            openFileChooser();
        });

        // Handle edit profile button click
        editProfileButton.setOnClickListener(v -> {
            // Fetch user data from Firestore and open edit profile activity
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userEmail = currentUser.getEmail();
                db.collection("customers")
                        .whereEqualTo("email", userEmail)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    // Retrieve user data
                                    String email = document.getString("email");
                                    String mobile = document.getString("mobile");
                                    String username = document.getString("username");

                                    // Pass user data to the edit profile activity
                                    Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                                    intent.putExtra("email", email);
                                    intent.putExtra("mobile", mobile);
                                    intent.putExtra("username", username);
                                    startActivity(intent);
                                    return; // Exit loop after first match
                                }
                                // If no matching document is found
                                Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Failed to fetch user data: " + task.getException(), Toast.LENGTH_SHORT).show();
                                Log.e("ProfileFragment", "Error fetching user data", task.getException());
                            }
                        });
            }
        });

        // Handle logout button click
        logoutButton.setOnClickListener(v -> {
            // Sign out the user
            FirebaseAuth.getInstance().signOut();
            // Redirect to LoginActivity
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish(); // Optional: close the current activity
        });

        return view;
    }

    // Open file chooser to select an image from gallery
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Handle the result of selecting an image
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            // Load selected image into profileImageView
            Picasso.get().load(imageUri).into(profileImageView);
            // Upload the image to Firebase Storage
            uploadImageToFirebase();
        }
    }

    // Upload image to Firebase Storage
    private void uploadImageToFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && imageUri != null) {
            String userEmail = currentUser.getEmail();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_images/" + userEmail);
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully, get download URL
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Save download URL to Firestore under the user's document
                            db.collection("customers")
                                    .whereEqualTo("email", userEmail)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            for (DocumentSnapshot document : task.getResult()) {
                                                String documentId = document.getId();
                                                db.collection("customers").document(documentId)
                                                        .update("profileImageUrl", uri.toString())
                                                        .addOnSuccessListener(aVoid -> {

                                                            Toast.makeText(requireContext(), "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();
                                                        })
                                                        .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to upload profile image", Toast.LENGTH_SHORT).show());
                                            }
                                        } else {
                                            Toast.makeText(requireContext(), "Failed to fetch user document: " + task.getException(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to upload profile image", Toast.LENGTH_SHORT).show());
        }
    }


   // Load username and profile image from Firestore
   private void loadUserData() {
       FirebaseUser currentUser = mAuth.getCurrentUser();
       if (currentUser != null) {
           String userEmail = currentUser.getEmail();
           db.collection("customers")
                   .whereEqualTo("email", userEmail)
                   .get()
                   .addOnCompleteListener(task -> {
                       if (task.isSuccessful()) {
                           for (DocumentSnapshot document : task.getResult()) {
                               String username = document.getString("username");
                               // Set username to the TextView
                               usernameTextView.setText(username);

                               // Check if profile image URL exists
                               if (document.contains("profileImageUrl")) {
                                   String profileImageUrl = document.getString("profileImageUrl");
                                   // Load profile image using Picasso
                                   Picasso.get().load(profileImageUrl).into(profileImageView);
                               } else {
                                   // Profile image URL doesn't exist
                                   Toast.makeText(requireContext(), "Profile image doesn't exist. Please add one.", Toast.LENGTH_SHORT).show();
                               }
                               return; // Exit loop after first match
                           }
                           // If no matching document is found
                           Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show();
                       } else {
                           Toast.makeText(requireContext(), "Failed to fetch user data: " + task.getException(), Toast.LENGTH_SHORT).show();
                           Log.e("ProfileFragment", "Error fetching user data", task.getException());
                       }
                   });
       }
   }


    @Override
    public void onResume() {
        super.onResume();
        // Load the profile image again when the fragment is resumed
        loadProfileImage();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the imageUri to restore it later
        outState.putParcelable("imageUri", imageUri);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // Restore the imageUri if savedInstanceState is not null
        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable("imageUri");
            loadProfileImage();
        }
    }

    private void loadProfileImage() {
        // Load selected image into profileImageView if imageUri is not null
        if (imageUri != null) {
            Picasso.get().load(imageUri).into(profileImageView);
        }
    }
}

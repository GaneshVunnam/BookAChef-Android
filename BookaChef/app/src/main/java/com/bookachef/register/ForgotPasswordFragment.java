package com.bookachef.register;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bookachef.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgotPasswordFragment extends Fragment {

    private TextInputLayout emailInputLayout;
    private TextInputEditText emailEditText;

    private FirebaseAuth mAuth;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailInputLayout = view.findViewById(R.id.emailInputLayout);
        emailEditText = view.findViewById(R.id.emailEditText);
        Button resetPasswordButton = view.findViewById(R.id.resetPasswordButton);

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        return view;
    }

    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Email is required");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            showSnackbar("Password reset email sent. Please check your email inbox.");
                        } else {
                            showSnackbar("Failed to send password reset email. Please try again.");
                        }
                    }
                });
    }

    private void showSnackbar(String message) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show();
    }
}

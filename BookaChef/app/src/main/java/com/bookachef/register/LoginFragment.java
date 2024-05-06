package com.bookachef.register;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bookachef.MainActivity;
import com.bookachef.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginFragment extends Fragment {

    private OnForgotPasswordClickListener mForgotPasswordClickListener;
    private OnRegisterButtonClickListener mRegisterButtonClickListener;
    private FirebaseAuth mAuth;
    private TextInputEditText emailEditText, passwordEditText;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Check if the user is already authenticated
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            // User is already logged in, navigate to the main activity
//            startActivity(new Intent(getActivity(), MainActivity.class));
//            requireActivity().finish(); // Optional: close the current activity
//            return view; // Exit onCreateView early
//        }


        // Initialize views and set click listeners
        Button createAccountButton = view.findViewById(R.id.createAccountButton);
        TextView forgotPasswordTextView = view.findViewById(R.id.forgotPasswordTextView);
        TextInputLayout emailInputLayout = view.findViewById(R.id.emailInputLayout);
        TextInputLayout passwordInputLayout = view.findViewById(R.id.passwordInputLayout);
        emailEditText = emailInputLayout.findViewById(R.id.emailEditText);
        passwordEditText = passwordInputLayout.findViewById(R.id.passwordEditText);
        Button loginButton = view.findViewById(R.id.loginButton);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRegisterButtonClickListener != null) {
                    mRegisterButtonClickListener.onRegisterButtonClicked();
                }
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mForgotPasswordClickListener != null) {
                    mForgotPasswordClickListener.onForgotPasswordClicked();
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnRegisterButtonClickListener) {
            mRegisterButtonClickListener = (OnRegisterButtonClickListener) context;
        }
        if (context instanceof OnForgotPasswordClickListener) {
            mForgotPasswordClickListener = (OnForgotPasswordClickListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mForgotPasswordClickListener = null;
        mRegisterButtonClickListener = null;
    }

    // Interface for communication with the activity for creating an account
    public interface OnRegisterButtonClickListener {
        void onRegisterButtonClicked();
    }

    // Interface for communication with the activity for forgot password
    public interface OnForgotPasswordClickListener {
        void onForgotPasswordClicked();
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // User is logged in, navigate to the next screen
                                // Replace "NextActivity.class" with your desired activity
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                                requireActivity().finish(); // Optional: close the current activity
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(requireContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
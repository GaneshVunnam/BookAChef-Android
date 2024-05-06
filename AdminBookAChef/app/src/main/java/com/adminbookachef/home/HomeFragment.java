package com.adminbookachef.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adminbookachef.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private EditText editTextTitle, editTextDate, editTextTime, editTextVenue, editTextImageUrl, editTextTitleso, editTextDateso, editTextTimeso, editTextVenueso, editTextImageUrlso;
    private Button buttonAddEvent, buttonAddEventso;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextDate = view.findViewById(R.id.editTextDate);
        editTextTime = view.findViewById(R.id.editTextTime);
        editTextVenue = view.findViewById(R.id.editTextVenue);
        editTextImageUrl = view.findViewById(R.id.editTextImageUrl);
        buttonAddEvent = view.findViewById(R.id.buttonAddEvent);
        editTextTitleso = view.findViewById(R.id.editTextTitleso);
        editTextDateso = view.findViewById(R.id.editTextDateso);
        editTextTimeso = view.findViewById(R.id.editTextTimeso);
        editTextVenueso = view.findViewById(R.id.editTextVenueso);
        editTextImageUrlso = view.findViewById(R.id.editTextImageUrlso);
        buttonAddEventso = view.findViewById(R.id.buttonAddEventso);

        db = FirebaseFirestore.getInstance();

        buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEventToFirestore();
            }
        });

        buttonAddEventso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEventToFirestoreso();
            }
        });

        return view;
    }

    private void addEventToFirestore() {
        String title = editTextTitle.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String time = editTextTime.getText().toString().trim();
        String venue = editTextVenue.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();

        Map<String, Object> event = new HashMap<>();
        event.put("title", title);
        event.put("date", date);
        event.put("time", time);
        event.put("venue", venue);
        event.put("imageUrl", imageUrl);

        db.collection("upcoming_events")
                .document()
                .set(event)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Event added successfully!", Toast.LENGTH_SHORT).show();
                        // Clear input fields after successful addition
                        editTextTitle.setText("");
                        editTextDate.setText("");
                        editTextTime.setText("");
                        editTextVenue.setText("");
                        editTextImageUrl.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Failed to add event. Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addEventToFirestoreso() {
        String title = editTextTitleso.getText().toString().trim();
        String date = editTextDateso.getText().toString().trim();
        String time = editTextTimeso.getText().toString().trim();
        String venue = editTextVenueso.getText().toString().trim();
        String imageUrl = editTextImageUrlso.getText().toString().trim();

        Map<String, Object> event = new HashMap<>();
        event.put("title", title);
        event.put("date", date);
        event.put("time", time);
        event.put("venue", venue);
        event.put("imageUrl", imageUrl);

        db.collection("special_occasions")
                .document()
                .set(event)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Event added successfully!", Toast.LENGTH_SHORT).show();
                        // Clear input fields after successful addition
                        editTextTitleso.setText("");
                        editTextDateso.setText("");
                        editTextTimeso.setText("");
                        editTextVenueso.setText("");
                        editTextImageUrlso.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Failed to add event. Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

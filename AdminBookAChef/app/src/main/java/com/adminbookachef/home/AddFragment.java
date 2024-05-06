package com.adminbookachef.home;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adminbookachef.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddFragment extends Fragment {

    private EditText editTextCuisineName, editTextDishName, editTextCategoryName, editTextCategoryPrice, dish, catname, catprice;
    private Spinner spinnerCuisine, spinnerCuisine1, spinnerDish;
    private Button buttonAddCuisine, buttonAddDish, buttonAddCategory;
    private FirebaseFirestore db;
    private ArrayAdapter<String> cuisineAdapter;
    private List<String> cuisineList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        editTextCuisineName = view.findViewById(R.id.editTextCuisineName);
        editTextDishName = view.findViewById(R.id.editTextDishName);
        spinnerCuisine = view.findViewById(R.id.spinnerCuisine);
        buttonAddCuisine = view.findViewById(R.id.buttonAddCuisine);
        buttonAddDish = view.findViewById(R.id.buttonAddDish);
        dish = view.findViewById(R.id.dish);
        catname = view.findViewById(R.id.catname);
        catprice = view.findViewById(R.id.catprice);
        spinnerCuisine1 = view.findViewById(R.id.spinnerCuisine1);
        spinnerDish = view.findViewById(R.id.spinnerDish);
        buttonAddCategory = view.findViewById(R.id.buttonAddCategory);
        editTextCategoryName = view.findViewById(R.id.editTextCategoryName);
        editTextCategoryPrice = view.findViewById(R.id.editTextCategoryPrice);


        db = FirebaseFirestore.getInstance();

        // Initialize list and adapter for cuisines spinner
        cuisineList = new ArrayList<>();
        cuisineAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, cuisineList);
        spinnerCuisine.setAdapter(cuisineAdapter);
        spinnerCuisine1.setAdapter(cuisineAdapter);


        // Load cuisines
        loadCuisines();

        spinnerCuisine1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedCuisine = parentView.getItemAtPosition(position).toString();
                loadDishesForCuisine(selectedCuisine);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });


        // Button click listeners
        buttonAddCuisine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCuisine();
            }
        });

        buttonAddDish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDish();
            }
        });

        buttonAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addCategory();

            }
        });

        return view;
    }

    private void loadCuisines() {
        db.collection("cuisines")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Clear existing list of cuisines
                        cuisineList.clear();

                        // Loop through each document snapshot and add cuisine names to the list
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String cuisineName = documentSnapshot.getString("name");
                            cuisineList.add(cuisineName);
                        }

                        // Notify the adapter about the data change
                        cuisineAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Failed to load cuisines: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void addCuisine() {
        String cuisineName = editTextCuisineName.getText().toString().trim();
        if (cuisineName.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter cuisine name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new cuisine document
        Map<String, Object> cuisineData = new HashMap<>();
        cuisineData.put("name", cuisineName);

        // Add the cuisine document to Firestore
        db.collection("cuisines")
                .document(cuisineName)
                .set(cuisineData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Create a sub-collection named "dishes" inside the cuisine document
                        String dishName = dish.getText().toString().trim();
                        createDishesSubcollection(cuisineName, dishName);
                        Toast.makeText(getActivity(), "Data upload Success", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Failed to add cuisine: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createDishesSubcollection(String cuisineName, String dishName) {
        // Create a new sub-collection named "dishes" inside the cuisine document
        Map<String, Object> dishData = new HashMap<>();
        dishData.put("name", dishName); // Add the dish name field

        db.collection("cuisines")
                .document(cuisineName)
                .collection("dishes")
                .document(dishName) // Use the specified dishName as the document ID
                .set(dishData) // Set the dishData
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Create a sub-collection named "categories" inside the dish document
                        createCategoriesSubcollection(cuisineName, dishName);
                        Toast.makeText(getActivity(), "Dish added successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Failed to add dish: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createCategoriesSubcollection(String cuisineName, String dishName) {
        // Create a new sub-collection named "categories" inside the dish document
        db.collection("cuisines")
                .document(cuisineName)
                .collection("dishes")
                .document(dishName)
                .collection("categories")
                .document() // Generate a new document ID
                .set(getCategoryData()) // Set the category data
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Categories sub-collection created successfully", Toast.LENGTH_SHORT).show();
                        editTextCuisineName.setText("");
                        dish.setText("");
                        catname.setText("");
                        catprice.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Failed to create categories sub-collection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Map<String, Object> getCategoryData() {
        Map<String, Object> categoryData = new HashMap<>();
        categoryData.put("name", catname.getText().toString().trim());
        categoryData.put("price", Double.parseDouble(catprice.getText().toString().trim()));
        return categoryData;
    }




    private void addDish() {
        String dishName = editTextDishName.getText().toString().trim();
        String selectedCuisine = spinnerCuisine.getSelectedItem().toString();

        // Check if dish name is empty
        if (dishName.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter dish name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new dish document
        Map<String, Object> dishData = new HashMap<>();
        dishData.put("name", dishName);

        // Add the dish document to the "dishes" sub-collection within the selected cuisine
        db.collection("cuisines")
                .document(selectedCuisine)
                .collection("dishes")
                .document(dishName)
                .set(dishData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Dish added successfully", Toast.LENGTH_SHORT).show();
                        // Clear dish name field after successful addition
                        editTextDishName.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Failed to add dish: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }




    private void loadDishesForCuisine(String cuisineName) {
        db.collection("cuisines")
                .document(cuisineName)
                .collection("dishes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> dishList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String dishName = document.getString("name");
                                Log.d(TAG, "Dish Name: " + dishName); // Logging dishName
                                dishList.add(dishName);
                            }
                            Log.d(TAG, "Number of Dishes: " + dishList.size()); // Logging number of dishes
                            ArrayAdapter<String> dishAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, dishList);
                            dishAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerDish.setAdapter(dishAdapter);
                        } else {
                            Log.d(TAG, "Error getting dishes for cuisine " + cuisineName + ": ", task.getException());
                        }
                    }
                });
    }


    private void addCategory() {
        String cuisineName = spinnerCuisine1.getSelectedItem().toString();
        String dishName = spinnerDish.getSelectedItem().toString();
        String categoryName = editTextCategoryName.getText().toString().trim();
        String categoryPriceStr = editTextCategoryPrice.getText().toString().trim();

        // Check if category name and price are empty
        if (categoryName.isEmpty() || categoryPriceStr.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter category name and price", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert category price to double
        double categoryPrice = Double.parseDouble(categoryPriceStr);

        // Create a new category document
        Map<String, Object> categoryData = new HashMap<>();
        categoryData.put("name", categoryName);
        categoryData.put("price", categoryPrice);

        // Add the category document to the "categories" collection within the selected dish
        db.collection("cuisines")
                .document(cuisineName)
                .collection("dishes")
                .document(dishName)
                .collection("categories")
                .add(categoryData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getActivity(), "Category added successfully", Toast.LENGTH_SHORT).show();
                        // Clear category name and price fields after successful addition
                        editTextCategoryName.setText("");
                        editTextCategoryPrice.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Failed to add category: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}

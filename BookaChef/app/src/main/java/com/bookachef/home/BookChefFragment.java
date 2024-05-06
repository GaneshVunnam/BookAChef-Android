package com.bookachef.home;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bookachef.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class BookChefFragment extends Fragment {

    private BookingData bookingData; // Use the local BookingData class


    public BookChefFragment() {
        // Required empty public constructor
    }

    public static BookChefFragment newInstance() {
        return new BookChefFragment();
    }

    private LinearLayout dishBoxesContainer;
    private LinearLayout categoryBoxesContainer;
    private String selectedCuisine;
    private String selectedDish;
    private List<String> selectedCategories = new ArrayList<>();
    private MaterialAutoCompleteTextView dietaryRestrictionsEditText;

    private void openSelectCuisineLayout() {

        // Store the bookingData object in SharedPreferences for later use
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("BookingData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Venue", bookingData.getVenue());
        editor.putString("Date", bookingData.getDate());
        editor.putInt("Number of Guests", bookingData.getNumOfGuests());
        editor.putString("Meal", bookingData.getMeal());
        editor.apply();

        // Create a new instance of the select cuisine layout
        View selectCuisineView = LayoutInflater.from(getContext()).inflate(R.layout.select_cuisine_layout, null);

        // Access views in the select cuisine layout
        Spinner cuisineSpinner = selectCuisineView.findViewById(R.id.cuisineSpinner);
        dishBoxesContainer = selectCuisineView.findViewById(R.id.dishBoxesContainer);
        categoryBoxesContainer = selectCuisineView.findViewById(R.id.categoryBoxesContainer);
        dietaryRestrictionsEditText = selectCuisineView.findViewById(R.id.dietaryRestrictionsEditText); // Assign value
        Button nextButton1 = selectCuisineView.findViewById(R.id.nextButton1);
        ImageView backButton = selectCuisineView.findViewById(R.id.backButton);

        // Set OnClickListener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the HomeFragment
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new BookChefFragment()); // Assuming R.id.fragment_container is the ID of the container where you replace fragments
                fragmentTransaction.addToBackStack(null); // Add the transaction to the back stack
                fragmentTransaction.commit();
            }
        });


        // Open select cuisine view
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(selectCuisineView);

        // Retrieve Cuisine Names from Firestore and populate the spinner
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("cuisines")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> cuisineNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String cuisineName = document.getString("name");
                            cuisineNames.add(cuisineName);
                        }
                        ArrayAdapter<String> cuisineAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, cuisineNames);
                        cuisineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        cuisineSpinner.setAdapter(cuisineAdapter);

                        // Set up listener for cuisine selection
                        cuisineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                // Retrieve and show dishes associated with the selected cuisine
                                selectedCuisine = cuisineNames.get(position);
                                showDishes(selectedCuisine);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                // Handle case when nothing is selected
                            }
                        });
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });

        CompletableFuture<List<BookChefFragment.CategoryPrice>> futureCategoryPrices = new CompletableFuture<>();


        // Set OnClickListener for the "Next" button
        nextButton1.setOnClickListener(v -> {
            // Log the selected data
            if (selectedCuisine != null && selectedDish != null && !selectedCategories.isEmpty()) {
                Log.d(TAG, "Selected Cuisine: " + selectedCuisine);
                Log.d(TAG, "Selected Dish: " + selectedDish);
                Log.d(TAG, "Selected Categories: " + selectedCategories.toString());
                Log.d(TAG, "Dietary Restrictions: " + dietaryRestrictionsEditText.getText().toString()); // Add this line

                // Create a list to hold category prices
                List<BookChefFragment.CategoryPrice> categoryPrices = new ArrayList<>();

                // Fetch category prices asynchronously
                fetchCategoryPrices(selectedCategories, categoryPrices, () -> {
                    // Once category prices are fetched, calculate total price and open confirm booking layout
                    double totalPrice = calculateTotalPrice(categoryPrices);
                    // Fetch category prices asynchronously
                    fetchCategoryPrices(selectedCategories, categoryPrices, () -> {
                        // Once category prices are fetched, calculate total price and open confirm booking layout
                        openConfirmbookinglayout(selectedCuisine, selectedDish, selectedCategories, dietaryRestrictionsEditText.getText().toString(), totalPrice);
                    });
                });
            } else {
                Log.d(TAG, "No data selected.");
            }
        });


    }



    // Method to fetch and log prices of selected categories
    // Method to fetch and log prices of selected categories
    private void fetchCategoryPrices(List<String> selectedCategories, List<CategoryPrice> categoryPrices, Runnable onComplete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        AtomicInteger tasksCount = new AtomicInteger(selectedCategories.size());
        for (String categoryName : selectedCategories) {
            db.collection("cuisines")
                    .whereEqualTo("name", selectedCuisine)
                    .limit(1)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot cuisineDoc : task.getResult()) {
                                String cuisineId = cuisineDoc.getId();
                                db.collection("cuisines")
                                        .document(cuisineId)
                                        .collection("dishes")
                                        .whereEqualTo("name", selectedDish)
                                        .limit(1)
                                        .get()
                                        .addOnCompleteListener(dishTask -> {
                                            if (dishTask.isSuccessful()) {
                                                for (QueryDocumentSnapshot dishDoc : dishTask.getResult()) {
                                                    db.collection("cuisines")
                                                            .document(cuisineId)
                                                            .collection("dishes")
                                                            .document(dishDoc.getId())
                                                            .collection("categories")
                                                            .whereEqualTo("name", categoryName)
                                                            .limit(1)
                                                            .get()
                                                            .addOnCompleteListener(categoryTask -> {
                                                                if (categoryTask.isSuccessful()) {
                                                                    for (QueryDocumentSnapshot categoryDoc : categoryTask.getResult()) {
                                                                        double categoryPrice = categoryDoc.getDouble("price");
                                                                        categoryPrices.add(new CategoryPrice(categoryName, categoryPrice));
                                                                    }
                                                                } else {
                                                                    Log.d(TAG, "Error getting category documents: ", categoryTask.getException());
                                                                }
                                                                if (tasksCount.decrementAndGet() == 0) {
                                                                    onComplete.run();
                                                                }
                                                            });
                                                }
                                            } else {
                                                Log.d(TAG, "Error getting dish documents: ", dishTask.getException());
                                            }
                                        });
                            }
                        } else {
                            Log.d(TAG, "Error getting cuisine documents: ", task.getException());
                        }
                    });
        }
    }

    // Method to calculate total price
    private double calculateTotalPrice(List<BookChefFragment.CategoryPrice> categoryPrices) {
        int numOfGuests = getNumberOfGuestsFromSharedPreferences();
        double totalPrice = categoryPrices.stream().mapToDouble(BookChefFragment.CategoryPrice::getPrice).sum() * numOfGuests;
        return totalPrice;
    }

    // Method to get the number of guests from SharedPreferences
    private int getNumberOfGuestsFromSharedPreferences() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("BookingData", Context.MODE_PRIVATE);
        int numOfGuests = sharedPreferences.getInt("Number of Guests", 0);
        return numOfGuests;
    }
    class CategoryPrice {
        private String categoryName;
        private double price;

        public CategoryPrice(String categoryName, double price) {
            this.categoryName = categoryName;
            this.price = price;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public double getPrice() {
            return price;
        }
    }



    // Inside openConfirmbookinglayout()
    private void openConfirmbookinglayout(String selectedCuisine, String selectedDish, List<String> selectedCategories, String dietaryRestrictions, double totalPrice) {
        // Retrieve other data from SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("BookingData", Context.MODE_PRIVATE);
        String venue = sharedPreferences.getString("Venue", "");
        String date = sharedPreferences.getString("Date", "");
        int numOfGuests = sharedPreferences.getInt("Number of Guests", 0);
        String meal = sharedPreferences.getString("Meal", "");

        // Create a new instance of the confirm booking layout
        View confirmBookingView = LayoutInflater.from(getContext()).inflate(R.layout.confirm_booking_layout, null);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT); // Center the view horizontally and vertically

        // Apply the layout parameters to the inflated view
        confirmBookingView.setLayoutParams(layoutParams);

        // Access views in the confirm booking layout
        TextView venueTextView = confirmBookingView.findViewById(R.id.venueTextView);
        TextView numberOfGuestsTextView = confirmBookingView.findViewById(R.id.numberOfGuestsTextView);
        TextView mealTextView = confirmBookingView.findViewById(R.id.mealTextView);
        TextView dateTextView = confirmBookingView.findViewById(R.id.dateTextView);
        TextView selectedCuisineTextView = confirmBookingView.findViewById(R.id.selectedCuisineTextView);
        TextView selectedDishTextView = confirmBookingView.findViewById(R.id.selectedDishTextView);
        TextView selectedCategoriesTextView = confirmBookingView.findViewById(R.id.selectedCategoriesTextView);
        TextView priceTextView = confirmBookingView.findViewById(R.id.priceTextView);
        TextView gstTextView = confirmBookingView.findViewById(R.id.gstTextView);
        TextView totalPriceTextView = confirmBookingView.findViewById(R.id.totalPriceTextView);
        TextView deitaryrestriction = confirmBookingView.findViewById(R.id.selectedDietaryrestrictionTextView);
        ImageView backButton = confirmBookingView.findViewById(R.id.backButton);

        // Set OnClickListener for the "Back" button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the booking screen
                openSelectCuisineLayout();
            }
        });

        // Set the values for each TextView
        venueTextView.setText("Venue: " + venue);
        numberOfGuestsTextView.setText("Number of Guests: " + numOfGuests);
        mealTextView.setText("Meal: " + meal);
        dateTextView.setText("Date: " + date);
        selectedCuisineTextView.setText("Selected Cuisine: " + selectedCuisine);
        selectedDishTextView.setText("Selected Dish: " + selectedDish);
        selectedCategoriesTextView.setText("Selected Categories: " + selectedCategories.toString());
        totalPriceTextView.setText("Total Price: $" + totalPrice);
        deitaryrestriction.setText("Dietary Restrictions: " + dietaryRestrictions);

        // Calculate GST (18%)
        double gst = totalPrice * 0.18;
        double totalPriceWithGST = totalPrice + gst;

        // Set the price and GST TextViews
        priceTextView.setText("Price: ₹" + totalPrice);
        gstTextView.setText("GST (18%): ₹" + gst);
        totalPriceTextView.setText("Total Price (incl. GST): ₹" + totalPriceWithGST);


        // Add OnClickListener for Confirm Booking button
        Button confirmBookingButton = confirmBookingView.findViewById(R.id.confirmBookingButton);
        confirmBookingButton.setOnClickListener(v -> {
            // Handle confirm booking action here
            // Step 1: Storing Booking Data with User ID
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String userId = currentUser.getUid();
            DocumentReference docRef = FirebaseFirestore.getInstance().collection("chefbookings").document();
            Map<String, Object> bookingData = new HashMap<>();
            bookingData.put("userId", userId);
            bookingData.put("venue", venue);
            bookingData.put("numOfGuests", numOfGuests);
            bookingData.put("meal", meal);
            bookingData.put("date", date);
            bookingData.put("selectedCuisine", selectedCuisine);
            bookingData.put("selectedDish", selectedDish);
            bookingData.put("selectedCategories", selectedCategories);
            bookingData.put("totalPrice", totalPriceWithGST);
            bookingData.put("dietaryRestrictions", dietaryRestrictions);
            bookingData.put("status", "Pending");
            docRef.set(bookingData)
                    .addOnSuccessListener(aVoid -> {
                        // Data added successfully
                        showConfirmationDialog();
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        });




        // Open confirm booking view
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(confirmBookingView);
    }

    // Show confirmation dialog
    private void showConfirmationDialog() {
        // Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Booking Confirmed")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Dismiss the dialog
                    dialog.dismiss();


                    // Navigate to the home fragment
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new BookChefFragment())
                            .addToBackStack(null)
                            .commit();

                })
                .show();
    }






    private void showDishes(String selectedCuisine) {
        // Access the Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the dishes collection under the selected cuisine
        db.collection("cuisines").whereEqualTo("name", selectedCuisine).limit(1).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String cuisineId = document.getId();
                            // Query the dishes subcollection under the selected cuisine
                            db.collection("cuisines").document(cuisineId).collection("dishes")
                                    .get()
                                    .addOnCompleteListener(dishTask -> {
                                        if (dishTask.isSuccessful()) {
                                            List<String> dishes = new ArrayList<>();
                                            for (QueryDocumentSnapshot dishDoc : dishTask.getResult()) {
                                                String dishName = dishDoc.getString("name");
                                                dishes.add(dishName);
                                            }
                                            // Populate dish view with Spinner for selecting a dish
                                            ArrayAdapter<String> dishAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, dishes);
                                            dishAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            Spinner dishSpinner = new Spinner(getContext());
                                            dishSpinner.setAdapter(dishAdapter);
                                            dishBoxesContainer.removeAllViews();
                                            dishBoxesContainer.addView(dishSpinner);

                                            // Set up listener for dish selection
                                            dishSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                    selectedDish = dishes.get(position);
                                                    // Show categories for the selected dish
                                                    showCategories(selectedCuisine, selectedDish);
                                                }

                                                @Override
                                                public void onNothingSelected(AdapterView<?> parent) {
                                                    // Handle case when nothing is selected
                                                }
                                            });

                                            // Set visibility of dish view to VISIBLE
                                            dishBoxesContainer.setVisibility(View.VISIBLE);
                                            // Set visibility of category view to GONE initially
                                            categoryBoxesContainer.setVisibility(View.GONE);
                                        } else {
                                            Log.d(TAG, "Error getting documents: ", dishTask.getException());
                                        }
                                    });
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }



    private void showCategories(String cuisineName, String dishName) {
        // Access the Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the dishes collection under the selected cuisine
        db.collection("cuisines")
                .whereEqualTo("name", cuisineName)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot cuisineDoc : task.getResult()) {
                            String cuisineId = cuisineDoc.getId();
                            // Query the dishes subcollection under the selected cuisine
                            db.collection("cuisines")
                                    .document(cuisineId)
                                    .collection("dishes")
                                    .whereEqualTo("name", dishName)
                                    .limit(1)
                                    .get()
                                    .addOnCompleteListener(dishTask -> {
                                        if (dishTask.isSuccessful()) {
                                            for (QueryDocumentSnapshot dishDoc : dishTask.getResult()) {
                                                // Fetch the categories subcollection from the dish document
                                                db.collection("cuisines")
                                                        .document(cuisineId)
                                                        .collection("dishes")
                                                        .document(dishDoc.getId())
                                                        .collection("categories")
                                                        .get()
                                                        .addOnCompleteListener(categoryTask -> {
                                                            if (categoryTask.isSuccessful()) {
                                                                List<String> categories = new ArrayList<>();
                                                                for (QueryDocumentSnapshot categoryDoc : categoryTask.getResult()) {
                                                                    String categoryName = categoryDoc.getString("name");
                                                                    categories.add(categoryName);
                                                                    // Check if the category is selected
                                                                    if (selectedCategories.contains(categoryName)) {
                                                                        // Retrieve the price field from Firestore
                                                                        double categoryPrice = categoryDoc.getDouble("price");
                                                                        Log.d(TAG, "Selected category: " + categoryName + ", Price: " + categoryPrice);
                                                                    }
                                                                }
                                                                if (!categories.isEmpty()) {
                                                                    // Populate category view with checkboxes for each category
                                                                    categoryBoxesContainer.removeAllViews();
                                                                    for (String category : categories) {
                                                                        CheckBox checkBox = new CheckBox(getContext());
                                                                        checkBox.setText(category);
                                                                        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                                                            if (isChecked) {
                                                                                selectedCategories.add(category);
                                                                            } else {
                                                                                selectedCategories.remove(category);
                                                                            }
                                                                        });
                                                                        categoryBoxesContainer.addView(checkBox);
                                                                    }

                                                                    // Set visibility of category view to VISIBLE
                                                                    categoryBoxesContainer.setVisibility(View.VISIBLE);
                                                                } else {
                                                                    Log.d(TAG, "No categories found for dish: " + dishName);
                                                                }
                                                            } else {
                                                                Log.d(TAG, "Error getting category documents: ", categoryTask.getException());
                                                            }
                                                        });
                                            }
                                        } else {
                                            Log.d(TAG, "Error getting dish documents: ", dishTask.getException());
                                        }
                                    });
                        }
                    } else {
                        Log.d(TAG, "Error getting cuisine documents: ", task.getException());
                    }
                });
    }


    public class BookingData {
        private String venue;
        private String date;
        private int numOfGuests;
        private String meal;

        // Constructor
        public BookingData() {
            // Default constructor
        }

        // Getter and setter methods for venue
        public String getVenue() {
            return venue;
        }

        public void setVenue(String venue) {
            this.venue = venue;
        }

        // Getter and setter methods for date
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        // Getter and setter methods for numOfGuests
        public int getNumOfGuests() {
            return numOfGuests;
        }

        public void setNumOfGuests(int numOfGuests) {
            this.numOfGuests = numOfGuests;
        }

        // Getter and setter methods for meal
        public String getMeal() {
            return meal;
        }

        public void setMeal(String meal) {
            this.meal = meal;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.book_a_chef_flow1, container, false);

        TextView dateTextView = view.findViewById(R.id.dateTextView);
        DatePicker datePicker = view.findViewById(R.id.datePicker);
        Button nextButton = view.findViewById(R.id.nextButton);
        MaterialAutoCompleteTextView placeEditText = view.findViewById(R.id.PlaceEditText);
        Spinner mealSpinner = view.findViewById(R.id.mealSpinner);
        TextView guestsTextView = view.findViewById(R.id.guestsTextView);
        Button decreaseGuestsButton = view.findViewById(R.id.decreaseGuestsButton);
        Button increaseGuestsButton = view.findViewById(R.id.increaseGuestsButton);
        ImageView backButton = view.findViewById(R.id.backButton);

        // Set OnClickListener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the HomeFragment
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new HomeFragment()); // Assuming R.id.fragment_container is the ID of the container where you replace fragments
                fragmentTransaction.addToBackStack(null); // Add the transaction to the back stack
                fragmentTransaction.commit();
            }
        });

        // Set OnClickListener for the dateTextView to toggle the visibility of the datePicker
        dateTextView.setOnClickListener(v -> {
            // Toggle visibility of the datePicker
            if (datePicker.getVisibility() == View.VISIBLE) {
                datePicker.setVisibility(View.GONE);
            } else {
                datePicker.setVisibility(View.VISIBLE);
            }
        });

        // Set up meal spinner
        ArrayAdapter<String> mealAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new String[]{"Breakfast", "Lunch", "Dinner"});
        mealAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealSpinner.setAdapter(mealAdapter);

        // Declare an array to hold the number of guests
        final int[] numOfGuests = {1}; // Initial value is 1

        // Increase guests when clicking on plus button
        increaseGuestsButton.setOnClickListener(v -> {
            numOfGuests[0]++;
            guestsTextView.setText(String.valueOf(numOfGuests[0]));
        });

        // Decrease guests when clicking on minus button
        decreaseGuestsButton.setOnClickListener(v -> {
            if (numOfGuests[0] > 1) {
                numOfGuests[0]--;
                guestsTextView.setText(String.valueOf(numOfGuests[0]));
            }
        });

        datePicker.init(
                datePicker.getYear(),
                datePicker.getMonth(),
                datePicker.getDayOfMonth(),
                (view1, year, monthOfYear, dayOfMonth) -> {
                    String selectedDate = formatDate(year, monthOfYear, dayOfMonth);
                    dateTextView.setText(selectedDate);
                });

        nextButton.setOnClickListener(v -> {
            int selectedYear = datePicker.getYear();
            int selectedMonth = datePicker.getMonth();
            int selectedDayOfMonth = datePicker.getDayOfMonth();
            String selectedDate = formatDate(selectedYear, selectedMonth, selectedDayOfMonth);
            String selectedPlace = placeEditText.getText().toString();
            String selectedMeal = (String) mealSpinner.getSelectedItem();
            int numberOfGuests = Integer.parseInt(guestsTextView.getText().toString());

            // Create a new instance of BookingData and populate it with user-entered information
            bookingData = new BookingData();
            bookingData.setVenue(selectedPlace);
            bookingData.setDate(selectedDate);
            bookingData.setNumOfGuests(numberOfGuests);
            bookingData.setMeal(selectedMeal);

            // Log the booking data
            Log.d("BookingData", "Venue: " + bookingData.getVenue());
            Log.d("BookingData", "Date: " + bookingData.getDate());
            Log.d("BookingData", "Number of Guests: " + bookingData.getNumOfGuests());
            Log.d("BookingData", "Meal: " + bookingData.getMeal());

            // Pass the bookingData to the next screen or save it for later use
            // For now, let's log it
            Log.d("BookingData", "BookingData: " + bookingData.toString());

            openSelectCuisineLayout();
        });

        return view;
    }

    private String formatDate(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }
}

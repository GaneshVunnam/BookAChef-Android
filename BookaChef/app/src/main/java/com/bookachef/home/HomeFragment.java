package com.bookachef.home;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bookachef.R;
import com.bookachef.adapters.SpecialOccasionsAdapter;
import com.bookachef.adapters.UpcomingEventsAdapter;
import com.bookachef.models.Event;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment {

    // RecyclerViews
    private RecyclerView upcomingEventsRecyclerView;
    private RecyclerView specialOccasionsRecyclerView;
    private Button openbookachef,openbookachef1;

    // Adapters
    private UpcomingEventsAdapter upcomingEventsAdapter;
    private SpecialOccasionsAdapter specialOccasionsAdapter;

    // List of events
    private List<Event> upcomingEventsList;
    private List<Event> specialOccasionsList;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize RecyclerViews
        upcomingEventsRecyclerView = view.findViewById(R.id.upcomingEventsRecyclerView);
        specialOccasionsRecyclerView = view.findViewById(R.id.specialOccasionsRecyclerView);
        openbookachef = view.findViewById(R.id.openbookachef);
        openbookachef1 = view.findViewById(R.id.openbookachef1);

        // Initialize lists
        upcomingEventsList = new ArrayList<>();
        specialOccasionsList = new ArrayList<>();

        // Set up adapters
        upcomingEventsAdapter = new UpcomingEventsAdapter(getContext(), upcomingEventsList, event -> {
            // Handle click on upcoming event item
            // For now, you can leave it empty or implement any desired functionality
            openBookingScreen(event); // Call method to open booking screen
        });

        specialOccasionsAdapter = new SpecialOccasionsAdapter(getContext(), specialOccasionsList, event -> {
            // Handle click on special occasion item
            // For now, you can leave it empty or implement any desired functionality
            openBookingScreen(event); // Call method to open booking screen
        });

        // Set layout managers and adapters for RecyclerViews
        upcomingEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        upcomingEventsRecyclerView.setAdapter(upcomingEventsAdapter);

        specialOccasionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        specialOccasionsRecyclerView.setAdapter(specialOccasionsAdapter);

        // Load events data
        loadEvents();

        openbookachef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace the current fragment with the BookChefFragment
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new BookChefFragment());
                fragmentTransaction.addToBackStack(null); // Optional: Add to back stack to enable back navigation
                fragmentTransaction.commit();
            }
        });

        openbookachef1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace the current fragment with the BookChefFragment
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new BookChefFragment());
                fragmentTransaction.addToBackStack(null); // Optional: Add to back stack to enable back navigation
                fragmentTransaction.commit();
            }
        });




        return view;
    }

    private void loadEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Load upcoming events
        db.collection("upcoming_events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                upcomingEventsList.clear(); // Clear existing list
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Event event = document.toObject(Event.class);
                    upcomingEventsList.add(event);
                }
                upcomingEventsAdapter.notifyDataSetChanged(); // Notify adapter of data changes
            }
        });

        // Load special occasions
        db.collection("special_occasions").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                specialOccasionsList.clear(); // Clear existing list
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Event event = document.toObject(Event.class);
                    specialOccasionsList.add(event);
                }
                specialOccasionsAdapter.notifyDataSetChanged(); // Notify adapter of data changes
            }
        });
    }

    private LinearLayout dishBoxesContainer;
    private LinearLayout categoryBoxesContainer;
    private String selectedCuisine;
    private String selectedDish;
    private List<String> selectedCategories = new ArrayList<>();
    private MaterialAutoCompleteTextView dietaryRestrictionsEditText;

    private void openSelectCuisineLayout(Event event) {

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

        // Set OnClickListener for the "Back" button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the booking screen
                openBookingScreen(event); // Assuming you have access to the 'event' variable
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

        CompletableFuture<List<CategoryPrice>> futureCategoryPrices = new CompletableFuture<>();


        // Set OnClickListener for the "Next" button
        nextButton1.setOnClickListener(v -> {
            // Log the selected data
            if (selectedCuisine != null && selectedDish != null && !selectedCategories.isEmpty()) {
                Log.d(TAG, "Selected Cuisine: " + selectedCuisine);
                Log.d(TAG, "Selected Dish: " + selectedDish);
                Log.d(TAG, "Selected Categories: " + selectedCategories.toString());
                Log.d(TAG, "Dietary Restrictions: " + dietaryRestrictionsEditText.getText().toString()); // Add this line

                // Create a list to hold category prices
                List<CategoryPrice> categoryPrices = new ArrayList<>();

                // Fetch category prices asynchronously
                fetchCategoryPrices(selectedCategories, categoryPrices, () -> {
                    // Once category prices are fetched, calculate total price and open confirm booking layout
                    double totalPrice = calculateTotalPrice(categoryPrices);
                    // Fetch category prices asynchronously
                    fetchCategoryPrices(selectedCategories, categoryPrices, () -> {
                        // Once category prices are fetched, calculate total price and open confirm booking layout
                        openConfirmbookinglayout(selectedCuisine, selectedDish, selectedCategories, dietaryRestrictionsEditText.getText().toString(), totalPrice, event);
                    });
                });
            } else {
                Log.d(TAG, "No data selected.");
            }
        });


    }




    // Method to fetch and log prices of selected categories
    private void fetchCategoryPrices(List<String> selectedCategories, List<CategoryPrice> categoryPrices, Runnable onComplete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Use AtomicInteger to count the number of completed tasks
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
                                                                        // Add fetched category price to the list
                                                                        categoryPrices.add(new CategoryPrice(categoryName, categoryPrice));
                                                                    }
                                                                } else {
                                                                    Log.d(TAG, "Error getting category documents: ", categoryTask.getException());
                                                                }
                                                                // Decrement tasks count and call onComplete when all tasks are completed
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
    private double calculateTotalPrice(List<CategoryPrice> categoryPrices) {
        int numOfGuests = getNumberOfGuestsFromSharedPreferences();
        double totalPrice = categoryPrices.stream().mapToDouble(CategoryPrice::getPrice).sum() * numOfGuests;
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
    private void openConfirmbookinglayout(String selectedCuisine, String selectedDish, List<String> selectedCategories, String dietaryRestrictions, double totalPrice, Event event) {
        // Retrieve other data from SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("BookingData", Context.MODE_PRIVATE);
        String venue = sharedPreferences.getString("Venue", "");
        String date = sharedPreferences.getString("Date", "");
        int numOfGuests = sharedPreferences.getInt("Number of Guests", 0);
        String meal = sharedPreferences.getString("Meal", "");

        // Create a new instance of the confirm booking layout
        View confirmBookingView = LayoutInflater.from(getContext()).inflate(R.layout.confirm_booking_layout, null);
        // Create layout parameters for the inflated view
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
                openSelectCuisineLayout(event); // Assuming you have access to the 'event' variable
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
        totalPriceTextView.setText("Total Price: ₹" + totalPrice);
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
            DocumentReference docRef = FirebaseFirestore.getInstance().collection("bookings").document();
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
                            .replace(R.id.fragment_container, new HomeFragment())
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
                                            // Set custom background drawable
                                            dishSpinner.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.list_bg));
                                            // Set custom height and width
                                            int heightInPixels = (int) getResources().getDimension(R.dimen.spinner_height); // Assuming spinner_height is defined in dimensions.xml
                                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightInPixels);
                                            dishSpinner.setLayoutParams(params);

// Change font
//                                            Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.poppins_regular);
//                                            ((TextView) dishSpinner.getSelectedView()).setTypeface(typeface);

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
                                                                        // Set custom background drawable
                                                                        checkBox.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.list_bg));
//                                                                        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.poppins_regular);
//                                                                        checkBox.setTypeface(typeface);

                                                                        // Set custom height and width
                                                                        int heightInPixels = (int) getResources().getDimension(R.dimen.spinner_height); // Assuming spinner_height is defined in dimensions.xml
                                                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightInPixels);
                                                                        // Set margin
                                                                        int marginInPixels = (int) getResources().getDimension(R.dimen.checkbox_margin); // Assuming checkbox_margin is defined in dimensions.xml
                                                                        params.setMargins(0, marginInPixels, 0, marginInPixels);
                                                                        checkBox.setLayoutParams(params);

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













    // Define a data model class to hold booking information
    public class BookingData {
        private String venue;
        private String date;
        private int numOfGuests;
        private String meal;

        // Constructor, getters, and setters
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

    private BookingData bookingData;


    // Modify your method to handle the "Next" button click
    private void openBookingScreen(Event event) {
        // Create a new instance of the booking screen layout
        View bookingScreenView = LayoutInflater.from(getContext()).inflate(R.layout.booking_screen_layout, null);

        // Access views in the booking screen layout
        Spinner venueSpinner = bookingScreenView.findViewById(R.id.venueSpinner);
        TextView dateTextView = bookingScreenView.findViewById(R.id.dateTextView);
        TextView guestsTextView = bookingScreenView.findViewById(R.id.guestsTextView);
        Spinner mealSpinner = bookingScreenView.findViewById(R.id.mealSpinner);
        Button nextButton = bookingScreenView.findViewById(R.id.nextButton);
        Button decreaseGuestsButton = bookingScreenView.findViewById(R.id.decreaseGuestsButton);
        Button increaseGuestsButton = bookingScreenView.findViewById(R.id.increaseGuestsButton);
        ImageView backButton = bookingScreenView.findViewById(R.id.backButton);

        // Populate views with event details
        dateTextView.setText(event.getDate());

        // Set up venue spinner
        ArrayAdapter<String> venueAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new String[]{event.getVenue()});
        venueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        venueSpinner.setAdapter(venueAdapter);

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

        // Set OnClickListener for the "Next" button
        nextButton.setOnClickListener(v -> {
            // Create a new instance of BookingData and populate it with user-entered information
            bookingData = new BookingData();
            bookingData.setVenue((String) venueSpinner.getSelectedItem());
            bookingData.setDate(event.getDate());
            bookingData.setNumOfGuests(numOfGuests[0]);
            bookingData.setMeal((String) mealSpinner.getSelectedItem());

            // Log the booking data
            Log.d("BookingData", "Venue: " + bookingData.getVenue());
            Log.d("BookingData", "Date: " + bookingData.getDate());
            Log.d("BookingData", "Number of Guests: " + bookingData.getNumOfGuests());
            Log.d("BookingData", "Meal: " + bookingData.getMeal());

            // Pass the bookingData to the next screen or save it for later use
            // You can navigate to the next screen or store the data as needed
            // For now, let's navigate to the select_cuisine_layout.xml
            // Open select cuisine layout
            openSelectCuisineLayout(event);
        });

        // Open booking screen view
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(bookingScreenView);

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



    }






}

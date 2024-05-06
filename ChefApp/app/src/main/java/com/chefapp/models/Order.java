package com.chefapp.models;

import java.util.List;

public class Order {
    private String documentId; // Add document ID field

    private String venue;
    private String date;
    private String meal;
    private int numOfGuests;
    private String selectedCuisine;
    private String selectedDish;
    private List<String> selectedCategories; // Change to List<String>
    private double totalPrice; // Change to double


    private String dietaryRestrictions; // Change to String
    private String status;

    // Default constructor required for Firestore deserialization
    public Order() {
        // Required empty constructor
    }

    // Constructor
    public Order(String venue, String date, String meal, int numOfGuests, String selectedCuisine,
                 String selectedDish, List<String> selectedCategories, double totalPrice,
                 String dietaryRestrictions, String status) {
        this.venue = venue;
        this.date = date;
        this.meal = meal;
        this.numOfGuests = numOfGuests;
        this.selectedCuisine = selectedCuisine;
        this.selectedDish = selectedDish;
        this.selectedCategories = selectedCategories;
        this.totalPrice = totalPrice;
        this.dietaryRestrictions = dietaryRestrictions;
        this.status = status;
    }

    // Getters
    public String getVenue() {
        return venue;
    }

    public String getDate() {
        return date;
    }

    public String getMeal() {
        return meal;
    }

    public int getNumOfGuests() {
        return numOfGuests;
    }

    public String getSelectedCuisine() {
        return selectedCuisine;
    }

    public String getSelectedDish() {
        return selectedDish;
    }

    public List<String> getSelectedCategories() {
        return selectedCategories;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public String getStatus() {
        return status;
    }

    // Getter and setter for document ID
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}

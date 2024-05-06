package com.bookachef.models;

// Dish.java
public class Dish {
    private String name;

    public Dish() {
        // Empty constructor needed for Firestore
    }

    public Dish(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

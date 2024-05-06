package com.bookachef.models;

// Category.java
public class Category {
    private String name;

    public Category() {
        // Empty constructor needed for Firestore
    }

    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


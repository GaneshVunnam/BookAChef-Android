package com.bookachef.models;

public class Event {
    private String id;
    private String title;
    private String imageUrl;
    private String date;
    private String time;
    private String venue;

    // Constructors, getters, and setters

    public Event() {
        // Default constructor required for Firestore
    }

    public Event(String id, String title, String imageUrl, String date, String time, String venue) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.date = date;
        this.time = time;
        this.venue = venue;
    }

    // Getters and setters for each field

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }
}


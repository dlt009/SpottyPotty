package com.teamoptimal.cse110project.data;

/**
 * Created by dltan on 2/21/2016.
 */
public class RestroomItem {
    String title;
    String distance;
    double rating;
    float color;

    public RestroomItem(String title, String distance, double rating, float color) {
        this.title = title;
        this.distance = distance;
        this.rating = rating;
        this.color = color;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public float getColor() { return color; }
    public void setColor(float color) { this.color = color; }
}
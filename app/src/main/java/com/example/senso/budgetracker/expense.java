package com.example.senso.budgetracker;

import com.google.android.gms.maps.model.LatLng;

/**
Expense class
 TODO:  image properties to the object
 */

public class expense {

    String name;

    String category;

    String description;

    String date;

    double cost;
    LatLng spot;

    //must have parameters constructor
    public expense(String name, String category, double cost, String date) {
        this.name = name    ;
        this.category = category;
        this.cost = cost;
        this.date = date;
    }
    //getters
    public String getName() {
        return name;
    }
    public String getCategory() {
        return category;
    }
    public String getDescription() {
        return description;
    }


    public double getCost() {
        return cost;
    }

    public String getDate() {
        return date;
    }

    // set 0 as "not-declared" location
    public double getLat() { return spot != null? spot.latitude:0;}

    public double getLng() { return spot != null? spot.longitude:0;}




    //setters
    public void setDate(String date) {
        this.date = date;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setSpot(LatLng location) { this.spot = location; }

    @Override
    public String toString() {
        return this.name+": "+this.getCost()+", "+this.getCategory()+", "+getDescription()+ ", "+getDate();
    }
}

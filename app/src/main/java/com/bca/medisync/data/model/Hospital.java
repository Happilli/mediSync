package com.bca.medisync.data.model;

public class Hospital {
    private  final String id;
    private final String name;
    private final String address;
    private final String phone;
    private final String website;
    private final String description;
    private final double rating;
    private final String imageUrl;


    public  Hospital(String id, String name, String address, String phone, String website, String description, double rating, String imageUrl){
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.website = website;
        this.description = description;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getWebsite() {
        return website;
    }

    public String getDescription() {
        return description;
    }

    public double getRating() {
        return rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}


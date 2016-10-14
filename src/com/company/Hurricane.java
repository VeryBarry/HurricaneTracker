package com.company;

/**
 * Created by VeryBarry on 10/4/16.
 */
public class Hurricane {
    int id;
    String name;
    String location;
    int category;
    String image;
    int userId;

    public Hurricane(int id, String name, String location, int category, String image, int userId) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.category = category;
        this.image = image;
        this.userId = userId;
    }
}

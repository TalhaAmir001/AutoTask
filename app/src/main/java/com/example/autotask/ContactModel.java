package com.example.autotask;

import java.util.ArrayList;

public class ContactModel {
    private String  id;
    private String name;
    private ArrayList<String> phoneNumber;
    private boolean isSelected;

    public ContactModel(String  id, String name, ArrayList<String> phoneNumber) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.isSelected = false; // Initially not selected
    }

    public ContactModel() {

    }

    // Getters and setters
    public String  getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(ArrayList<String> phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}


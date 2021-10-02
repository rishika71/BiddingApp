package com.example.biddingapp.models;

import java.io.Serializable;

public class User implements Serializable {

    String firstName, lastName, id;
    int currentBalance;

    public User(){

    }

    public User(String firstName, String lastName, String id, int currentBalance) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.currentBalance = currentBalance;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(int currentBalance) {
        this.currentBalance = currentBalance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", id='" + id + '\'' +
                ", currentBalance=" + currentBalance +
                '}';
    }
}

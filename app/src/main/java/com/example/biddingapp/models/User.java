package com.example.biddingapp.models;

import java.io.Serializable;
import java.text.DecimalFormat;

public class User implements Serializable {

    String firstname, lastname, email, id;
    Double currentbalance = 200.00, hold = 0.00;
    String noti_token = null;

    public String getNoti_token() {
        return noti_token;
    }

    public void setNoti_token(String noti_token) {
        this.noti_token = noti_token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName(){
        return this.firstname + " " + this.lastname;
    }

    public User(){}

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Double getCurrentbalance() {
        return currentbalance;
    }

    public void setCurrentbalance(Double currentbalance) {
        this.currentbalance = currentbalance;
    }

    public void addCurrentbalance(Double balance){
        this.currentbalance += balance;
    }

    public Double getHold() {
        return hold;
    }

    public void setHold(Double hold) {
        this.hold = hold;
    }

    public User(String firstname, String lastname, String email, String id) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.id = id;
    }

    public String getPrettyBalance(){
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return "$" + decimalFormat.format(this.currentbalance);
    }

    public String getPrettyHold(){
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return "$" + decimalFormat.format(this.hold);
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
                "firstName='" + firstname + '\'' +
                ", lastName='" + lastname + '\'' +
                ", id='" + id + '\'' +
                ", currentBalance=" + currentbalance +
                '}';
    }
}

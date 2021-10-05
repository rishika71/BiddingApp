package com.example.biddingapp.models;

import java.util.ArrayList;

public class Transaction {
    ArrayList<History> history;

    public Transaction() {
    }

    public ArrayList<History> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<History> history) {
        this.history = history;
    }
}

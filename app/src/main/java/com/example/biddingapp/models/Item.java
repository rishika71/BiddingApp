package com.example.biddingapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Item implements Serializable {

    String id, name, owner_id, owner_name;
    Date created_at = new Date();
    Double startBid;
    Double finalBid;
    String winningBid = null;
    ArrayList<Bid> bids = new ArrayList<>();

    public Date getCreated_at() {
        return created_at;
    }

    public String getNewBidId(){
        return String.valueOf(bids.size() + 1);
    }

    public Bid getWinBid(){
        if(winningBid == null) return null;
        for (Bid bid:
             bids) {
            if(bid.getId().equals(winningBid))  return bid;
        }
        return null;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", owner_id='" + owner_id + '\'' +
                ", owner_name='" + owner_name + '\'' +
                ", startBid=" + startBid +
                ", finalBid=" + finalBid +
                ", winningBid='" + winningBid + '\'' +
                ", bids=" + bids +
                '}';
    }

    public Item(String name, String owner_id, String owner_name, Double startBid, Double finalBid) {
        this.name = name;
        this.owner_id = owner_id;
        this.owner_name = owner_name;
        this.startBid = startBid;
        this.finalBid = finalBid;
    }

    public String getOwner_name() {
        return owner_name;
    }

    public void setOwner_name(String owner_name) {
        this.owner_name = owner_name;
    }

    public Item() {
    }

    public String getId() {
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

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public Double getStartBid() {
        return startBid;
    }

    public void setStartBid(Double startBid) {
        this.startBid = startBid;
    }

    public Double getFinalBid() {
        return finalBid;
    }

    public void setFinalBid(Double finalBid) {
        this.finalBid = finalBid;
    }

    public String getWinningBid() {
        return winningBid;
    }

    public void setWinningBid(String winningBid) {
        this.winningBid = winningBid;
    }

    public ArrayList<Bid> getBids() {
        return bids;
    }

    public void setBids(ArrayList<Bid> bids) {
        this.bids = bids;
    }

}

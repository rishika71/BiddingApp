package com.example.biddingapp.adapter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.biddingapp.R;
import com.example.biddingapp.databinding.AuctionItemBinding;
import com.example.biddingapp.databinding.HistoryItemBinding;
import com.example.biddingapp.databinding.YouritemviewBinding;
import com.example.biddingapp.models.History;
import com.example.biddingapp.models.Item;
import com.example.biddingapp.models.Utils;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.UViewHolder> {

    ArrayList<History> details;

    HistoryItemBinding binding;

    ViewGroup parent;

    public HistoryAdapter(ArrayList<History> details) {
        this.details = details;
    }

    @NonNull
    @Override
    public UViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = HistoryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        this.parent = parent;
        return new UViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UViewHolder holder, int position) {

        History history = details.get(position);

        binding.textView17.setText(history.getItem());
        binding.textView18.setText("Seller: " + history.getSeller_name());
        binding.textView19.setText("Date: " + Utils.getPrettyTime(history.getDate()));
        binding.textView20.setText("Price: $" + history.getPrice());

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.details.size();
    }


    public static class UViewHolder extends RecyclerView.ViewHolder {

        HistoryItemBinding binding;

        public UViewHolder(@NonNull HistoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

}

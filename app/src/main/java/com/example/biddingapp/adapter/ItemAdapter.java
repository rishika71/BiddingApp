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
import com.example.biddingapp.models.Item;
import com.example.biddingapp.models.Utils;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.UViewHolder> {

    ArrayList<Item> items;

    AuctionItemBinding binding;

    ViewGroup parent;

    public ItemAdapter(ArrayList<Item> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public UViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = AuctionItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        this.parent = parent;
        return new UViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UViewHolder holder, int position) {

        Item item = items.get(position);

        binding.textView3.setText(item.getName());
        binding.textView4.setText("Owner: " + item.getOwner_name());
        binding.textView5.setText("Start: $" + item.getStartBid());
        binding.textView6.setText("Final: $" + item.getFinalBid());

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Utils.DB_AUCTION, item);
                Navigation.findNavController(holder.itemView).navigate(R.id.action_tradingFragment_to_itemViewFragment, bundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }


    public static class UViewHolder extends RecyclerView.ViewHolder {

        AuctionItemBinding binding;

        public UViewHolder(@NonNull AuctionItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

}

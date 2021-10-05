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
import com.example.biddingapp.databinding.YouritemviewBinding;
import com.example.biddingapp.models.Item;
import com.example.biddingapp.models.Utils;

import java.util.ArrayList;

public class YourItemAdapter extends RecyclerView.Adapter<YourItemAdapter.UViewHolder> {

    ArrayList<Item> items;

    YouritemviewBinding binding;

    ViewGroup parent;

    public YourItemAdapter(ArrayList<Item> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public UViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = YouritemviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        this.parent = parent;
        return new UViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UViewHolder holder, int position) {

        Item item = items.get(position);

        binding.textView9.setText(item.getName());
        binding.textView13.setText(item.getBids().size() + " Bids");
        binding.textView11.setText(Utils.getPrettyTime(item.getCreated_at()));
        binding.textView14.setText("Final Bid: $" + item.getFinalBid());

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Utils.DB_AUCTION, item);
                Navigation.findNavController(holder.itemView).navigate(R.id.action_myItemsFragment_to_ownItemViewFragment, bundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }


    public static class UViewHolder extends RecyclerView.ViewHolder {

        YouritemviewBinding binding;

        public UViewHolder(@NonNull YouritemviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

}

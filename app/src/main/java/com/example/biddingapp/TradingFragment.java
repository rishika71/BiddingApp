package com.example.biddingapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.biddingapp.databinding.FragmentTradingBinding;
import com.google.firebase.auth.FirebaseAuth;

public class TradingFragment extends Fragment {

    FragmentTradingBinding binding;
    NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

      getActivity().setTitle(R.string.trading);

      binding = FragmentTradingBinding.inflate(inflater, container, false);
      View view = binding.getRoot();
      navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView);

        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                navController.navigate(R.id.action_tradingFragment_to_loginFragment);
            }
        });

        return view;
    }
}
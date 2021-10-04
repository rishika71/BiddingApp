package com.example.biddingapp.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.biddingapp.R;
import com.example.biddingapp.databinding.FragmentHistoryBinding;
import com.example.biddingapp.databinding.FragmentUserProfileBinding;
import com.example.biddingapp.models.User;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;


public class HistoryFragment extends Fragment {

    FragmentHistoryBinding binding;

    NavController navController;

    FirebaseFunctions mFunctions;

    IHistory am;

    User user;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IHistory) {
            am = (IHistory) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("History");

        mFunctions = FirebaseFunctions.getInstance();

        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        user = am.getUser();

        binding.bottomNavigation.setSelectedItemId(R.id.history);
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bids:
                        navController.navigate(R.id.action_historyFragment_to_tradingFragment);
                        return true;
                    case R.id.history:
                        return true;
                    case R.id.profileIcons:
                        navController.navigate(R.id.action_historyFragment_to_userProfileFragment);
                        return true;
                    case R.id.logOutIcons:
                        FirebaseAuth.getInstance().signOut();
                        navController.navigate(R.id.action_historyFragment_to_loginFragment);
                        return true;

                }
                return false;
            }
        });

        return view;
    }

    public interface IHistory{

        User getUser();

    }

}
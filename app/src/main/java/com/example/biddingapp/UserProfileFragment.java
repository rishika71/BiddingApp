package com.example.biddingapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.biddingapp.databinding.FragmentUserProfileBinding;
import com.example.biddingapp.models.User;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;


public class UserProfileFragment extends Fragment {

    FragmentUserProfileBinding binding;
    NavController navController;
    IUserProfile am;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IUserProfile) {
            am = (IUserProfile) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(R.string.userProfile);

        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        User user = am.getUser();
        Log.d("demo", "onCreateView: " + user);
        binding.firstnameTextViewId.setText(user.getFirstName());
        binding.lastnameTextViewId.setText(user.getLastName());

        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView);

        binding.bottomNavigation.setSelectedItemId(R.id.profileIcons);
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        navController.navigate(R.id.action_userProfileFragment_to_tradingFragment);
                        return true;
                    case R.id.profileIcons:
                        return true;
                    case R.id.logOutIcons:
                        FirebaseAuth.getInstance().signOut();
                        navController.navigate(R.id.action_userProfileFragment_to_loginFragment);
                        return true;

                }
                return false;
            }
        });

        return view;

    }

    interface IUserProfile {

        User getUser();
        void setUser(User user);
    }
}
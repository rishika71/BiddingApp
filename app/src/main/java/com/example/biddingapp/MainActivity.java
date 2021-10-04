package com.example.biddingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;


import com.example.biddingapp.databinding.ActivityMainBinding;
import com.example.biddingapp.models.User;

public class MainActivity extends AppCompatActivity implements LoginFragment.ILogin, CreateNewAccountFragment.IRegister, UserProfileFragment.IUserProfile {

    private ActivityMainBinding binding;
    User user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
  }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
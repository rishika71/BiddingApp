package com.example.biddingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;


import com.example.biddingapp.databinding.ActivityMainBinding;
import com.example.biddingapp.fragment.CreateNewAccountFragment;
import com.example.biddingapp.fragment.HistoryFragment;
import com.example.biddingapp.fragment.ItemViewFragment;
import com.example.biddingapp.fragment.LoginFragment;
import com.example.biddingapp.fragment.MyItemsFragment;
import com.example.biddingapp.fragment.OwnItemViewFragment;
import com.example.biddingapp.fragment.PostItemFragment;
import com.example.biddingapp.fragment.TradingFragment;
import com.example.biddingapp.fragment.UserProfileFragment;
import com.example.biddingapp.models.User;

public class MainActivity extends AppCompatActivity implements LoginFragment.ILogin, MyItemsFragment.IMyItems, OwnItemViewFragment.IOwnItemView, CreateNewAccountFragment.IRegister, UserProfileFragment.IUserProfile, HistoryFragment.IHistory, TradingFragment.ITrading, PostItemFragment.IPostItem, ItemViewFragment.IItemView {

    private ActivityMainBinding binding;
    User user = null;

    ProgressDialog dialog;

    public void alert(String alert) {
        runOnUiThread(() -> new AlertDialog.Builder(this)
                .setTitle(R.string.info)
                .setMessage(alert)
                .setPositiveButton(R.string.okay, null)
                .show());
    }

    public void toggleDialog(boolean show) {
        toggleDialog(show, null);
    }

    public void toggleDialog(boolean show, String msg) {
        if (show) {
            dialog = new ProgressDialog(this);
            if (msg == null)
                dialog.setMessage(getString(R.string.loading));
            else
                dialog.setMessage(msg);
            dialog.setCancelable(false);
            dialog.show();
        } else {
            dialog.dismiss();
        }
    }

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
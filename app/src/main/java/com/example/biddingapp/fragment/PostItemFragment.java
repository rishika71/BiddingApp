package com.example.biddingapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.biddingapp.R;
import com.example.biddingapp.databinding.FragmentHistoryBinding;
import com.example.biddingapp.databinding.FragmentPostItemBinding;
import com.example.biddingapp.models.User;
import com.google.firebase.functions.FirebaseFunctions;


public class PostItemFragment extends Fragment {

    FragmentPostItemBinding binding;

    FirebaseFunctions mFunctions;

    User user;

    IPostItem am;

    public interface IPostItem{

        User getUser();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("New Item");

        mFunctions = FirebaseFunctions.getInstance();

        binding = FragmentPostItemBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        user = am.getUser();
        return view;
    }

}
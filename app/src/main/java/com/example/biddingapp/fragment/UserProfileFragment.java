package com.example.biddingapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.biddingapp.R;
import com.example.biddingapp.databinding.FragmentUserProfileBinding;
import com.example.biddingapp.models.User;
import com.example.biddingapp.models.Utils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;


public class UserProfileFragment extends Fragment {

    FragmentUserProfileBinding binding;
    NavController navController;
    IUserProfile am;
    User user;
    FirebaseFunctions mFunctions;

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

        mFunctions = FirebaseFunctions.getInstance();

        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        user = am.getUser();

        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView);

        binding.firstnameTextViewId.setText(user.getFirstname());
        binding.lastnameTextViewId.setText(user.getLastname());
        binding.lastnameTextViewId2.setText(user.getEmail());
        binding.lastnameTextViewId3.setText(user.getPrettyBalance());

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String money = binding.editTextTextPersonName.getText().toString();
                if(money.equals("")){
                    Toast.makeText(getContext(), "Please enter money to add!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Double fmoney = Utils.parseMoney(money);
                if(fmoney == null){
                    Toast.makeText(getContext(), "Please enter valid money value!", Toast.LENGTH_SHORT).show();
                    return;
                }
                addMoney(fmoney);

            }
        });

        binding.button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_userProfileFragment_to_myItemsFragment);
            }
        });

        binding.bottomNavigation.setSelectedItemId(R.id.profileIcons);
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bids:
                        navController.navigate(R.id.action_userProfileFragment_to_tradingFragment);
                        return true;
                    case R.id.history:
                        navController.navigate(R.id.action_userProfileFragment_to_historyFragment);
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

    public void addMoney(Double amount){
        HashMap<String, Object> data = new HashMap<>();

        data.put("currentbalance", amount);

        am.toggleDialog(true);

        mFunctions
            .getHttpsCallable("addMoney")
            .call(data).continueWith(new Continuation<HttpsCallableResult, Object>() {
            @Override
            public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                return null;
            }
        }).addOnCompleteListener(new OnCompleteListener<Object>() {
            @Override
            public void onComplete(@NonNull Task<Object> task) {
                if(!task.isSuccessful()){
                    task.getException().printStackTrace();
                    return;
                }
                am.toggleDialog(false);
                user.addCurrentbalance(amount);
                binding.editTextTextPersonName.setText("");
                binding.lastnameTextViewId3.setText(user.getPrettyBalance());
            }
        });
    }

    public interface IUserProfile {

        User getUser();

        void toggleDialog(boolean show);

    }
}
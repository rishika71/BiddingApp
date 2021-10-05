package com.example.biddingapp.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.biddingapp.R;
import com.example.biddingapp.databinding.FragmentHistoryBinding;
import com.example.biddingapp.databinding.FragmentPostItemBinding;
import com.example.biddingapp.models.Item;
import com.example.biddingapp.models.User;
import com.example.biddingapp.models.Utils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;


public class PostItemFragment extends Fragment {

    FragmentPostItemBinding binding;

    FirebaseFunctions mFunctions;

    User user;

    IPostItem am;

    NavController navController;

    public interface IPostItem{

        User getUser();

        void toggleDialog(boolean show);

        void alert(String msg);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IPostItem) {
            am = (IPostItem) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("New Item");

        mFunctions = FirebaseFunctions.getInstance();

        binding = FragmentPostItemBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        user = am.getUser();

        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView);

        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = binding.editTextTextPersonName2.getText().toString();
                String startBid = binding.editTextTextPersonName3.getText().toString();
                String finalBid = binding.editTextTextPersonName4.getText().toString();
                if(name.equals("") || startBid.equals("") || finalBid.equals("")){
                    Toast.makeText(getContext(), "Please enter all the values!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Double fstartBid = Utils.parseMoney(startBid);
                Double ffinalBid = Utils.parseMoney(finalBid);
                if(fstartBid == null || ffinalBid == null){
                    Toast.makeText(getContext(), "Please enter valid values for item!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Item item = new Item(name, user.getId(), user.getDisplayName(), fstartBid, ffinalBid);
                postItem(item);
            }
        });

        return view;
    }

    private void postItem(Item item){
        HashMap<String, Object> mapping = new HashMap<>();
        mapping.put("name", item.getName());
        mapping.put("owner_id", item.getOwner_id());
        mapping.put("owner_name", item.getOwner_name());
        mapping.put("created_at", item.getCreated_at());
        mapping.put("startBid", item.getStartBid());
        mapping.put("finalBid", item.getFinalBid());

        am.toggleDialog(true);

        mFunctions.getHttpsCallable("postNewItem")
                .call(mapping)
                .continueWith(new Continuation<HttpsCallableResult, Object>() {
                    @Override
                    public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        return task.getResult().getData();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Object>() {
                @Override
                public void onComplete(@NonNull Task<Object> task) {
                    if(!task.isSuccessful()){
                        task.getException().printStackTrace();
                        return;
                    }

                    am.toggleDialog(false);
                    am.alert("Item Posted!");
                    navController.popBackStack();
                }
        });
    }

}
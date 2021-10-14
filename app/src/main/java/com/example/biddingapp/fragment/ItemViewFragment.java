package com.example.biddingapp.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.biddingapp.R;
import com.example.biddingapp.databinding.FragmentHistoryBinding;
import com.example.biddingapp.databinding.FragmentItemViewBinding;
import com.example.biddingapp.models.Bid;
import com.example.biddingapp.models.Item;
import com.example.biddingapp.models.User;
import com.example.biddingapp.models.Utils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

public class ItemViewFragment extends Fragment {

    FragmentItemViewBinding binding;

    FirebaseFunctions mFunctions;

    NavController navController;

    IItemView am;

    FirebaseFirestore db;

    User user;

    Bid winBid;

    Item item;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IItemView) {
            am = (IItemView) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            item = (Item) getArguments().getSerializable(Utils.DB_AUCTION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Item View");

        mFunctions = FirebaseFunctions.getInstance();

        binding = FragmentItemViewBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView);

        user = am.getUser();

        if(item.getOwner_id().equals(user.getId())){
            binding.button3.setEnabled(false);
            binding.button4.setEnabled(false);
            binding.editTextTextPersonName5.setEnabled(false);
        }

        db = FirebaseFirestore.getInstance();

        am.toggleDialog(true);
        db.collection(Utils.DB_AUCTION).document(item.getId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (value == null) {
                    return;
                }
                am.toggleDialog(false);

                item = value.toObject(Item.class);
                if(item == null) return;

                item.setId(value.getId());

                binding.textView7.setText(item.getName());
                binding.txtview.setText("Owner - " + item.getOwner_name());
                binding.textView10.setText("Created - " + Utils.getPrettyTime(item.getCreated_at()));
                binding.textView12.setText("Starting Bid - $" + item.getStartBid());
                binding.textView23.setText("Min Final Bid - $" + item.getFinalBid());

                winBid = item.getWinBid();
                if(winBid != null)
                    binding.textView22.setText("Winning Bid - " + winBid.getBidder_name() + " - $" + winBid.getAmount());
                else
                    binding.textView22.setText("Winning Bid - None");

            }
        });

        binding.button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String bid = binding.editTextTextPersonName5.getText().toString();
                if(bid.equals("")){
                    Toast.makeText(getContext(), "Please enter bid amount!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Double fbid = Utils.parseMoney(bid);
                if(fbid == null){
                    Toast.makeText(getContext(), "Please enter valid value for bid!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(fbid < item.getStartBid()){
                    Toast.makeText(getContext(), "Please enter value higher than the start bid!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(winBid != null && fbid <= winBid.getAmount()){
                    Toast.makeText(getContext(), "Please enter higher bid than the win bid!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(winBid != null && winBid.getBidder_id().equals(user.getId())){
                    Toast.makeText(getContext(), "You already have the win bid!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(fbid + 1 > user.getCurrentbalance()){
                    Toast.makeText(getContext(), "Insufficient funds!", Toast.LENGTH_SHORT).show();
                    return;
                }
                bidOnItem(fbid);
            }
        });

        binding.button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(winBid == null || !winBid.getBidder_id().equals(user.getId())){
                    Toast.makeText(getContext(), "You're not the winning bid!", Toast.LENGTH_SHORT).show();
                    return;
                }
                cancelBid();
            }
        });

        return view;
    }

    private void cancelBid(){
        HashMap<String, Object> data = new HashMap<>();
        data.put("itemId", item.getId());
        data.put("bidder_id", user.getId());

        am.toggleDialog(true);
        mFunctions.getHttpsCallable("cancelBid").call(data).continueWith(new Continuation<HttpsCallableResult, Object>() {
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
                am.alert("Winning bid Cancelled!");
                navController.popBackStack();
            }
        });
    }

    private void bidOnItem(Double bid){
        HashMap<String, Object> data = new HashMap<>();
        data.put("id", item.getNewBidId());
        data.put("itemId", item.getId());
        data.put("amount", bid);
        data.put("noti_token", user.getNoti_token());
        data.put("bidder_id", user.getId());
        data.put("bidder_name", user.getDisplayName());

        am.toggleDialog(true);
        mFunctions.getHttpsCallable("bidOnItem").call(data).continueWith(new Continuation<HttpsCallableResult, Object>() {
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

                am.alert("Bid Placed!");

                navController.popBackStack();
            }
        });
    }

    public interface IItemView{

        User getUser();

        void alert(String msg);

        void toggleDialog(boolean show);

    }

}
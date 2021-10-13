package com.example.biddingapp.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.biddingapp.R;
import com.example.biddingapp.databinding.FragmentHistoryBinding;
import com.example.biddingapp.databinding.FragmentOwnItemViewBinding;
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

public class OwnItemViewFragment extends Fragment {

    FragmentOwnItemViewBinding binding;

    NavController navController;

    FirebaseFunctions mFunctions;

    IOwnItemView am;

    FirebaseFirestore db;

    User user;

    Bid winBid;

    Item item;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IOwnItemView) {
            am = (IOwnItemView) context;
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
        getActivity().setTitle("Item " + item.getName());

        mFunctions = FirebaseFunctions.getInstance();

        binding = FragmentOwnItemViewBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView);

        user = am.getUser();

        if(item.getWinningBid() == null)
            binding.button4.setEnabled(false);

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
                   binding.txtview.setText("Owner - You");
                   binding.textView10.setText("Created - " + Utils.getPrettyTime(item.getCreated_at()));
                   binding.textView12.setText("Starting Bid - $" + item.getStartBid());
                   binding.textView16.setText("Min Final Bid - $" + item.getFinalBid());

                   winBid = item.getWinBid();
                   if(winBid != null)
                       binding.textView21.setText("Winning Bid - " + winBid.getBidder_name() + " - $" + winBid.getAmount());
                   else
                       binding.textView21.setText("Winning Bid - None");

               }
           });

        binding.button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelItem();
            }
        });

        binding.button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(winBid.getAmount() < item.getFinalBid()){
                    Toast.makeText(getContext(), "Bid has not reached minimum final bid amount yet!", Toast.LENGTH_SHORT).show();
                    return;
                }
                acceptBidOnItem();
            }
        });

        return view;
    }

    private void cancelItem(){
        HashMap<String, Object> data = new HashMap<>();
        data.put("itemId", item.getId());

        am.toggleDialog(true);

        mFunctions
                .getHttpsCallable("cancelItem")
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
                am.alert("Item removed!");
                navController.popBackStack();
            }
        });
    }

    private void acceptBidOnItem(){
        HashMap<String, Object> data = new HashMap<>();
        data.put("itemId", item.getId());
        data.put("owner_name", user.getDisplayName());

        am.toggleDialog(true);

        mFunctions
                .getHttpsCallable("acceptBidOnItem")
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
                am.alert("Bid accepted! Item is now off the list!");
                navController.popBackStack();
            }
        });
    }

    public interface IOwnItemView{

        User getUser();

        void alert(String msg);

        void toggleDialog(boolean show);

    }

}
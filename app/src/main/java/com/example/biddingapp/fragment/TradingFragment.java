package com.example.biddingapp.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.biddingapp.R;
import com.example.biddingapp.adapter.ItemAdapter;
import com.example.biddingapp.databinding.FragmentTradingBinding;
import com.example.biddingapp.models.Item;
import com.example.biddingapp.models.User;
import com.example.biddingapp.models.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

public class TradingFragment extends Fragment {

    FragmentTradingBinding binding;
    NavController navController;
    User user;
    ITrading am;
    ArrayList<Item> items = new ArrayList<>();
    ItemAdapter itemAdapter;

    FirebaseFirestore firestore;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ITrading) {
            am = (ITrading) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle("Ongoing Bids");

        user = am.getUser();

        binding = FragmentTradingBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView);

        firestore = FirebaseFirestore.getInstance();

        binding.auctionView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        binding.auctionView.setLayoutManager(llm);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.auctionView.getContext(),
                llm.getOrientation());
        binding.auctionView.addItemDecoration(dividerItemDecoration);

        am.toggleDialog(true);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                am.toggleDialog(false);
                if (!task.isSuccessful()) {
                    return;
                }
                user.setNoti_token(task.getResult());
                firestore.collection(Utils.DB_PROFILE).document(user.getId()).update("noti_token", task.getResult());
            }
        });

        firestore.collection(Utils.DB_AUCTION).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (value == null) {
                    return;
                }

                items.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Item item = doc.toObject(Item.class);
                    item.setId(doc.getId());
                    items.add(item);
                }

                binding.auctionView.setAdapter(new ItemAdapter(items));
            }
        });

        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_tradingFragment_to_postItemFragment);
            }
        });

        binding.bottomNavigation.setSelectedItemId(R.id.home);
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bids:
                        return true;
                    case R.id.history:
                        navController.navigate(R.id.action_tradingFragment_to_historyFragment);
                        return true;
                    case R.id.profileIcons:
                        navController.navigate(R.id.action_tradingFragment_to_userProfileFragment);
                        return true;
                    case R.id.logOutIcons:
                        FirebaseAuth.getInstance().signOut();
                        navController.navigate(R.id.action_tradingFragment_to_loginFragment);
                        return true;

                }
                return false;
            }
        });

        return view;
    }

    public interface ITrading{

        User getUser();

        void toggleDialog(boolean show);

    }

}
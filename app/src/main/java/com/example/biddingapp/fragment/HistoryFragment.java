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

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.biddingapp.MainActivity;
import com.example.biddingapp.R;
import com.example.biddingapp.adapter.HistoryAdapter;
import com.example.biddingapp.adapter.ItemAdapter;
import com.example.biddingapp.databinding.FragmentHistoryBinding;
import com.example.biddingapp.models.Item;
import com.example.biddingapp.models.Transaction;
import com.example.biddingapp.models.User;
import com.example.biddingapp.models.Utils;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;


public class HistoryFragment extends Fragment {

    FragmentHistoryBinding binding;

    NavController navController;

    FirebaseFunctions mFunctions;

    FirebaseFirestore db;

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
        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView);

        user = am.getUser();

        db = FirebaseFirestore.getInstance();

        binding.historyView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        binding.historyView.setLayoutManager(llm);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.historyView.getContext(),
                llm.getOrientation());
        binding.historyView.addItemDecoration(dividerItemDecoration);

        db.collection(Utils.DB_HISTORY).document(user.getId()).collection(Utils.DB_TRANSACTION).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (value == null) {
                    return;
                }

                ArrayList<Transaction> details = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    Transaction transaction = doc.toObject(Transaction.class);
                    transaction.setId(doc.getId());
                    details.add(transaction);
                }

                binding.historyView.setAdapter(new HistoryAdapter(details));
            }
        });

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
                        am.signout();
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

        void signout();

    }

}
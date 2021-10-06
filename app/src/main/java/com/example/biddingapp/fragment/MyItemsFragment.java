package com.example.biddingapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.biddingapp.R;
import com.example.biddingapp.adapter.ItemAdapter;
import com.example.biddingapp.adapter.YourItemAdapter;
import com.example.biddingapp.databinding.FragmentMyItemsBinding;
import com.example.biddingapp.models.Item;
import com.example.biddingapp.models.User;
import com.example.biddingapp.models.Utils;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;

public class MyItemsFragment extends Fragment {

    FragmentMyItemsBinding binding;

    NavController navController;

    FirebaseFunctions mFunctions;

    FirebaseFirestore firestore;

    IMyItems am;

    User user;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IMyItems) {
            am = (IMyItems) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Profile");

        mFunctions = FirebaseFunctions.getInstance();

        binding = FragmentMyItemsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        firestore = FirebaseFirestore.getInstance();
        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView);

        user = am.getUser();

        binding.youritemsview.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        binding.youritemsview.setLayoutManager(llm);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.youritemsview.getContext(),
                llm.getOrientation());
        binding.youritemsview.addItemDecoration(dividerItemDecoration);

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

                ArrayList<Item> items = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    Item item = doc.toObject(Item.class);
                    item.setId(doc.getId());
                    if(item.getOwner_id().equals(user.getId())) items.add(item);
                }

                binding.youritemsview.setAdapter(new YourItemAdapter(items));
            }
        });

        return view;
    }

    public interface IMyItems{

        User getUser();

    }


}
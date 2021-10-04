package com.example.biddingapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.biddingapp.R;
import com.example.biddingapp.databinding.FragmentCreateNewAccountBinding;
import com.example.biddingapp.models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;


public class CreateNewAccountFragment extends Fragment {

    private FirebaseAuth mAuth;
    FirebaseFunctions mFunctions;
    NavController navController;
    FragmentCreateNewAccountBinding binding;
    IRegister am;
    String email, password, firstName, lastName;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IRegister) {
            am = (IRegister) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(R.string.createAccount);

        binding = FragmentCreateNewAccountBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        mFunctions = FirebaseFunctions.getInstance();
        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView);

        binding.registerButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firstName = binding.createFragmentFirstNameId.getText().toString();
                lastName = binding.createFragmentLastNameId.getText().toString();
                email = binding.createFragmentEmailId.getText().toString();
                password = binding.createFragmentPasswordId.getText().toString();

                if(firstName.isEmpty()){
                    Toast.makeText(getContext(), getResources().getString(R.string.enterFirstName), Toast.LENGTH_SHORT).show();
                }else if(lastName.isEmpty()){
                    Toast.makeText(getContext(), getResources().getString(R.string.enterLastName), Toast.LENGTH_SHORT).show();
                } else if(email.isEmpty()){
                    Toast.makeText(getContext(), getResources().getString(R.string.enterEmail), Toast.LENGTH_SHORT).show();
                }else if(password.isEmpty()){
                    Toast.makeText(getContext(), getResources().getString(R.string.enterPassword), Toast.LENGTH_SHORT).show();
                }else {
                    mAuth = FirebaseAuth.getInstance();
                    am.toggleDialog(true);
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(!task.isSuccessful()) Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(firstName + " " + lastName).build();
                                FirebaseUser user = mAuth.getCurrentUser();
                                user.updateProfile(profileUpdates);

                                User user_obj = new User(firstName, lastName, email, mAuth.getUid());
                                createUser(user_obj);

                            }
                        });
                }

            }
        });

        binding.cancelButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_createNewAccountFragment_to_loginFragment);
            }
        });

     return view;
    }

    private void createUser(User user) {
        HashMap<String, Object> data = new HashMap<>();

        data.put("firstname", user.getFirstname());
        data.put("lastname", user.getLastname());
        data.put("email", user.getEmail());
        data.put("uid", user.getId());
        data.put("currentbalance", user.getCurrentbalance());

        mFunctions
            .getHttpsCallable("createUserProfile")
            .call(data)
            .continueWith(new Continuation<HttpsCallableResult, Object>() {
                @Override
                public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                    return null;
                }
            })
            .addOnCompleteListener(new OnCompleteListener<Object>() {
                @Override
                public void onComplete(@NonNull Task<Object> task) {
                    if(!task.isSuccessful()){
                        task.getException().printStackTrace();
                        return;
                    }
                    am.toggleDialog(false);
                    am.setUser(user);
                    navController.navigate(R.id.action_createNewAccountFragment_to_tradingFragment);
                }
            });
    }

    public interface IRegister {

        void setUser(User user);

        void toggleDialog(boolean show);

    }
}
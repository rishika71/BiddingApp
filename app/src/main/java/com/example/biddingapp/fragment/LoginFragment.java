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

import com.example.biddingapp.R;
import com.example.biddingapp.databinding.FragmentLoginBinding;
import com.example.biddingapp.models.User;
import com.example.biddingapp.models.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    NavController navController;
    FragmentLoginBinding binding;
    FirebaseUser currentUser;
    ILogin am;

    String email, password;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ILogin) {
            am = (ILogin) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) login();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(R.string.login);

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();

        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView);

        binding.createNewAccountId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_loginFragment_to_createNewAccountFragment);
            }
        });

        binding.loginButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = binding.emailTextFieldId.getText().toString();
                password = binding.passwordTextFieldId.getText().toString();
                if(email.isEmpty()){
                    Toast.makeText(getContext(), getResources().getString(R.string.enterEmail), Toast.LENGTH_SHORT).show();
                }else if(password.isEmpty()){
                    Toast.makeText(getContext(), getResources().getString(R.string.enterPassword), Toast.LENGTH_SHORT).show();
                }else{
                    mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    currentUser = mAuth.getCurrentUser();
                                    login();
                                } else{
                                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                }
            }
        });
        return view;
    }


    public void login() {
        am.toggleDialog(true);
        FirebaseFirestore.getInstance().collection(Utils.DB_PROFILE).document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()){
                    task.getException().printStackTrace();
                    return;
                }

                DocumentSnapshot snapshot = task.getResult();
                User user = snapshot.toObject(User.class);
                user.setId(snapshot.getId());

                am.toggleDialog(false);
                am.setUser(user);
                navController.navigate(R.id.action_loginFragment_to_tradingFragment);
            }
        });
    }


    public interface ILogin {

        void setUser(User user);

        void toggleDialog(boolean show);

    }
}
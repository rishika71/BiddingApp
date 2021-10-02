package com.example.biddingapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.util.Log;
import android.widget.Toast;

import com.example.biddingapp.databinding.FragmentForgotPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class ForgotPasswordFragment extends Fragment {

    final private String TAG = "demo";
    private FirebaseAuth mAuth;

    FragmentForgotPasswordBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(R.string.forgotPassword);

        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);

        View view = binding.getRoot();

        NavController navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView);

        binding.resetPasswordButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth = FirebaseAuth.getInstance();
                String emailAddress = binding.emailTextFieldId.getText().toString();

                if(!emailAddress.isEmpty()){
                    mAuth.sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), getResources().getString(R.string.passwordResetEmail), Toast.LENGTH_LONG).show();
                                        Log.d(TAG, "Email sent.");
                                        Log.d(TAG, emailAddress);
                                        navController.navigate(R.id.action_forgotPasswordFragment_to_loginFragment);
                                    }
                                }
                            });
                }else{
                    getAlertDialogBox(getResources().getString(R.string.enterEmail));
                }

            }
        });

        //....Cancel Button......
        binding.cancelResetPasswordId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_forgotPasswordFragment_to_loginFragment);
            }
        });


        return view;
    }

    public void getAlertDialogBox(String errorMessage){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.errorMessage))
                .setMessage(errorMessage);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();

    }
}
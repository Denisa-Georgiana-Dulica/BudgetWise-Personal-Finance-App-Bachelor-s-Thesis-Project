package com.example.budgetwise.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.budgetwise.ChangeEmailActivity;
import com.example.budgetwise.R;
import com.example.budgetwise.firebase.FirebaseAuthService;
import com.example.budgetwise.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class UserFragment extends Fragment {
    private TextView logOut;
    private TextView changeEmail;

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_user, container, false);
        logOut=view.findViewById(R.id.tvLogout);
        changeEmail=view.findViewById(R.id.tvChangeEmail);
        logOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });
        changeEmail.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ChangeEmailActivity.class);
            startActivity(intent);
        });
        return view;
    }
}
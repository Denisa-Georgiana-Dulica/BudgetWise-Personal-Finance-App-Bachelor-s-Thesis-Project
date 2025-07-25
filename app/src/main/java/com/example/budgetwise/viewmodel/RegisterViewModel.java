package com.example.budgetwise.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.budgetwise.firebase.FirebaseAuthService;
import com.google.firebase.auth.FirebaseUser;

public class RegisterViewModel extends ViewModel {
    private final FirebaseAuthService authService = new FirebaseAuthService();
    private final MutableLiveData<FirebaseUser> registerSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> registerError = new MutableLiveData<>();

    public LiveData<FirebaseUser> getRegisterSuccess() {
        return registerSuccess;
    }

    public LiveData<String> getRegisterError() {
        return registerError;
    }

    public void register(String email, String password, String lastName, String firstName) {
        authService.registerUser(email, password, lastName, firstName, new FirebaseAuthService.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                registerSuccess.postValue(firebaseUser);
            }

            @Override
            public void onFailure(String errorMessage) {
                registerError.postValue(errorMessage);
            }
        });
    }
}

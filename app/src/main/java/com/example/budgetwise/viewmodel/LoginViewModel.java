package com.example.budgetwise.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.budgetwise.firebase.FirebaseAuthService;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends ViewModel {
    private final FirebaseAuthService authService=new FirebaseAuthService();
    private final MutableLiveData<FirebaseUser> loginSuccess=new MutableLiveData<>();
    private final MutableLiveData<String> loginError=new MutableLiveData<>();

    public LiveData<FirebaseUser> getLoginSuccess()
    {
        return loginSuccess;
    }

    public LiveData<String> getLoginError()
    {
        return loginError;
    }

    public void login(String email, String password)
    {
        authService.loginUser(email, password, new FirebaseAuthService.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {//ViewModel updates loginSuccess
                //postValue= being in a Firebase callback (which works in another thread), you need to use postValue()
                loginSuccess.postValue(firebaseUser);//if the user has successfully logged in then announce all UI components that listen for loginSuccess (to all observers, i.e. .observe(...) methods).
            }

            @Override
            public void onFailure(String errorMessage) {//if the login failed, the ViewModel updates loginFailure
                loginError.postValue(errorMessage);
            }
        });
    }
}

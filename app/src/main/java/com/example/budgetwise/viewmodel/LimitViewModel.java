package com.example.budgetwise.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.budgetwise.classes.FinancialAccount;
import com.example.budgetwise.classes.Limit;
import com.example.budgetwise.firebase.FirebaseAccountRepository;
import com.example.budgetwise.firebase.FirebaseLimitRepository;

import java.util.ArrayList;
import java.util.List;

public class LimitViewModel extends ViewModel {
    private final FirebaseLimitRepository firebaseLimitRepository = new FirebaseLimitRepository();
    private final MutableLiveData<List<Limit>> limitsList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> success = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<Limit>> getLimits() {
        return limitsList;
    }

    public LiveData<Boolean> getSuccess() {
        return success;
    }

    public LiveData<String> getError() {
        return error;
    }
    public void loadLimits() {
        firebaseLimitRepository.getAllLimits(new FirebaseLimitRepository.RepositoryCallbackList() {
            @Override
            public void onSuccessList(List<Limit> list) {
                limitsList.postValue(list);
            }

            @Override
            public void onErrorList(String message) {
                error.postValue(message);
            }
        });
    }
    public void addLimit(Limit limit) {
        firebaseLimitRepository.addLimit(limit, new FirebaseLimitRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                success.postValue(true);
                loadLimits();//reloading in ui
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
                success.postValue(false);
            }
        });
    }

    public void updateLimit(Limit limit) {
        success.setValue(null);
        error.setValue(null);
        firebaseLimitRepository.updateLimit(limit, new FirebaseLimitRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                success.postValue(true);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
                success.postValue(false);
            }
        });
    }

    public void deleteLimit(String idLimit) {
        firebaseLimitRepository.deleteLimit(idLimit, new FirebaseLimitRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                success.postValue(true);
                loadLimits();//reloading in ui
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
                success.postValue(false);
            }
        });
    }

    public void reloadAllLimitsFromFirebase() {
        firebaseLimitRepository.getAllLimits(new FirebaseLimitRepository.RepositoryCallbackList() {

            @Override
            public void onSuccessList(List<Limit> list) {
                limitsList.setValue(new ArrayList<>(list));
            }

            @Override
            public void onErrorList(String message) {
                Log.e("LimitVM", "Failed to reload limits: " + message);
            }
        });
    }

}

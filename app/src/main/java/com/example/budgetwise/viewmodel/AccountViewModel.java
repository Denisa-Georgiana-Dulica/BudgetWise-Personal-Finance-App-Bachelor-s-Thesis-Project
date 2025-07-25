package com.example.budgetwise.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.budgetwise.classes.FinancialAccount;
import com.example.budgetwise.firebase.FirebaseAccountRepository;

import java.util.ArrayList;
import java.util.List;

public class AccountViewModel extends ViewModel {
    private final FirebaseAccountRepository firebaseAccountRepository=new FirebaseAccountRepository();
    private final MutableLiveData<Boolean> operationSuccess=new MutableLiveData<>();
    private final MutableLiveData<String> operationError=new MutableLiveData<>();
    private final MutableLiveData<List<FinancialAccount>> listAccounts=new MutableLiveData<>();

    private final MutableLiveData<FinancialAccount> accountById = new MutableLiveData<>();

    public AccountViewModel() {
        addDefaultAccount(new Runnable() {
            @Override
            public void run() {
                getAllAccounts();
            }
        });
    }
    public LiveData<FinancialAccount> getAccountByIdLiveData() {
        return accountById;
    }

    public LiveData<Boolean> getSuccess()
    {
        return operationSuccess;
    }

    public LiveData<String> getError()
    {
        return operationError;
    }

    public LiveData<List<FinancialAccount>> getList()
    {
        return listAccounts;
    }

    public void getAllAccounts()
    {
        firebaseAccountRepository.getAllAccounts(new FirebaseAccountRepository.RepositoryCallbackList() {
            @Override
            public void onSuccessList(List<FinancialAccount> list) {
                listAccounts.postValue(list);
            }

            @Override
            public void onErrorList(String message) {
                operationError.postValue(message);
            }
        });
    }

    public void addAccount(FinancialAccount financialAccount)
    {
        firebaseAccountRepository.addAccount(financialAccount, new FirebaseAccountRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                operationSuccess.postValue(true);
            }

            @Override
            public void onError(String message) {
                operationError.postValue(message);
                operationSuccess.postValue(false);
            }
        });
    }

    public void updateAccount(FinancialAccount financialAccount)
    {
        firebaseAccountRepository.updateAccount(financialAccount, new FirebaseAccountRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                operationSuccess.postValue(true);
            }

            @Override
            public void onError(String message) {
                operationError.postValue(message);
                operationSuccess.postValue(false);
            }
        });
    }

    public void deleteAccount(String financialAccountId)
    {
        firebaseAccountRepository.deleteAccount(financialAccountId, new FirebaseAccountRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                operationSuccess.postValue(true);
            }

            @Override
            public void onError(String message) {
                operationError.postValue(message);
                operationSuccess.postValue(false);
            }
        });
    }

    public void addDefaultAccount(Runnable onComplete) {
        firebaseAccountRepository.addDefaultAccount(onComplete);
    }

    public void loadAccountById(String id) {
        firebaseAccountRepository.getAccountById(id, new FirebaseAccountRepository.RepositoryCallbackSingle() {
            @Override
            public void onSuccess(FinancialAccount account) {
                accountById.postValue(account);
            }

            @Override
            public void onError(String message) {
                operationError.postValue(message);
            }
        });
    }

    public void reloadAllAccountsFromFirebase() {
        firebaseAccountRepository.getAllAccounts(new FirebaseAccountRepository.RepositoryCallbackList() {

            @Override
            public void onSuccessList(List<FinancialAccount> list) {
                listAccounts.setValue(new ArrayList<>(list));
            }

            @Override
            public void onErrorList(String message) {
                Log.e("AccountVM", "Failed to reload accounts: " + message);
            }
        });
    }



}

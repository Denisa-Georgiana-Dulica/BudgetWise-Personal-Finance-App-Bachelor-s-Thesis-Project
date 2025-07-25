package com.example.budgetwise.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.budgetwise.classes.Transaction;
import com.example.budgetwise.firebase.FirebaseTransactionRepository;

import java.util.Calendar;
import java.util.List;

public class TransactionViewModel extends ViewModel {
    private final FirebaseTransactionRepository firebaseTransactionRepository=new FirebaseTransactionRepository();
    private final MutableLiveData<Boolean> operationSuccess=new MutableLiveData<>();
    private final MutableLiveData<String> operationFailure=new MutableLiveData<>();
    private final MutableLiveData<List<Transaction>> transactionsLiveData=new MutableLiveData<>();
    //used to update the list when adding scheduled transactions
    private final MutableLiveData<Boolean> refreshNeeded = new MutableLiveData<>(false);

    public LiveData<List<Transaction>> getTransactions()
    {
        return transactionsLiveData;
    }
    public LiveData<Boolean> getRefreshNeeded() {
        return refreshNeeded;
    }

    public void triggerRefresh() {
        refreshNeeded.setValue(true);
    }

    public void resetRefresh() {
        refreshNeeded.setValue(false);
    }

    public void addTransaction(Transaction transaction)
    {
        firebaseTransactionRepository.addTransaction(transaction, new FirebaseTransactionRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                operationSuccess.postValue(true);
            }

            @Override
            public void onError(String userIsNotAuthenticated) {
                operationFailure.postValue(userIsNotAuthenticated);
                operationSuccess.postValue(false);
            }
        });
    }

    public void updateTransaction(Transaction transaction)
    {
        firebaseTransactionRepository.updateTransaction(transaction, new FirebaseTransactionRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                operationSuccess.postValue(true);
            }

            @Override
            public void onError(String userIsNotAuthenticated) {
                operationFailure.postValue(userIsNotAuthenticated);
                operationSuccess.postValue(false);
            }
        });
    }

    public void updateTransactionWithCallback(Transaction transaction, FirebaseTransactionRepository.RepositoryCallback callback)
    {
        firebaseTransactionRepository.updateTransaction(transaction, callback);
    }

    public void deleteTransaction(String transactionId,FirebaseTransactionRepository.RepositoryCallback callback)
    {
        firebaseTransactionRepository.deleteTransaction(transactionId, callback);
    }

    public void loadAllTransactions()
    {
        firebaseTransactionRepository.getAllTransactionsForUser(new FirebaseTransactionRepository.RepositoryCallbackList() {
            @Override
            public void onSuccessList(List<Transaction> transactionList) {
                transactionsLiveData.postValue(transactionList);
            }

            @Override
            public void onErrorList(String message) {
                operationFailure.postValue(message);
            }
        });
    }

    public void loadAllTransactionsR(Runnable onComplete) {
        firebaseTransactionRepository.getAllTransactionsForUser(new FirebaseTransactionRepository.RepositoryCallbackList() {
            @Override
            public void onSuccessList(List<Transaction> transactionList) {
                transactionsLiveData.postValue(transactionList);
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onErrorList(String message) {
                operationFailure.postValue(message);
            }
        });
    }

}

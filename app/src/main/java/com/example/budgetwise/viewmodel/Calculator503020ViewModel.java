package com.example.budgetwise.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.budgetwise.classes.Calculator503020;
import com.example.budgetwise.firebase.FirebaseCalculatorRepository;

public class Calculator503020ViewModel extends ViewModel {
    private final FirebaseCalculatorRepository repository=new FirebaseCalculatorRepository();
    private final MutableLiveData<Boolean> succes=new MutableLiveData<>();
    private final MutableLiveData<String> error=new MutableLiveData<>();
    private final MutableLiveData<Calculator503020> calculatorData = new MutableLiveData<>();

    public LiveData<Boolean> getSuccess()
    {
        return succes;
    }

    public LiveData<String> getError()
    {
        return error;
    }

    public LiveData<Calculator503020> getCalculatorData() {
        return calculatorData;
    }


    public void saveCalculator(Calculator503020 calculator503020)
    {
        repository.saveCalculator(calculator503020, new FirebaseCalculatorRepository.RepositoryCallbackCalc() {
            @Override
            public void onSuccess(Calculator503020 calculator) {
                succes.postValue(true); //postValue - updates the value of the LiveData calculatorData with the new calculator object on a secondary thread (Send the value to the UI thread safely)
                calculatorData.postValue(calculator);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
                succes.postValue(false);
            }
        });
    }

    public void getCalculator()
    {
        repository.getCalculator(new FirebaseCalculatorRepository.RepositoryCallbackCalc() {
            @Override
            public void onSuccess(Calculator503020 calculator) {
                calculatorData.postValue(calculator);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

}

package com.example.budgetwise.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.budgetwise.classes.TransactionCategory;
import com.example.budgetwise.firebase.CategoryRepository;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class CategoryViewModel extends ViewModel {
    private final CategoryRepository categoryRepository=new CategoryRepository();
    private MutableLiveData<Boolean> success=new MutableLiveData<>();
    private MutableLiveData<String> errorMessage=new MutableLiveData<>();
    private MutableLiveData<List<TransactionCategory>> list=new MutableLiveData<>();

    public LiveData<Boolean> getOperationSuccess()
    {
        return success;
    }

    public LiveData<String> getOperationFailure()
    {
        return errorMessage;
    }

    public LiveData<List<TransactionCategory>> getCategories()
    {
        return list;
    }

    public void addDefaultCategories()
    {
        categoryRepository.addDefaultCategories(new CategoryRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                success.postValue(true);
            }

            @Override
            public void onError(String userIsNotAuthenticated) {
                    errorMessage.postValue(userIsNotAuthenticated);
                    success.postValue(false);
            }
        });
    }

    public void addDefaultIfEmpty() {
        categoryRepository.addDefaultIfEmpty(new CategoryRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                success.postValue(true);
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message);
                success.postValue(false);
            }
        });
    }

    public void loadCategories() {
        categoryRepository.loadListCategories(new CategoryRepository.LoadCategoriesCallback() {
            @Override
            public void onCategoriesLoaded(List<TransactionCategory> categories) {
                list.postValue(categories);
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message);
            }
        });
    }
    public String getCategoryNameById(String idCategory) {
        List<TransactionCategory> categories = list.getValue();
        if (categories != null) {
            for (TransactionCategory category : categories) {
                if (category.getIdCategory().equals(idCategory)) {
                    return category.getCategoryName();
                }
            }
        }
        return "Categorie necunoscută";
    }

}

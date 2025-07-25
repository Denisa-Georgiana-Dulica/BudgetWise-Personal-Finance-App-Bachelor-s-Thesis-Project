package com.example.budgetwise.firebase;

import androidx.annotation.NonNull;
import com.example.budgetwise.classes.TransactionCategory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryRepository {
    private DatabaseReference databaseReference;

    public CategoryRepository() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference().child("categories");
    }

    public void addDefaultCategories(RepositoryCallback callback)
    {
        List<TransactionCategory> defaultCategories= Arrays.asList(
                new TransactionCategory(null,"Food/Drinks","food_drinks","EXPENSE","NEED"),
                new TransactionCategory(null,"Ate in town","out_meal","EXPENSE","WANT"),
                new TransactionCategory(null,"Clothes","clothes","EXPENSE","NEED"),
                new TransactionCategory(null,"Footwear","foot_wear","EXPENSE","NEED"),
                new TransactionCategory(null,"Salary","salary","INCOME",null),
                new TransactionCategory(null,"Extra work","extra_money","INCOME",null),
                new TransactionCategory(null,"Fun","fun","EXPENSE","WANT"),
                new TransactionCategory(null,"Scholarship","scholarship","INCOME",null),
                new TransactionCategory(null,"Student dormitory","bed","EXPENSE","NEED"),
                new TransactionCategory(null,"Public transport","bus","EXPENSE","NEED"),
                new TransactionCategory(null,"Business","business","INCOME",null),
                new TransactionCategory(null,"Car","car","EXPENSE","NEED"),
                new TransactionCategory(null,"Extra money","extra","INCOME",null),
                new TransactionCategory(null,"Gift","gift","EXPENSE","WANT"),
                new TransactionCategory(null,"Rent","rent","EXPENSE","NEED"),
                new TransactionCategory(null,"Savings","savings","INCOME","SAVING"),
                new TransactionCategory(null,"Shopping","shopping","EXPENSE","WANT"),
                new TransactionCategory(null, "Sport","sport","EXPENSE","WANT"),
                new TransactionCategory(null,"Unexpected expenses","unexpected","EXPENSE","WANT"),
                new TransactionCategory(null,"Vacation","vacations","EXPENSE","WANT"),
                new TransactionCategory(null,"Health","health","EXPENSE","NEED")
        );

        for(TransactionCategory t:defaultCategories)
        {
            String key=databaseReference.push().getKey();
            t.setIdCategory(key);
            databaseReference.child(key).setValue(t).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    callback.onSuccess();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onError(e.getMessage());
                }
            });
        }
    }

    public interface RepositoryCallback
    {
        void onSuccess();
        void onError(String userIsNotAuthenticated);
    }

    public interface LoadCategoriesCallback {
        void onCategoriesLoaded(List<TransactionCategory> categories);
        void onError(String message);
    }

    public void addDefaultIfEmpty(RepositoryCallback callback)
    {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists())
                {
                    addDefaultCategories(callback);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Error checking categories:" + error.getMessage());
            }
        });
    }

    public void loadListCategories(LoadCategoriesCallback callback)
    {
        //when the category list changes
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<TransactionCategory> categoryList=new ArrayList<>();
                for(DataSnapshot child:snapshot.getChildren())
                {
                    TransactionCategory category=child.getValue(TransactionCategory.class);
                    categoryList.add(category);
                }
                callback.onCategoriesLoaded(categoryList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Error loading categories:" + error.getMessage());
            }
        });
    }
}

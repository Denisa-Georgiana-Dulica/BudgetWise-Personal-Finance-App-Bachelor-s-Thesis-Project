package com.example.budgetwise.firebase;

import androidx.annotation.NonNull;

import com.example.budgetwise.classes.Calculator503020;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseCalculatorRepository {
    private final DatabaseReference databaseReference;
    private final FirebaseAuth firebaseAuth;

    public FirebaseCalculatorRepository() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference("calculator503020");
        this.firebaseAuth= FirebaseAuth.getInstance();
    }

    private String getUserId()
    {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null)
        {
            return user.getUid();
        }else{
            return null;
        }
    }

    public void saveCalculator(Calculator503020 calculator503020,RepositoryCallbackCalc callbackCalc)
    {
        String userId=getUserId();
        if(userId==null)
        {
            callbackCalc.onError("User is not authenticated");
            return;
        }

        databaseReference.child(userId).setValue(calculator503020)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                callbackCalc.onSuccess(calculator503020);
            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callbackCalc.onError(e.getMessage());
                    }
                });
    }

    public void getCalculator(RepositoryCallbackCalc callbackCalc)
    {
        String userId = getUserId();
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Calculator503020 calculator = snapshot.getValue(Calculator503020.class);
                if (calculator != null) {
                    callbackCalc.onSuccess(calculator);
                } else {
                    callbackCalc.onError("No calculator data found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public interface RepositoryCallbackCalc {
        void onSuccess(Calculator503020 calculator);
        void onError(String message);
    }

}

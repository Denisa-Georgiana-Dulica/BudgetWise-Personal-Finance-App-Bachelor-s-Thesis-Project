package com.example.budgetwise.firebase;

import androidx.annotation.NonNull;

import com.example.budgetwise.classes.Limit;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseLimitRepository {
    private final DatabaseReference databaseReference;
    private final FirebaseAuth firebaseAuth;

    public FirebaseLimitRepository() {
        this.databaseReference= FirebaseDatabase.getInstance().getReference("limits");
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    private String getUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public interface RepositoryCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface RepositoryCallbackList {
        void onSuccessList(List<Limit> list);
        void onErrorList(String message);
    }

    public void addLimit(Limit limit,RepositoryCallback callback)
    {
        String userId=getUserId();
        if(userId==null)
        {
            callback.onError("User not authenticated");
            return;
        }
        String limitId = databaseReference.child(userId).push().getKey();
        limit.setIdLimit(limitId);
        databaseReference.child(userId).child(limitId).setValue(limit).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public void getAllLimits(RepositoryCallbackList callback) {
        String userId = getUserId();
        if (userId == null) {
            callback.onErrorList("User not authenticated");
            return;
        }

        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Limit> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Limit limit = child.getValue(Limit.class);
                    if (limit != null) {
                        list.add(limit);
                    }
                }
                callback.onSuccessList(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onErrorList(error.getMessage());
            }
        });
    }

    public void updateLimit(Limit limit, RepositoryCallback callback) {
        String userId = getUserId();
        if (userId == null || limit.getIdLimit() == null) {
            callback.onError("Invalid data");
            return;
        }

        databaseReference.child(userId).child(limit.getIdLimit()).setValue(limit).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public void deleteLimit(String idLimit, RepositoryCallback callback) {
        String userId = getUserId();
        if (userId == null || idLimit == null) {
            callback.onError("Invalid data");
            return;
        }

        databaseReference.child(userId).child(idLimit).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
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

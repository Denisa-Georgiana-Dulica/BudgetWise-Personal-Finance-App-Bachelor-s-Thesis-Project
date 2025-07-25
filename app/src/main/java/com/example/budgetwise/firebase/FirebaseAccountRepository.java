package com.example.budgetwise.firebase;

import androidx.annotation.NonNull;
import com.example.budgetwise.classes.AccountType;
import com.example.budgetwise.classes.FinancialAccount;
import com.example.budgetwise.classes.Transaction;
import com.example.budgetwise.classes.User;
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

public class FirebaseAccountRepository {
    private final DatabaseReference databaseReference;
    private final FirebaseAuth firebaseAuth;

    public FirebaseAccountRepository() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference("accounts");
        this.firebaseAuth=FirebaseAuth.getInstance();
    }

    public String getUserId()
    {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public interface RepositoryCallback
    {
        void onSuccess();
        void onError(String message);
    }

    public void addAccount(FinancialAccount financialAccount,RepositoryCallback callback)
    {
        String userId=getUserId();
        if(userId==null)
        {
            callback.onError("User is not authenticated");
            return;
        }

        String accountId=databaseReference.child(userId).push().getKey();
        financialAccount.setIdAccount(accountId);
        databaseReference.child(userId).child(accountId).setValue(financialAccount).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public interface RepositoryCallbackList
    {
        void onSuccessList(List<FinancialAccount> list);
        void onErrorList(String message);
    }

    public void getAllAccounts(RepositoryCallbackList callback)
    {
        String userId=getUserId();
        if(userId==null)
        {
            callback.onErrorList("User is not authenticated");
            return;
        }

        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<FinancialAccount> list=new ArrayList<>();
                for(DataSnapshot child:snapshot.getChildren())
                {
                    FinancialAccount financialAccount=child.getValue(FinancialAccount.class);
                    if(financialAccount!=null)
                    {
                        financialAccount.setIdAccount(child.getKey());
                        list.add(financialAccount);
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

    public void updateAccount(FinancialAccount financialAccount,RepositoryCallback callback)
    {
        String userId = getUserId();
        if (userId == null || financialAccount.getIdAccount() == null) {
            callback.onError("Invalid data");
            return;
        }

        databaseReference.child(userId).child(financialAccount.getIdAccount()).setValue(financialAccount).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public void deleteAccount(String accountId,RepositoryCallback callback)
    {
        String userId = getUserId();
        if (userId == null || accountId == null) {
            callback.onError("Invalid data");
            return;
        }

        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("transactions").child(userId);
        transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                    String accountIdInTransaction = transactionSnapshot.child("financialAccount").child("idAccount").getValue(String.class);
                    if (accountIdInTransaction != null && accountIdInTransaction.equals(accountId)) {
                        transactionSnapshot.getRef().removeValue();
                    }
                }
                databaseReference.child(userId).child(accountId).removeValue()
                        .addOnSuccessListener(unused -> callback.onSuccess())
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void addDefaultAccount(Runnable onComplete) {
        String userId = getUserId();
        if (userId == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("accounts").child(userId);
        databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    DatabaseReference databaseReferenceUser = FirebaseDatabase.getInstance().getReference("users").child(userId);
                    databaseReferenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                FinancialAccount wallet = new FinancialAccount(user, "Wallet", AccountType.CASH, 0.0, "Default account");
                                String walletId = databaseReference1.push().getKey();
                                wallet.setIdAccount(walletId);

                                databaseReference1.child(walletId).setValue(wallet)
                                        .addOnSuccessListener(unused -> {
                                            if (onComplete != null) onComplete.run();
                                        })
                                        .addOnFailureListener(e -> {
                                            if (onComplete != null) onComplete.run();
                                        });
                            } else {
                                if (onComplete != null) onComplete.run();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            if (onComplete != null) onComplete.run();
                        }
                    });
                } else {
                    // Dacă deja există, apelezi onComplete direct!
                    if (onComplete != null) onComplete.run();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (onComplete != null) onComplete.run();
            }
        });
    }

    // Modifică metoda addDefaultAccount pentru debugging

    public interface RepositoryCallbackSingle {
        void onSuccess(FinancialAccount account);
        void onError(String message);
    }

    public void getAccountById(String accountId, RepositoryCallbackSingle callback) {
        String userId = getUserId();
        if (userId == null) {
            callback.onError("User is not authenticated");
            return;
        }

        databaseReference.child(userId).child(accountId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        FinancialAccount account = snapshot.getValue(FinancialAccount.class);
                        if (account != null) {
                            account.setIdAccount(snapshot.getKey());
                            callback.onSuccess(account);
                        } else {
                            callback.onError("Account not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }


}

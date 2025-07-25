package com.example.budgetwise.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.budgetwise.classes.FinancialAccount;
import com.example.budgetwise.classes.ScheduledTransactions;
import com.example.budgetwise.classes.Transaction;
import com.example.budgetwise.classes.TransactionCategory;
import com.example.budgetwise.classes.TransactionType;
import com.example.budgetwise.classes.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseTransactionRepository {
    private final DatabaseReference databaseReference;//(pointer to json)access point to firebase branch - "transactions"
    private final FirebaseAuth firebaseAuth;//who is connected

    public FirebaseTransactionRepository() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference("transactions");
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    //returns the UID of the authenticated user in Firebase (uid unique)
    private String getUserId()
    {
        if(firebaseAuth.getCurrentUser()!=null)
        {
            return firebaseAuth.getCurrentUser().getUid();
        }
        else{
            return null;
        }
    }

    public void addTransaction(Transaction transaction,RepositoryCallback callback)
    {
        String userId=getUserId();//current user id
        if(userId==null)
        {
            callback.onError("User is not authenticated");
            return;
        }
        String transactionId=databaseReference.child(userId).push().getKey();//Create a unique ID for the new transaction
        transaction.setIdTransaction(transactionId);
        databaseReference.child(userId).child(transactionId).setValue(transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public interface RepositoryCallback
    {
        void onSuccess();
        void onError(String userIsNotAuthenticated);
    }

    public void getAllTransactionsForUser(RepositoryCallbackList callbackList)
    {
        String userId=getUserId();
        if(userId==null)
        {
            callbackList.onErrorList("User is not authenticated");
            return;
        }

        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {//We listen to the Firebase response only once, after the callback it stops automatically
            @Override //The method that is called when Firebase returns the data.
            public void onDataChange(@NonNull DataSnapshot snapshot) {//DataSnapshot will contain all transactions of the user child(user)
                List<Transaction> transactionList=new ArrayList<>();
                for(DataSnapshot child:snapshot.getChildren())//DataSnapshot is an object provided by Firebase that represents a piece of data in the database at a specific point in time
                {
                    if (child.child("transactionFrequency").exists()) {
                        com.example.budgetwise.classes.ScheduledTransactions scheduled = child.getValue(com.example.budgetwise.classes.ScheduledTransactions.class);
                        if(scheduled != null) transactionList.add(scheduled);
                    } else {
                        Transaction transaction=child.getValue(Transaction.class);
                        if(transaction!=null) transactionList.add(transaction);
                    }
                }
                callbackList.onSuccessList(transactionList);//When we're done, we send the entire list back so it can be displayed in the UI
            }

            @Override //If an error occurs
            public void onCancelled(@NonNull DatabaseError error) {
                callbackList.onErrorList(error.getMessage());
            }
        });
    }

    public interface RepositoryCallbackList
    {
        void onSuccessList(List<Transaction> transactionList);
        void onErrorList(String message);

    }

    public void getCurrentMonthTransactions(RepositoryCallbackList callbackListMonth)
    {
       getAllTransactionsForUser(new RepositoryCallbackList() {
           @Override
           public void onSuccessList(List<Transaction> transactionList) {
               Calendar calendar=Calendar.getInstance();//current date and time
               int currentMonth=calendar.get(Calendar.MONTH);
               int currentYear=calendar.get(Calendar.YEAR);

               List<Transaction> monthTransactionsList=new ArrayList<>();
               for(Transaction t:transactionList)
               {
                   Date date=t.getTransactionDate();
                   Calendar transformDate=Calendar.getInstance();
                   transformDate.setTime(date);//convert Date to Calendar to be able to get the month and year more easily
                   if(transformDate.get(Calendar.MONTH)==currentMonth && transformDate.get(Calendar.YEAR)==currentYear)
                   {
                       monthTransactionsList.add(t);
                   }
               }
               callbackListMonth.onSuccessList(monthTransactionsList);
           }

           @Override
           public void onErrorList(String message) {
               callbackListMonth.onErrorList(message);

           }
       });
    }

    public void getTransactionMonth(int month, int year, RepositoryCallbackList callbackList)
    {
        getAllTransactionsForUser(new RepositoryCallbackList() {
            @Override
            public void onSuccessList(List<Transaction> transactionList) {
                List<Transaction> filteredList = new ArrayList<>();
                for (Transaction t : transactionList) {
                    Date date;
                    if (t instanceof ScheduledTransactions) {
                        date = ((ScheduledTransactions) t).getStartDate();
                    } else {
                        date = t.getTransactionDate();
                    }
                    if (date != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        int tMonth = cal.get(Calendar.MONTH);
                        int tYear = cal.get(Calendar.YEAR);

                        if (tMonth == month && tYear == year) {
                            filteredList.add(t);
                        }
                    }
                }
                callbackList.onSuccessList(filteredList);
            }

            @Override
            public void onErrorList(String message) {
                callbackList.onErrorList(message);
            }
        });
    }

    public void updateTransaction(Transaction transaction, RepositoryCallback callback)
    {
        String userId=getUserId();
        if(userId==null || transaction.getIdTransaction()==null)
        {
            callback.onError("Invalid data");
            return;
        }
        //we don't save ScheduledTransactions's clones in firebase
        if (transaction instanceof ScheduledTransactions) {
            ScheduledTransactions st = (ScheduledTransactions) transaction;
            if (st.isCloneInstanceLocalOnly()) {
                return;
            }
        }

        databaseReference.child(userId).child(transaction.getIdTransaction()).setValue(transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public void deleteTransaction(String transactionId, RepositoryCallback callback)
    {
        String userId=getUserId();
        if(userId==null || transactionId==null)
        {
            callback.onError("Invalid data");
            return;
        }
        databaseReference.child(userId).child(transactionId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
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

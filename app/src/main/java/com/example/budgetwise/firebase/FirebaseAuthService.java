package com.example.budgetwise.firebase;

import androidx.annotation.NonNull;

import com.example.budgetwise.classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseAuthService {

    private static final String USER_REFERENCE="users"; //root
    private final DatabaseReference reference; //this is where I access the nodes in the database, used to establish the connection toward a parent node from database
    private final FirebaseAuth auth;
    private static FirebaseAuthService instance;

    //final = once  the auth and reference are initialized in the constructor, they won t be modified again
    public FirebaseAuthService()
    {
        this.auth=FirebaseAuth.getInstance(); //class used to establish a connection between the mobile application and the FirebaseAuthentication
        this.reference= FirebaseDatabase.getInstance().getReference(USER_REFERENCE);//the exact address where we want to work in the database (pointer to USERS)
    }

    public static FirebaseAuthService getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthService();
        }
        return instance;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    //SIGN UP
    //user is logged in with email and password. we ll save the date in the firebase database
    public void registerUser(String email, String password,String nume, String prenume,AuthCallback callback)
    {
        //createUserWithEmailAndPassword - this method belongs to FirebaseAuth; create a new account in Firebase Auth, but the user isn t added to Firebase Realtime db (The operation is asynchronous, so we have to wait for the result)
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {//Add a listener that will activate when the account creation operation completes.
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) //Task is the result of creating the account . If the account creation is successful, then the onComplete method will be called and task.isSuccessful() will be true.
            {
                if(task.isSuccessful()) //Was the user added successful?
                {
                    //if yes
                    FirebaseUser firebaseUser=auth.getCurrentUser();//Get the user who just registered.
                    if(firebaseUser!=null)
                    {
                        String userId=firebaseUser.getUid();
                        User user=new User(userId,nume,prenume,email);
                        reference.child(userId).setValue(user);//Creates a node (child) with the user's UID in the Firebase Database.
                        callback.onSuccess(firebaseUser);
                    }
                    else {
                        //FirebaseUser is null
                        callback.onFailure(task.getException().getMessage());
                    }
                }else {
                    callback.onFailure(task.getException().getMessage());
                }
            }
        });
    }


    //SING IN
    public void loginUser(String email, String password,AuthCallback callback)
    {
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    FirebaseUser firebaseUser=auth.getCurrentUser();
                    callback.onSuccess(firebaseUser);
                }
                else {
                    callback.onFailure(task.getException().getMessage());
                }
            }
        });
    }

    //when the user is added in the db, onSuccess is called (the implementation is developed in Login/Register class) / onFailure = the account wasn t added with success
    public interface AuthCallback
    {
        void onSuccess(FirebaseUser firebaseUser);
        void onFailure(String errorMessage);
    }
}

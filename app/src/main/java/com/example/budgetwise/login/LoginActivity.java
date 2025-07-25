package com.example.budgetwise.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.budgetwise.MainActivity;
import com.example.budgetwise.R;
import com.example.budgetwise.firebase.FirebaseAuthService;
import com.example.budgetwise.viewmodel.LoginViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText email_te;
    private TextInputEditText password_te;
    private Button signIn_btn;
    private TextView without_account_tv;

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //if the user is logged in, it goes directly to Main
        FirebaseUser currentUser = FirebaseAuthService.getInstance().getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        if(getSupportActionBar()!=null)
        {
            getSupportActionBar().hide();
        }
        initComponents();
        setupObservers();

    }

    private void initComponents() {
        email_te=findViewById(R.id.emailTextInputEditText);
        password_te=findViewById(R.id.passwordTextInputEditText);
        signIn_btn=findViewById(R.id.loginButton);
        without_account_tv=findViewById(R.id.without_account_tv);
        loginViewModel=new ViewModelProvider(this).get(LoginViewModel.class);

        signIn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValid())
                {
                    String email=email_te.getText().toString();
                    String password=password_te.getText().toString();
                    loginViewModel.login(email,password);//When the user clicks the Log In button, you call the login(...) method in the ViewModel

                }
            }
        });

        without_account_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setupObservers() { //the user was not logged in and logged in with email and password
            loginViewModel.getLoginSuccess().observe(this, firebaseUser -> {//is executed automatically when ViewModel updates a LiveData
            Toast.makeText(LoginActivity.this, R.string.login_successful, Toast.LENGTH_SHORT).show();
                if (firebaseUser != null && firebaseUser.getEmail() != null) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
                    ref.child("email").setValue(firebaseUser.getEmail());
                }
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        loginViewModel.getLoginError().observe(this, error -> {
            if (error.toLowerCase().contains("auth credential is incorrect")) {
                Toast.makeText(this, R.string.incorrect_email_or_password, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.error) + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private boolean isValid()
    {

        if(email_te.getText().toString().trim().isEmpty() || password_te.getText().toString().trim().isEmpty())
        {
            Toast.makeText(this, R.string.all_fields_are_required, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
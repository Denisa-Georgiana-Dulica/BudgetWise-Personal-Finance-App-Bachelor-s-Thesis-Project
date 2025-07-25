package com.example.budgetwise.login;

import android.content.Intent;
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
import com.example.budgetwise.viewmodel.RegisterViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText last_name_te;
    private TextInputEditText first_name_te;
    private TextInputEditText email_te;
    private TextInputEditText password_te;
    private Button register_btn;
    private RegisterViewModel registerViewModel;

    private TextView tv_go_to_LogIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if(getSupportActionBar()!=null)
        {
            getSupportActionBar().hide();
        }

        initComponents();
        setupObservers();
    }

    private void initComponents() {
        last_name_te=findViewById(R.id.nameRegisterTextInputEditText);
        first_name_te=findViewById(R.id.firstNameRegisterTextInputEditText);
        email_te=findViewById(R.id.emailRegisterTextInputEditText);
        password_te=findViewById(R.id.passwordRegisterTextInputEditText);
        register_btn=findViewById(R.id.registerButton);
        tv_go_to_LogIn=findViewById(R.id.tv_go_to_logIn);
        registerViewModel=new ViewModelProvider(this).get(RegisterViewModel.class);

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValid())
                {
                    String last_name=last_name_te.getText().toString();
                    String first_name=first_name_te.getText().toString();
                    String email=email_te.getText().toString();
                    String password=password_te.getText().toString();
                    registerViewModel.register(email,password,last_name,first_name);

                }
            }
        });

        tv_go_to_LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setupObservers()
    {
        registerViewModel.getRegisterSuccess().observe(this,firebaseUser -> {
            Toast.makeText(this, R.string.registration_successful, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        registerViewModel.getRegisterError().observe(this,error->{
            if (error.toLowerCase().contains("email address is already in use")) {
                Toast.makeText(this, getString(R.string.this_user_already_exists), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValid()
    {

        if(last_name_te.getText()==null || last_name_te.getText().toString().trim().length()<2 || !last_name_te.getText().toString().trim().matches("^[a-zA-Z]+$"))
        {
            Toast.makeText(getApplicationContext(), R.string.please_enter_a_valid_last_name_letters_only_minimum_2_characters, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(first_name_te.getText()==null || first_name_te.getText().toString().trim().length()<2 || !first_name_te.getText().toString().trim().matches("^[a-zA-Z]+$") )
        {
            Toast.makeText(getApplicationContext(), R.string.please_enter_a_valid_first_name_letters_only_minimum_2_characters, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(email_te.getText()==null || email_te.getText().toString().trim().length()<10 || !Patterns.EMAIL_ADDRESS.matcher(email_te.getText().toString().trim()).matches())
        {
            Toast.makeText(getApplicationContext(), R.string.please_enter_a_valid_email, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(password_te.getText().toString().trim().length()<6 || !password_te.getText().toString().trim().matches(".*\\d.*") || !password_te.getText().toString().trim().matches(".*[a-zA-Z].*"))
        {
            Toast.makeText(getApplicationContext(), R.string.the_password_must_have_at_least_6_characters_letters_and_numbers, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(email_te.getText().toString().trim().isEmpty() || password_te.getText().toString().trim().isEmpty() || first_name_te.getText().toString().trim().isEmpty() || last_name_te.getText().toString().trim().isEmpty())
        {
            Toast.makeText(this, R.string.all_fields_are_required, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}
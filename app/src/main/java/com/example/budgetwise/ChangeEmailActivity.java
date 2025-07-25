package com.example.budgetwise;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.budgetwise.login.LoginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeEmailActivity extends AppCompatActivity {

    private TextInputEditText oldEmail;
    private TextInputEditText newEmail;
    private Button changeButton;
    private Button cancelButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_email);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initComponents();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            oldEmail.setText(user.getEmail());
            oldEmail.setEnabled(false);
        }
        applyEmailStyle();
    }

    private void initComponents() {
        newEmail = findViewById(R.id.etNewEmail);
        oldEmail = findViewById(R.id.etPassword);
        changeButton = findViewById(R.id.btnChangeEmail);
        cancelButton = findViewById(R.id.btnCancel);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValid())
                {
                    String newEmailText = newEmail.getText().toString().trim();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user!=null)
                    {
                        user.verifyBeforeUpdateEmail(newEmailText).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getApplicationContext(), "Verification email sent. Please confirm then reconnect.", Toast.LENGTH_LONG).show();
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(ChangeEmailActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //we can't go back anymore
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("ChangeEmailActivity", "Error: " + e.getMessage(), e);
                                Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });
    }
    private boolean isValid()
    {
        String email = newEmail.getText() != null ? newEmail.getText().toString().trim() : "";

        if (email.isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.all_fields_are_required, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (email.length() < 10 || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getApplicationContext(), R.string.please_enter_a_valid_email, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void applyEmailStyle()
    {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Change email");
        }
    }

}
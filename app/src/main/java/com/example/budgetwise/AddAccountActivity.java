package com.example.budgetwise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.budgetwise.classes.AccountType;
import com.example.budgetwise.classes.FinancialAccount;
import com.example.budgetwise.classes.User;
import com.example.budgetwise.viewmodel.AccountViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddAccountActivity extends AppCompatActivity {

    public static final String ADDED_ACCOUNT = "added_account";
    public static final String UPDATED_ACCOUNT = "UPDATED_ACCOUNT";
    private MaterialToolbar toolbar;
    private TextView previousBalanceTv;
    private TextInputEditText accountNameTe;
    private TextInputEditText currentBalanceTe;
    private Spinner accountTypeSpinner;
    private TextInputEditText noteTe;
    private Button cancelBtn;
    private Button saveBtn;
    private AccountViewModel accountViewModel;
    private Intent intent;
    private boolean isUpdate = false;
    private FinancialAccount accountToUpdate = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initComponents();
        accountViewModel=new AccountViewModel();
        applyAccountStyle();
        intent=getIntent();
        isUpdate = intent.getBooleanExtra(MainActivity.IS_UPDATE_ACCOUNT, false);
        if (isUpdate) {
            accountToUpdate = intent.getParcelableExtra(MainActivity.UPDATE_ACCOUNT);
            if (accountToUpdate != null) {
                fillAccountData(accountToUpdate);
            }
        }
        if (!isUpdate) {
            previousBalanceTv.setVisibility(View.GONE);
        }

    }

    private void fillAccountData(FinancialAccount accountToUpdate) {
        accountNameTe.setText(accountToUpdate.getAccountName());
        currentBalanceTe.setText(String.valueOf(accountToUpdate.getCurrentBalance()));
        noteTe.setText(accountToUpdate.getNotes());

        currentBalanceTe.setEnabled(false);
        currentBalanceTe.setFocusable(false);

        AccountType type = accountToUpdate.getAccountType();
        for (int i = 0; i < accountTypeSpinner.getCount(); i++) {
            String item = accountTypeSpinner.getItemAtPosition(i).toString().toUpperCase();
            if (item.equals(type.name())) {
                accountTypeSpinner.setSelection(i);
                break;
            }
        }

        previousBalanceTv.setVisibility(View.VISIBLE);
        previousBalanceTv.setText("Previous balance: " + accountToUpdate.getInitialBalance() + " RON");

    }

    private void initComponents() {
        toolbar=findViewById(R.id.toolbar_add_account);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white));
        previousBalanceTv = findViewById(R.id.previousBalanceTv);
        accountNameTe=findViewById(R.id.accountNameTie);
        currentBalanceTe=findViewById(R.id.balanceTie);
        accountTypeSpinner=findViewById(R.id.spinner_accountType);
        noteTe=findViewById(R.id.accountNoteTie);
        cancelBtn=findViewById(R.id.btn_cancel_account);
        saveBtn=findViewById(R.id.btn_save_account);

        List<String> accountType=new ArrayList<>();
        for(AccountType a:AccountType.values())
        {
            accountType.add(formatEnumName(a.name()));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, accountType);
        accountTypeSpinner.setAdapter(adapter);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValid())
                {
                    String nameAccount=accountNameTe.getText().toString().trim();
                    String selectedTypeString = accountTypeSpinner.getSelectedItem().toString();
                    AccountType selectedType = AccountType.valueOf(selectedTypeString.toUpperCase());
                    String note=noteTe.getText().toString().trim();

                    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String userUid = user.getUid();
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userUid);
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user1 = snapshot.getValue(User.class);

                                if (!isUpdate) {
                                    double currentBalance=Double.parseDouble(currentBalanceTe.getText().toString().trim());
                                    FinancialAccount newAccount = new FinancialAccount(user1, nameAccount, selectedType, currentBalance, note);
                                    newAccount.setInitialBalance(currentBalance);
                                    accountViewModel.addAccount(newAccount);
                                    intent.putExtra(MainActivity.RESULT_TYPE, ADDED_ACCOUNT);
                                } else {
                                    accountToUpdate.setAccountName(nameAccount);
                                    accountToUpdate.setAccountType(selectedType);
                                    accountToUpdate.setNotes(note);
                                    accountViewModel.updateAccount(accountToUpdate);
                                    intent.putExtra(MainActivity.RESULT_TYPE, UPDATED_ACCOUNT);
                                }
                                setResult(RESULT_OK, intent);
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
    }

    private String formatEnumName(String name) {
        String lowerName = name.toLowerCase();
        StringBuilder formatted = new StringBuilder();
        formatted.append(Character.toUpperCase(lowerName.charAt(0)))
                    .append(lowerName.substring(1));
        return formatted.toString();
    }

    private boolean isValid() {
        String accountName = accountNameTe.getText().toString().trim();
        if (accountName.isEmpty()) {
            Toast.makeText(this, R.string.enter_account_name, Toast.LENGTH_SHORT).show();
            return false;
        }
        String amountText = currentBalanceTe.getText().toString().trim();
        if (amountText.isEmpty()) {
            Toast.makeText(this, R.string.enter_the_current_balance, Toast.LENGTH_SHORT).show();
            return false;
        }

        //not to be negative
        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                Toast.makeText(this, R.string.the_balance_must_be_positive, Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.invalid_balance_format, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void applyAccountStyle()
    {
        //set the title in the actionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add accounts");
        }
    }
}
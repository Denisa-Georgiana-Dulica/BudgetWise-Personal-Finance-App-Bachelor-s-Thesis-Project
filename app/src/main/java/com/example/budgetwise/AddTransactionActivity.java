package com.example.budgetwise;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.budgetwise.adapter.CategoryAdapter;
import com.example.budgetwise.classes.FinancialAccount;
import com.example.budgetwise.classes.ScheduledTransactions;
import com.example.budgetwise.classes.Transaction;
import com.example.budgetwise.classes.TransactionCategory;
import com.example.budgetwise.classes.TransactionType;
import com.example.budgetwise.classes.User;
import com.example.budgetwise.firebase.FirebaseTransactionRepository;
import com.example.budgetwise.viewmodel.AccountViewModel;
import com.example.budgetwise.viewmodel.CategoryViewModel;
import com.example.budgetwise.viewmodel.TransactionViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;

import org.checkerframework.checker.units.qual.C;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddTransactionActivity extends AppCompatActivity {

    public static final String ADDED_TRANSACTION = "added_transaction";
    public static final String UPDATED_TRANSACTION = "UPDATED_TRANSACTION";
    private MaterialToolbar toolbar;
    private TextInputEditText transactionAmountTie;
    private TextView dateTransactionTv;
    private Spinner spinnerCategory;
    private Spinner spinnerAccount;
    private TextInputEditText noteTie;
    private CheckBox isScheduledTransaction;
    private TextView startDateTransactionTv;
    private TextView endDateTransactionTv;
    private Spinner spinnerFrequency;
    private Button calcelBtn;
    private Button saveBtn;
    private LinearLayout layoutScheduled;
    private Intent intent;
    private TransactionViewModel transactionViewModel;
    private TransactionType transactionType;
    private CategoryViewModel categoryViewModel;
    private AccountViewModel accountViewModel;
    private Transaction transactionUpdate = null;
    private boolean isUpdate = false;
    private Map<String, FinancialAccount> accountMap = new HashMap<>();

    private TextInputLayout layoutAmount;
    private TextInputLayout layoutNote;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_transaction);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainAddTransaction), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initComponents();

        intent = getIntent();
        isUpdate = intent.getBooleanExtra(MainActivity.IS_UPDATE, false);

        //for adding transactions in firebase and updating
        transactionViewModel=new ViewModelProvider(this).get(TransactionViewModel.class);
        //for adding and retrieving  categories in firebase
        categoryViewModel=new ViewModelProvider(this).get(CategoryViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        if (isUpdate) {
            // UPDATE LOGIC
            transactionUpdate = intent.getParcelableExtra(MainActivity.UPDATE_TRANSACTION);
            if (transactionUpdate != null) {
                transactionType = transactionUpdate.getTransactionType();
                fillTransactionData(transactionUpdate);

                if (transactionUpdate instanceof ScheduledTransactions) {
                    saveBtn.setEnabled(false);
                    saveBtn.setAlpha(0.5f);
                    Toast.makeText(this, "Scheduled transactions can't be updated. Only view or delete.", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            // ADD LOGIC
            String typeString = intent.getStringExtra(MainActivity.TRANSACTION_TYPE);
            transactionType = TransactionType.EXPENSE; // default
            if (typeString != null) {
                transactionType = TransactionType.valueOf(typeString);
            }
            //the category spinner will be set to the first category
            spinnerCategory.setSelection(0);
            spinnerAccount.setSelection(0);
            spinnerFrequency.setSelection(0);
        }

        categoryViewModel.addDefaultIfEmpty();
        categoryViewModel.loadCategories();
        categoryViewModel.getCategories().observe(this,categories->{
            List<TransactionCategory> filteredCategories = new ArrayList<>();
            String typeToShow = (transactionType == TransactionType.INCOME) ? "INCOME" : "EXPENSE";
            for (TransactionCategory cat : categories) {
                if (cat.getType() != null && cat.getType().equalsIgnoreCase(typeToShow)) {
                    filteredCategories.add(cat);
                }
            }
            CategoryAdapter adapterCategory=new CategoryAdapter(this,R.layout.category_row,filteredCategories,getLayoutInflater());
            spinnerCategory.setAdapter(adapterCategory);
            if (isUpdate && transactionUpdate != null) {
                for (int i = 0; i < filteredCategories.size(); i++)
                {
                    if (filteredCategories.get(i).getIdCategory().equals(transactionUpdate.getTransactionCategory().getIdCategory())) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }
            }
        });

        List<FinancialAccount> accountsList = new ArrayList<>();
        accountViewModel.getAllAccounts();
        accountViewModel.getList().observe(this, accounts -> {
            accountsList.clear();
            accountMap.clear();
            if (accounts != null) {
                for (FinancialAccount acc : accounts) {
                    accountsList.add(acc);
                    accountMap.put(acc.getIdAccount(), acc);
                }
            }
            ArrayAdapter<FinancialAccount> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, accountsList);
            spinnerAccount.setAdapter(adapter);
            if (isUpdate && transactionUpdate != null) {
                for (int i = 0; i < accountsList.size(); i++) {
                    if (accountsList.get(i).getIdAccount().equals(transactionUpdate.getIdAccount())) {
                        spinnerAccount.setSelection(i);
                        break;
                    }
                }
            }
        });
        accountViewModel.reloadAllAccountsFromFirebase();

        // apply styling
        applyTransactionStyle(transactionType);

        //set the current date OR set the date selected by user
        setDate(dateTransactionTv, transactionType);
        //set the current date OR set the date selected by user for scheduled transaction
        setStartDate(startDateTransactionTv, transactionType);
        setDate(endDateTransactionTv, transactionType);
    }

    private void fillTransactionData(Transaction transactionUpdate) {
        transactionAmountTie.setText(String.valueOf(transactionUpdate.getTransactionAmount()));
        dateTransactionTv.setText(DateConverter.fromDate(transactionUpdate.getTransactionDate()));
        noteTie.setText(transactionUpdate.getTransactionDescription());
        if (transactionUpdate instanceof ScheduledTransactions) {
            ScheduledTransactions scheduled = (ScheduledTransactions) transactionUpdate;
            isScheduledTransaction.setChecked(true);
            isScheduledTransaction.setEnabled(false);
            isScheduledTransaction.setVisibility(View.VISIBLE);
            layoutScheduled.setVisibility(View.VISIBLE);
            startDateTransactionTv.setText(DateConverter.fromDate(scheduled.getStartDate()));
            endDateTransactionTv.setText(DateConverter.fromDate(scheduled.getEndDate()));

            dateTransactionTv.setEnabled(false);
            dateTransactionTv.setAlpha(0.5f);

            String freq = scheduled.getTransactionFrequency();
            for (int i = 0; i < spinnerFrequency.getCount(); i++) {
                if (spinnerFrequency.getItemAtPosition(i).toString().equals(freq)) {
                    spinnerFrequency.setSelection(i);
                    break;
                }
            }
        } else {
            isScheduledTransaction.setChecked(false);
            isScheduledTransaction.setVisibility(View.GONE);
            layoutScheduled.setVisibility(View.GONE);

            dateTransactionTv.setEnabled(true);
            dateTransactionTv.setAlpha(1f);
            dateTransactionTv.setText(DateConverter.fromDate(transactionUpdate.getTransactionDate()));
        }
        applyTransactionStyle(transactionUpdate.getTransactionType());
    }

    private void applyTransactionStyle(TransactionType transactionType) {
        int colorResId;
        int titleResId;

        if (transactionType == TransactionType.INCOME) {
            colorResId = R.color.income_color;
            titleResId = R.string.add_new_income;
        } else {
            colorResId = R.color.expense_color;
            titleResId = R.string.add_new_expense;
        }

        //set the title in the actionBar
        if (getSupportActionBar() != null) {//Android gives control of the title to the SupportActionBar, not to the MaterialToolbar object directly (after setSuportActionVBar(..))
            getSupportActionBar().setTitle(titleResId);
        }

        //change the color
        toolbar.setBackgroundColor(ContextCompat.getColor(this, colorResId));
        saveBtn.setBackgroundColor(ContextCompat.getColor(this, colorResId));
        calcelBtn.setBackgroundColor(ContextCompat.getColor(this, colorResId));
        isScheduledTransaction.setButtonTintList(ContextCompat.getColorStateList(this, colorResId));

        getWindow().setStatusBarColor(ContextCompat.getColor(this, colorResId));

        if (layoutAmount != null) {
            layoutAmount.setBoxStrokeColor(ContextCompat.getColor(this, colorResId));
            layoutAmount.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, colorResId)));
        }
        if (layoutNote != null) {
            layoutNote.setBoxStrokeColor(ContextCompat.getColor(this, colorResId));
            layoutNote.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, colorResId)));
        }
    }

    private void initComponents() {
        toolbar=findViewById(R.id.toolbar_add_transaction);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white));
        transactionAmountTie=findViewById(R.id.amountTie);
        dateTransactionTv=findViewById(R.id.dateTransactionAdd);
        spinnerCategory=findViewById(R.id.spinnerCategory);
        spinnerAccount=findViewById(R.id.spinner_account);
        noteTie=findViewById(R.id.noteTie);
        isScheduledTransaction=findViewById(R.id.checkbox_scheduled);
        startDateTransactionTv=findViewById(R.id.startDateTransactionScheduled);
        endDateTransactionTv=findViewById(R.id.endDateTransactionScheduled);
        spinnerFrequency=findViewById(R.id.spinner_frequency);
        calcelBtn=findViewById(R.id.btn_cancel);
        saveBtn=findViewById(R.id.btn_save);
        layoutScheduled=findViewById(R.id.layoutScheduledTransaction);
        layoutAmount=findViewById(R.id.amountTil);
        layoutNote=findViewById(R.id.noteTil);

        //if check box is true, the schedualed options are available and visible
        isScheduledTransaction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    layoutScheduled.setVisibility(View.VISIBLE);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                    Calendar calendar = Calendar.getInstance();
                    startDateTransactionTv.setText(sdf.format(calendar.getTime()));

                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    endDateTransactionTv.setText(sdf.format(calendar.getTime()));

                    Date startDate = DateConverter.toDate(startDateTransactionTv.getText().toString());
                    Date endDate = DateConverter.toDate(endDateTransactionTv.getText().toString());
                    updateFrequencyOptions(startDate, endDate);

                    dateTransactionTv.setText(sdf.format(Calendar.getInstance().getTime()));
                    dateTransactionTv.setEnabled(false);//if it is a scheduled transaction, I cannot change the current date (today)
                    dateTransactionTv.setAlpha(0.5f);
                    dateTransactionTv.setOnClickListener(null);
                }else{
                    layoutScheduled.setVisibility(View.GONE);
                    dateTransactionTv.setEnabled(true);
                    dateTransactionTv.setAlpha(1f);
                    setDate(dateTransactionTv, transactionType);
                }
            }
        });

        calcelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transactionUpdate instanceof ScheduledTransactions) {
                    saveBtn.setEnabled(false);
                    saveBtn.setAlpha(0.5f);
                    Toast.makeText(getApplicationContext(), "Scheduled transactions can't be updated. Only view or delete.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(isValid())
                {
                    //take the new data entered
                    double newAmount=Double.parseDouble(transactionAmountTie.getText().toString().trim());
                    TransactionCategory selectedCategory= (TransactionCategory) spinnerCategory.getSelectedItem();
                    String selectedId = ((FinancialAccount) spinnerAccount.getSelectedItem()).getIdAccount();
                    FinancialAccount newAccount = accountMap.get(selectedId);
                    String note=noteTie.getText().toString().trim();
                    Date date=DateConverter.toDate(dateTransactionTv.getText().toString());

                        FirebaseUser currentUser= FirebaseAuth.getInstance().getCurrentUser();
                        if(currentUser!=null)
                        {
                            String userId=currentUser.getUid();
                            DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Users").child(userId);
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    User user=snapshot.getValue(User.class);//the logged in user
                                    if (!isUpdate)//add the transaction
                                    {
                                        Transaction transaction;
                                        if (isScheduledTransaction.isChecked()) {
                                            // You only create ScheduledTransactions, you do not change the balance
                                            Date start = DateConverter.toDate(startDateTransactionTv.getText().toString());
                                            Date end = DateConverter.toDate(endDateTransactionTv.getText().toString());
                                            String freq = spinnerFrequency.getSelectedItem().toString();
                                            transaction = new ScheduledTransactions(selectedCategory, selectedId, user, transactionType, date, newAmount, note, freq, start, end);
                                        } else {
                                            //during a normal transaction you change the balance
                                            if (transactionType == TransactionType.EXPENSE) {
                                                newAccount.setCurrentBalance(newAccount.getCurrentBalance() - newAmount);
                                            } else {
                                                newAccount.setCurrentBalance(newAccount.getCurrentBalance() + newAmount);
                                            }
                                            transaction = new Transaction(selectedCategory, selectedId, user, transactionType, date, newAmount, note);
                                            accountViewModel.updateAccount(newAccount);
                                            accountViewModel.reloadAllAccountsFromFirebase();
                                        }
                                        transactionViewModel.addTransaction(transaction);
                                        if (transaction instanceof ScheduledTransactions) {
                                            transactionViewModel.triggerRefresh();
                                        }
                                    } else {
                                        // UPDATE transaction
                                        double oldAmount = transactionUpdate.getTransactionAmount();
                                        String oldId = transactionUpdate.getIdAccount();
                                        FinancialAccount oldAccount = accountMap.get(oldId);

                                        transactionUpdate.setTransactionAmount(newAmount);
                                        transactionUpdate.setTransactionDescription(note);
                                        transactionUpdate.setTransactionDate(date);
                                        transactionUpdate.setTransactionCategory(selectedCategory);
                                        transactionUpdate.setIdAccount(selectedId);

                                        if (transactionUpdate instanceof ScheduledTransactions && isScheduledTransaction.isChecked()) {
                                            ScheduledTransactions scheduled = (ScheduledTransactions) transactionUpdate;
                                            scheduled.setStartDate(DateConverter.toDate(startDateTransactionTv.getText().toString()));
                                            scheduled.setEndDate(DateConverter.toDate(endDateTransactionTv.getText().toString()));
                                            scheduled.setTransactionFrequency(spinnerFrequency.getSelectedItem().toString());
                                        }

                                        if (!oldAccount.getIdAccount().equals(newAccount.getIdAccount())) {
                                            if (transactionUpdate.getTransactionType() == TransactionType.EXPENSE) {
                                                oldAccount.setCurrentBalance(oldAccount.getCurrentBalance() + oldAmount);
                                                newAccount.setCurrentBalance(newAccount.getCurrentBalance() - newAmount);
                                            } else {
                                                oldAccount.setCurrentBalance(oldAccount.getCurrentBalance() - oldAmount);
                                                newAccount.setCurrentBalance(newAccount.getCurrentBalance() + newAmount);
                                            }
                                            accountViewModel.updateAccount(oldAccount);
                                            accountViewModel.updateAccount(newAccount);
                                            accountViewModel.reloadAllAccountsFromFirebase();
                                        } else if (oldAmount != newAmount) {
                                            double diff = newAmount - oldAmount;
                                            if (transactionUpdate.getTransactionType() == TransactionType.EXPENSE) {
                                                newAccount.setCurrentBalance(newAccount.getCurrentBalance() - diff);
                                            } else {
                                                newAccount.setCurrentBalance(newAccount.getCurrentBalance() + diff);
                                            }
                                            accountViewModel.updateAccount(newAccount);
                                            accountViewModel.reloadAllAccountsFromFirebase();
                                        }

                                        transactionViewModel.updateTransactionWithCallback(transactionUpdate, new FirebaseTransactionRepository.RepositoryCallback() {
                                            @Override
                                            public void onSuccess() {
                                                intent.putExtra(MainActivity.RESULT_TYPE, UPDATED_TRANSACTION);
                                                setResult(RESULT_OK, intent);
                                                finish();
                                            }

                                            @Override
                                            public void onError(String message) {
                                                Toast.makeText(AddTransactionActivity.this, "Update failed: " + message, Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }

                                    if (!isUpdate) {
                                        intent.putExtra(MainActivity.RESULT_TYPE, ADDED_TRANSACTION);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.error_user_add_transaction) + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }


                }
            }
        });
    }

    //sets a new date when the current date is clicked
    public void setDate(TextView tv,TransactionType transactionType)
    {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        if (tv.getText().toString().trim().isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            tv.setText(simpleDateFormat.format(calendar.getTime()));
        }
        //wraps a Context with a custom theme
        //I am using the context of this activity (AddTransactionActivity), but I am applying a different theme ONLY for this dialog or UI component
        ContextThemeWrapper contextThemeWrapper=new ContextThemeWrapper(AddTransactionActivity.this,transactionType==TransactionType.INCOME ? R.style.DatePickerIncome : R.style.DatePickerExpense);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now=Calendar.getInstance();
                new DatePickerDialog(contextThemeWrapper, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar selected=Calendar.getInstance();
                        selected.set(year,month,dayOfMonth);
                        tv.setText(simpleDateFormat.format(selected.getTime()));
                        if (isScheduledTransaction.isChecked()){
                            String start = startDateTransactionTv.getText().toString();
                            String end = endDateTransactionTv.getText().toString();
                            if (!start.isEmpty() && !end.isEmpty()) {
                                Date startDate = DateConverter.toDate(start);
                                Date endDate = DateConverter.toDate(end);
                                updateFrequencyOptions(startDate, endDate);
                            }
                        }
                    }
                },now.get(Calendar.YEAR),now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }
    private boolean isValid() {
        //amount mandatory to enter
        String amountText = transactionAmountTie.getText().toString().trim();
        if (amountText.isEmpty()) {
            Toast.makeText(this, R.string.enter_an_amount, Toast.LENGTH_SHORT).show();
            return false;
        }

        //not to be negative
        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                Toast.makeText(this, R.string.the_sum_must_be_positive, Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.invalid_amount_format, Toast.LENGTH_SHORT).show();
            return false;
        }

        //if it is a scheduled transaction
        if (isScheduledTransaction.isChecked()) {

            //the start date must be less than the end date
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            try {
                String start = startDateTransactionTv.getText().toString();
                String end = endDateTransactionTv.getText().toString();
                if (!sdf.parse(end).after(sdf.parse(start))) {
                    Toast.makeText(this, "End date must be at least one day after start date", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (Exception e) {
                Toast.makeText(this, R.string.data_parsing_error, Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if(transactionType==TransactionType.EXPENSE)
        {
            String selectedId = ((FinancialAccount) spinnerAccount.getSelectedItem()).getIdAccount();
            FinancialAccount financialAccount = accountMap.get(selectedId);
            if(financialAccount!=null && financialAccount.getCurrentBalance()<amount)
            {
                Toast.makeText(this, R.string.insufficient_balance_in_selected_account, Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private void updateFrequencyOptions(Date start,Date end)
    {
        long diff = end.getTime() - start.getTime();//miliseconds
        long days = diff / (1000 * 60 * 60 * 24);

        String[] items = getResources().getStringArray(R.array.frequency);

        if (days >= 365) {
            items = getResources().getStringArray(R.array.frequency);
        } else if (days >= 30) {
            items = getResources().getStringArray(R.array.frequency_monthly_weekly);
        } else if (days >= 7) {
            items = getResources().getStringArray(R.array.frequency_weekly);
        } else {
            items = getResources().getStringArray(R.array.frequency_daily);
        }

        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,items);
        spinnerFrequency.setAdapter(adapter);
    }

    public void setStartDate(TextView tv, TransactionType transactionType) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        if (tv.getText().toString().trim().isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            tv.setText(simpleDateFormat.format(calendar.getTime()));
        }
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(AddTransactionActivity.this,
                transactionType == TransactionType.INCOME ? R.style.DatePickerIncome : R.style.DatePickerExpense);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                new DatePickerDialog(contextThemeWrapper, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, dayOfMonth);

                        Calendar today = Calendar.getInstance();//we are not allowed to set start dates from the past
                        today.set(Calendar.HOUR_OF_DAY, 0);
                        today.set(Calendar.MINUTE, 0);
                        today.set(Calendar.SECOND, 0);
                        today.set(Calendar.MILLISECOND, 0);

                        if (selected.before(today)) {
                            Toast.makeText(AddTransactionActivity.this, "Start date must be after today's date", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        tv.setText(simpleDateFormat.format(selected.getTime()));

                        if (isScheduledTransaction.isChecked()) {
                            String start = startDateTransactionTv.getText().toString();
                            String end = endDateTransactionTv.getText().toString();
                            if (!start.isEmpty() && !end.isEmpty()) {
                                Date startDate = DateConverter.toDate(start);
                                Date endDate = DateConverter.toDate(end);
                                updateFrequencyOptions(startDate, endDate);
                            }
                        }
                    }
                }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

}
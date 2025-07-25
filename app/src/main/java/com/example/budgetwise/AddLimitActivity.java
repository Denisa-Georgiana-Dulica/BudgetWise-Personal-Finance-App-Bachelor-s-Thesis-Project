package com.example.budgetwise;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.budgetwise.adapter.CategoryAdapter;
import com.example.budgetwise.classes.FinancialAccount;
import com.example.budgetwise.classes.Limit;
import com.example.budgetwise.classes.TransactionCategory;
import com.example.budgetwise.viewmodel.AccountViewModel;
import com.example.budgetwise.viewmodel.CategoryViewModel;
import com.example.budgetwise.viewmodel.LimitViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddLimitActivity extends AppCompatActivity {

    public static final String ADDED_LIMIT = "added_limit";
    public static final String UPDATED_LIMIT = "UPDATED_LIMIT";
    private Spinner spinnerCategory;
    private TextInputEditText maxAmountTe;
    private TextView startDateTv;
    private TextView endDateTv;
    private Button saveLimit;
    private Button cancelLimit;
    private CategoryViewModel categoryViewModel;
    private Map<String, FinancialAccount> accountMap = new HashMap<>();
    private List<FinancialAccount> accountsList = new ArrayList<>();
    private LimitViewModel limitViewModel;
    private Intent intent;
    private boolean isUpdate = false;
    private Limit limitToUpdate=null;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_limit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initComponents();
        limitViewModel = new ViewModelProvider(this).get(LimitViewModel.class);
        limitViewModel.loadLimits();
        intent=getIntent();
        isUpdate = intent.getBooleanExtra(MainActivity.IS_UPDATE_LIMIT, false);
        if (isUpdate) {
            limitToUpdate = intent.getParcelableExtra(MainActivity.UPDATE_LIMIT);
            if (limitToUpdate != null) {
                fillLimitData(limitToUpdate);
            }
        }

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        categoryViewModel.addDefaultIfEmpty();
        categoryViewModel.loadCategories();
        categoryViewModel.getCategories().observe(this, categories -> {
            List<TransactionCategory> filteredCategories = new ArrayList<>();
            for (TransactionCategory cat : categories) {
                if ("EXPENSE".equalsIgnoreCase(cat.getType())) {
                    filteredCategories.add(cat);
                }
            }
            CategoryAdapter adapterCategory = new CategoryAdapter(this, R.layout.category_row, filteredCategories, getLayoutInflater());
            spinnerCategory.setAdapter(adapterCategory);

            if (!filteredCategories.isEmpty()) {
                spinnerCategory.setSelection(0);
            }
        });

        setStartDateWithEndUpdate();
        setDateForLimit(endDateTv);

        applyLimitStyle();
    }

    private void fillLimitData(Limit limitToUpdate) {
        maxAmountTe.setText(String.valueOf(limitToUpdate.getMaxSum()));
        if (limitToUpdate.getDateStartLimit() != null) {
            startDateTv.setText(DateConverter.fromDate(limitToUpdate.getDateStartLimit()));
        }
        if (limitToUpdate.getDateFinalLimit() != null) {
            endDateTv.setText(DateConverter.fromDate(limitToUpdate.getDateFinalLimit()));
        }

        spinnerCategory.setEnabled(false);
        // make category selection match the limit s category
        spinnerCategory.post(() -> {
            for (int i = 0; i < spinnerCategory.getCount(); i++) {
                TransactionCategory category = (TransactionCategory) spinnerCategory.getItemAtPosition(i);
                if (category.getIdCategory().equals(limitToUpdate.getIdCategory())) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        });


        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.update_limit);
        }
    }

    private void initComponents() {
        spinnerCategory=findViewById(R.id.spinnerCategoryType);
        maxAmountTe=findViewById(R.id.maxAmountTie);
        startDateTv=findViewById(R.id.startDateLimit);
        endDateTv=findViewById(R.id.endDateLimit);
        saveLimit=findViewById(R.id.btn_save_limit);
        cancelLimit=findViewById(R.id.btn_cancel_limit);
        toolbar=findViewById(R.id.toolbar_add_limit);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        cancelLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        saveLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValid())
                {
                    double maxAmount=Double.parseDouble(maxAmountTe.getText().toString().trim());
                    Date startDate=DateConverter.toDate(startDateTv.getText().toString().trim());
                    Date endDate=DateConverter.toDate(endDateTv.getText().toString().trim());

                    if (isUpdate) {
                        limitToUpdate.setMaxSum(maxAmount);
                        limitToUpdate.setDateStartLimit(startDate);
                        limitToUpdate.setDateFinalLimit(endDate);

                        limitViewModel.updateLimit(limitToUpdate);
                        limitViewModel.getSuccess().observe(AddLimitActivity.this, isSuccess -> {
                            if (isSuccess != null) {
                                Toast.makeText(AddLimitActivity.this, "Limit updated successfully", Toast.LENGTH_SHORT).show();
                                intent.putExtra(MainActivity.RESULT_TYPE, UPDATED_LIMIT);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        });
                        limitViewModel.getError().observe(AddLimitActivity.this, errorMsg -> {
                            if (errorMsg != null && !errorMsg.isEmpty()) {
                                Toast.makeText(AddLimitActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        });
                    }else{
                        TransactionCategory selectedCategory = (TransactionCategory) spinnerCategory.getSelectedItem();
                        String categoryId = selectedCategory.getIdCategory();

                        Limit limit=new Limit(categoryId,maxAmount,startDate,endDate);
                        limitViewModel.addLimit(limit);
                        intent.putExtra(MainActivity.RESULT_TYPE, ADDED_LIMIT);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                }
            }
        });
    }

    private boolean isValid()
    {
        String amountS=maxAmountTe.getText()!=null ? maxAmountTe.getText().toString():"";
        String startDateS = startDateTv.getText().toString().trim();
        String endDateS = endDateTv.getText().toString().trim();
        if (amountS.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_enter_a_maximum_amount), Toast.LENGTH_SHORT).show();
            return false;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountS);
            if (amount <= 0) {
                Toast.makeText(this, getString(R.string.amount_must_be_greater_than_0), Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show();
            return false;
        }
        Date startDate = DateConverter.toDate(startDateS);
        Date today = DateConverter.toDate(DateConverter.fromDate(new Date()));

        if(isUpdate)
        {
            if (!endDateS.isEmpty()) {
                Date endDate = DateConverter.toDate(endDateS);
                if (endDate == null || !endDateAfterStartDate(startDate,endDate)) {
                    Toast.makeText(this, R.string.end_date_must_be_at_least_one_day_after_start_date, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }else{
            if (startDate == null || startDate.before(today)) {
                Toast.makeText(this, getString(R.string.start_date_must_be_today_or_in_the_future), Toast.LENGTH_SHORT).show();
                return false;
            }
            if (!endDateS.isEmpty()) {
                Date endDate = DateConverter.toDate(endDateS);
                if (endDate == null || !endDateAfterStartDate(startDate,endDate)) {
                    Toast.makeText(this, R.string.end_date_must_be_at_least_one_day_after_start_date, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        if (spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(this, R.string.please_select_a_category, Toast.LENGTH_SHORT).show();
            return false;
        }

        TransactionCategory selectedCategory = (TransactionCategory) spinnerCategory.getSelectedItem();
        String selectedCategoryId = selectedCategory.getIdCategory();

        List<Limit> allLimits = limitViewModel.getLimits().getValue();
        if (allLimits != null) {
            for (Limit l : allLimits) {
                if (l.getIdCategory().equals(selectedCategoryId)) {
                    if (!isUpdate){
                        Toast.makeText(this, "A limit already exists for this category.", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void setStartDateWithEndUpdate() {
        if (startDateTv.getText().toString().trim().isEmpty()) {
            Date today = new Date();
            startDateTv.setText(DateConverter.fromDate(today));

            Calendar endDateCal = Calendar.getInstance();
            endDateCal.add(Calendar.DAY_OF_MONTH, 1);
            endDateTv.setText(DateConverter.fromDate(endDateCal.getTime()));
        }

        startDateTv.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    AddLimitActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, dayOfMonth);
                        startDateTv.setText(DateConverter.fromDate(selected.getTime()));

                        Calendar endDateCal = (Calendar) selected.clone();
                        endDateCal.add(Calendar.DAY_OF_MONTH, 1);
                        endDateTv.setText(DateConverter.fromDate(endDateCal.getTime()));
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            dialog.getDatePicker().setMinDate(now.getTimeInMillis());
            dialog.show();
        });
    }


    private void setDateForLimit(TextView textView) {
        if (textView.getText().toString().trim().isEmpty()) {
            Date today = new Date();
            textView.setText(DateConverter.fromDate(today));
        }

        textView.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    AddLimitActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, dayOfMonth);
                        textView.setText(DateConverter.fromDate(selected.getTime()));
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            dialog.getDatePicker().setMinDate(now.getTimeInMillis());
            dialog.show();
        });
    }
    private void applyLimitStyle()
    {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add limit");
        }
    }

    private boolean endDateAfterStartDate(Date start, Date end)
    {
        Calendar startCalendar=Calendar.getInstance();
        startCalendar.setTime(start);
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);

        Calendar endCalendar=Calendar.getInstance();
        endCalendar.setTime(end);
        endCalendar.set(Calendar.HOUR_OF_DAY, 0);
        endCalendar.set(Calendar.MINUTE, 0);
        endCalendar.set(Calendar.SECOND, 0);
        endCalendar.set(Calendar.MILLISECOND, 0);

        startCalendar.add(Calendar.DAY_OF_MONTH, 1);
        return endCalendar.compareTo(startCalendar) >= 0;
    }
}
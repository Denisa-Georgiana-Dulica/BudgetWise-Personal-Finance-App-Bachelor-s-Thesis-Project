package com.example.budgetwise.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.budgetwise.MainActivity;
import com.example.budgetwise.R;
import com.example.budgetwise.adapter.TransactionAdapter;
import com.example.budgetwise.classes.FinancialAccount;
import com.example.budgetwise.classes.ScheduledTransactions;
import com.example.budgetwise.classes.Transaction;
import com.example.budgetwise.classes.TransactionCategory;
import com.example.budgetwise.classes.TransactionType;
import com.example.budgetwise.firebase.FirebaseTransactionRepository;
import com.example.budgetwise.viewmodel.AccountViewModel;
import com.example.budgetwise.viewmodel.CategoryViewModel;
import com.example.budgetwise.viewmodel.TransactionViewModel;
import com.google.android.material.appbar.MaterialToolbar;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TransactionFragment extends Fragment {

    private MaterialToolbar toolbar;
    private ImageView imageFilter;
    private ListView listView;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;
    private TransactionViewModel transactionViewModel;
    private TextView textMonth;
    private ImageView btnPrevMonth;
    private ImageView btnNextMonth;
    private Calendar currentCalendar;
    private TextView totalSumTransaction;
    private CategoryViewModel categoryViewModel;
    private AccountViewModel accountViewModel;
    private LinearLayout layout_total_transactions;
    private List<Transaction> filteredList = new ArrayList<>();
    private Map<String, FinancialAccount> accountMap = new HashMap<>();
    private Set<String> sentNotifications = new HashSet<>();

    public TransactionFragment() {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) { //getActivity gives you the reference to the activity where the fragment is "pasted"
            ((MainActivity) getActivity()).setVisibilityFab(true);
        }
        currentCalendar = Calendar.getInstance();
        accountViewModel.reloadAllAccountsFromFirebase();
        accountViewModel.getList().observe(getViewLifecycleOwner(), updatedAccounts -> {
            accountMap.clear();
            for (FinancialAccount acc : updatedAccounts) {
                accountMap.put(acc.getIdAccount(), acc);
            }
            adapter.notifyDataSetChanged();
            processScheduledTransactions();
        });
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        imageFilter=view.findViewById(R.id.filterIcon);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
        categoryViewModel.addDefaultIfEmpty();
        categoryViewModel.loadCategories();
        toolbar=view.findViewById(R.id.toolbar_transaction);
        textMonth=view.findViewById(R.id.textMonth);
        totalSumTransaction=view.findViewById(R.id.sumTransactionMonth);
        totalSumTransaction.setVisibility(View.INVISIBLE);
        layout_total_transactions=view.findViewById(R.id.layout_total_transactions);
        btnPrevMonth=view.findViewById(R.id.btnPrevMonth);
        btnNextMonth=view.findViewById(R.id.btnNextMonth);
        currentCalendar=Calendar.getInstance();
        formDate();//displays the current month and year in this format

        listView=view.findViewById(R.id.listViewTransactions);
        transactionList=new ArrayList<>();
        //adapter=new TransactionAdapter(getContext().getApplicationContext(),R.layout.transaction_row,transactionList,getLayoutInflater(),accountMap);
        filteredList.addAll(transactionList);
        adapter=new TransactionAdapter(getContext().getApplicationContext(),R.layout.transaction_row,filteredList,getLayoutInflater(),accountMap);
        listView.setAdapter(adapter);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        transactionViewModel.getRefreshNeeded().observe(getViewLifecycleOwner(), refresh -> {
            if (refresh != null && refresh) {
                processScheduledTransactions();
                reloadData();
                transactionViewModel.resetRefresh();
            }
        });
        showTransactionMonth(currentCalendar);//we show the transactions of the current month when entering the fragment

        //if the < button is pressed then currentCalendar decreases by one month
        btnPrevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCalendar.add(Calendar.MONTH,-1);
                formDate();
                showTransactionMonth(currentCalendar);//show me the transactions from the selected month
            }
        });

        //if the < button is pressed then currentCalendar increases by one month
        btnNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCalendar.add(Calendar.MONTH,1);
                formDate();
                showTransactionMonth(currentCalendar);
            }
        });

        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Transaction selectedTransaction = transactionList.get(position);
                showOptionsDialog(selectedTransaction);
            }
        });

        imageFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v);
            }
        });

        return view;
    }

    private void showPopup(View v) {
        PopupMenu popupMenu=new PopupMenu(requireContext(),v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_filter,popupMenu.getMenu());//getMenuInflater() returns a MenuInflater that aims to "read" menu XML files and transform them into Menu objects
        //getMenuInflater() returns a MenuInflater that aims to "read" menu XML files and transform them into Menu objects
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.filter_category) {
                    showCategoryFilterDialog();
                    return true;

                } else if (id == R.id.filter_date) {
                    showDateFilterDialog();
                    return true;
                }else if (id == R.id.filter_all) {
                    resetFilterAllTransactions();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void showCategoryFilterDialog()
    {
        categoryViewModel.getCategories().observe(getViewLifecycleOwner(),categoryList->{
            List<String> categoryNames = new ArrayList<>();
            for(TransactionCategory t:categoryList)
            {
                categoryNames.add(t.getCategoryName());
            }

            String[] array=categoryNames.toArray(new String[0]);
            new AlertDialog.Builder(requireContext()).setTitle("Select a category")
                    .setItems(array,(dialog, which) -> {
                        String selectedCategory=array[which];
                        filterByCategory(selectedCategory);
                    }).setNegativeButton("Cancel",null).show();
        });
    }

    private void showDateFilterDialog()
    {
        Calendar calendar=Calendar.getInstance();
        DatePickerDialog datePickerDialog=new DatePickerDialog(requireContext(),(view, year, month, dayOfMonth) -> { //when the user clicks "OK" selectedDate will be set to the date chosen by the user
            Calendar selectedDate=Calendar.getInstance();
            selectedDate.set(Calendar.YEAR,year);
            selectedDate.set(Calendar.MONTH,month);
            selectedDate.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            filterByDate(selectedDate.getTime());
        }, calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void filterByDate(Date selectedDate) {
        Date selected=reserHour(selectedDate);
        filteredList.clear();
        for (Transaction t : transactionList) {
            Date trDate=reserHour(t.getTransactionDate());
            if(trDate.equals(selected))
            {
                filteredList.add(t);
            }
        }
        adapter.notifyDataSetChanged();
        recalculateFilteredSum();
    }

    private void filterByCategory(String category)
    {
        filteredList.clear();
        for(Transaction t:transactionList)
        {
            if(t.getTransactionCategory().getCategoryName().equalsIgnoreCase(category))
            {
                filteredList.add(t);
            }
        }
        adapter.notifyDataSetChanged();
        recalculateFilteredSum();
    }

    private void recalculateFilteredSum() {
        double sum = 0;
        for (Transaction t : filteredList) {
            sum += t.getTransactionType() == TransactionType.INCOME
                    ? t.getTransactionAmount()
                    : -t.getTransactionAmount();
        }

        if (!filteredList.isEmpty()) {
            totalSumTransaction.setText("Total: " + getString(R.string.expense_row_amount_template, sum));
            totalSumTransaction.setVisibility(View.VISIBLE);
            layout_total_transactions.setVisibility(View.VISIBLE);
            totalSumTransaction.setTextColor(ContextCompat.getColor(requireContext(),
                            sum > 0 ? R.color.income_color : sum < 0 ? R.color.expense_color : R.color.black));
        } else {
            totalSumTransaction.setText("");
            layout_total_transactions.setVisibility(View.GONE);
        }
    }

    private void resetFilterAllTransactions() {
        filteredList.clear();
        filteredList.addAll(transactionList);
        adapter.notifyDataSetChanged();
        recalculateFilteredSum();
    }


    private void formDate()
    {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MMMM yyyy",Locale.US);
        textMonth.setText(simpleDateFormat.format(currentCalendar.getTime()));
    }

    private void showTransactionMonth(Calendar currentCalendar)
    {
        //if they are in May, the fragment will display May 2025 at the top and display transactions from May
        int selectedMonth = currentCalendar.get(Calendar.MONTH);
        int selectedYear = currentCalendar.get(Calendar.YEAR);

        //take over all normal/scheduled transactions
        transactionViewModel.loadAllTransactions();
        transactionViewModel.getTransactions().observe(getViewLifecycleOwner(), list -> {
            double sum = 0;
            transactionList.clear();

            //for scheduled transactions that repeat (daily, weekly...) I need to display each time the transaction is successfully completed
            List<Transaction> expandedList = new ArrayList<>();
            for(Transaction t:list)
            {
                if(t instanceof ScheduledTransactions)
                {
                    ScheduledTransactions st = (ScheduledTransactions) t;
                    if (st.isMarkedForDeletionLocalOnly()) {//if transaction is marked as deleted
                        continue;
                    }
                    //We add the "base" instance, i.e. the first transaction made if startDate is in the current month
                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(st.getStartDate());
                    int year = cal1.get(Calendar.YEAR);
                    int month = cal1.get(Calendar.MONTH);
                    if (year == selectedYear && month == selectedMonth) {
                        st.setTransactionDate(st.getStartDate());//I only set it locally so I can do the transaction order correctly (in the database anyway the scheduled transactions have start date=date)
                        st.setCloneInstance(false);
                        st.setProcessed(true); //we use `processed` as a flag
                        expandedList.add(st);
                    }

                    for (Date date : st.getSuccessDates()) {
                        if (date.equals(st.getStartDate())) continue;
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        int y = cal.get(Calendar.YEAR);
                        int m = cal.get(Calendar.MONTH);
                        if (y == selectedYear && m == selectedMonth) {
                            ScheduledTransactions clone = null;
                            try {
                                clone = (ScheduledTransactions) st.clone();
                            } catch (CloneNotSupportedException e) {
                                throw new RuntimeException(e);
                            }
                            clone.setTransactionDate(date);
                            clone.setCloneInstance(true);
                            expandedList.add(clone);
                        }
                    }
                }else{
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(t.getTransactionDate());
                    int y = cal.get(Calendar.YEAR);
                    int m = cal.get(Calendar.MONTH);

                    if (y == selectedYear && m == selectedMonth) {
                        expandedList.add(t);
                    }
                }
            }
            Collections.sort(expandedList, (o1, o2) -> o2.getTransactionDate().compareTo(o1.getTransactionDate()));

            transactionList.addAll(expandedList);
            filteredList.clear();
            filteredList.addAll(transactionList);
            double filteredSum = 0;//
            for (Transaction t : filteredList) {
                filteredSum += t.getTransactionType() == TransactionType.INCOME
                        ? t.getTransactionAmount()
                        : -t.getTransactionAmount();
            }
            adapter.notifyDataSetChanged();

            if (!filteredList.isEmpty()) {
                totalSumTransaction.setText("Total: " + getString(R.string.expense_row_amount_template, filteredSum));
                totalSumTransaction.setVisibility(View.VISIBLE);
                layout_total_transactions.setVisibility(View.VISIBLE);

                if (filteredSum > 0) {
                    totalSumTransaction.setTextColor(ContextCompat.getColor(requireContext(), R.color.income_color));
                } else if (filteredSum < 0) {
                    totalSumTransaction.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_color));
                } else {
                    totalSumTransaction.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                }
            } else {
                totalSumTransaction.setText("");
                layout_total_transactions.setVisibility(View.GONE);
            }
        });
    }


    private void showOptionsDialog(Transaction transaction)
    {
        if (transaction instanceof ScheduledTransactions) {
            ScheduledTransactions st = (ScheduledTransactions) transaction;
            if (!st.isCloneInstanceLocalOnly()) {
                String[] options = {
                        "View the transaction",
                        "Stop schedule",
                        "Delete the transaction"
                };
                new AlertDialog.Builder(requireContext())
                        .setTitle("Options")
                        .setItems(options, ((dialog, which) -> {
                            switch (which) {
                                case 0:
                                    ((MainActivity) requireActivity()).openUpdateTransaction(transaction);
                                    break;
                                case 1: //all transactions that are part of the scheduled transaction so far are preserved, and those that are to be executed are canceled
                                    stopFutureScheduling((ScheduledTransactions) transaction);
                                    break;
                                case 2:
                                    showDeleteConfirmation(transaction);
                                    break;
                            }
                        }))
                        .setNegativeButton("Cancel", null)
                        .show();
            }else {
                String[] options = {"View the transaction"};
                new AlertDialog.Builder(requireContext())
                        .setTitle("Options")
                        .setItems(options, ((dialog, which) -> {
                            ((MainActivity) requireActivity()).openUpdateTransaction(transaction);
                        }))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        } else {
            String[] options = {
                    "Update the transaction",
                    "Delete the transaction"
            };
            new AlertDialog.Builder(requireContext())
                    .setTitle("Options")
                    .setItems(options, ((dialog, which) -> {
                        switch (which) {
                            case 0:
                                ((MainActivity) requireActivity()).openUpdateTransaction(transaction);
                                break;
                            case 1:
                                showDeleteConfirmation(transaction);
                                break;
                        }
                    }))
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private void stopFutureScheduling(ScheduledTransactions transaction) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Stop Scheduling Confirmation")
                .setMessage("Are you sure you want to stop future scheduling? Past executions will remain, but no future transactions will occur.")
                .setPositiveButton("Stop", (dialog, which) -> {
                    // end date = today
                    Date today = new Date();
                    transaction.setEndDate(today);
                    transaction.getFailedDates().clear();
                    transactionViewModel.updateTransaction(transaction);
                    Toast.makeText(requireContext(), "Future scheduling stopped successfully.", Toast.LENGTH_SHORT).show();
                    reloadData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    //delete simple transactions
    //Full DELETE for a scheduled transaction, taking into account: Successful executions (successDates), failed executions (failedDates), any future executions (which would otherwise appear as a “clone”)
    private void deleteTransaction(Transaction transaction) {
        if (transaction instanceof ScheduledTransactions) {
            ScheduledTransactions st = (ScheduledTransactions) transaction;
            FinancialAccount account = accountMap.get(transaction.getIdAccount());
            if (account == null) return;
            // rollback balance for all successful executions (successDate)
            for (Date execDate : st.getSuccessDates()) {
                if (st.getTransactionType() == TransactionType.EXPENSE) {
                    account.setCurrentBalance(account.getCurrentBalance() + st.getTransactionAmount());
                } else {
                    account.setCurrentBalance(account.getCurrentBalance() - st.getTransactionAmount());
                }
            }
            accountViewModel.updateAccount(account);
            accountViewModel.reloadAllAccountsFromFirebase();
            st.getFailedDates().clear();
            st.getSuccessDates().clear();
            st.setMarkedForDeletion(true);

            // delete the ScheduledTransaction from Firebase
            transactionViewModel.deleteTransaction(st.getIdTransaction(), new FirebaseTransactionRepository.RepositoryCallback() {
                @Override
                public void onSuccess() {
                    requireActivity().runOnUiThread(()->{
                        transactionViewModel.loadAllTransactionsR(()->{
                            reloadData();
                        });
                    });
                }

                @Override
                public void onError(String userIsNotAuthenticated) {
                    Log.e("Errors", "DeleteTransaction-Error deleting scheduled transaction: " + userIsNotAuthenticated);
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Could not delete transaction. Please try again.", Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            //normal transaction
            FinancialAccount account = accountMap.get(transaction.getIdAccount());
            if (account == null){
                return;
            }
            if (transaction.getTransactionType() == TransactionType.EXPENSE) {
                account.setCurrentBalance(account.getCurrentBalance() + transaction.getTransactionAmount());
            } else {
                account.setCurrentBalance(account.getCurrentBalance() - transaction.getTransactionAmount());
            }

            accountViewModel.updateAccount(account);
            accountViewModel.reloadAllAccountsFromFirebase();

            transactionViewModel.deleteTransaction(transaction.getIdTransaction(), new FirebaseTransactionRepository.RepositoryCallback() {
                @Override
                public void onSuccess() {
                    requireActivity().runOnUiThread(() -> {
                        transactionList.remove(transaction);
                        adapter.notifyDataSetChanged();
                        reloadData();
                        Toast.makeText(getContext(), R.string.the_transaction_has_been_deleted, Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(String error) {
                    Log.e("Errors", "DeleteTransaction-Error deleting transaction: " + error);
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Could not delete transaction. Please try again.", Toast.LENGTH_SHORT).show());
                }
            });

            Toast.makeText(getContext(), R.string.the_transaction_has_been_deleted, Toast.LENGTH_SHORT).show();
        }
    }


    public void reloadData() {
        showTransactionMonth(currentCalendar);
    }

    private void showDeleteConfirmation(Transaction transaction)
    {
        String message = (transaction instanceof ScheduledTransactions)
                ? "Warning: All processed, failed, and future scheduled transactions will be removed.Are you sure you want to continue?"
                : "Are you sure you want to delete this transaction?";

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Confirmation")
                .setMessage(message)
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteTransaction(transaction);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    //we use observeOnce() to ensure that the processScheduledTransactions() method processes scheduled transactions only once on each fragment refresh
    public static <T> void observeOnce(LiveData<T> liveData, LifecycleOwner owner, Observer<T> observer) {
        liveData.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                observer.onChanged(t);
                liveData.removeObserver(this);
            }
        });
    }

    private void processScheduledTransactions() {
        observeOnce(transactionViewModel.getTransactions(), getViewLifecycleOwner(), list -> {
            Date today = reserHour(new Date());

            List<FinancialAccount> allAccounts = accountViewModel.getList().getValue();
            accountMap.clear();
            if (allAccounts != null) {
                for (FinancialAccount acc : allAccounts) {
                    accountMap.put(acc.getIdAccount(), acc);
                }
                adapter.notifyDataSetChanged();
            }

            Collections.sort(list, (t1, t2) -> {
                if (t1 instanceof ScheduledTransactions && t2 instanceof ScheduledTransactions) {
                    ScheduledTransactions st1 = (ScheduledTransactions) t1;
                    ScheduledTransactions st2 = (ScheduledTransactions) t2;
                    return st1.getTransactionType() == TransactionType.INCOME ? -1 : 1;
                }
                return 0;
            });

            boolean accountsUpdated = false;
            Set<String> transactionsToUpdate = new HashSet<>();

            for (Transaction t : list) {
                if (t instanceof ScheduledTransactions) {
                    ScheduledTransactions st = (ScheduledTransactions) t;
                    if (st.isMarkedForDeletionLocalOnly()) {
                        Log.d("PROCESS_SKIP", "Tranzacția " + st.getIdTransaction() + " este marcată pentru ștergere. Ignor.");
                        continue;
                    }
                    String accountId = st.getIdAccount();
                    if (!accountMap.containsKey(accountId)) {
                        continue;
                    }
                    if (st.isCloneInstanceLocalOnly()) {
                        continue;
                    }
                    FinancialAccount account = accountMap.get(st.getIdAccount());
                    if (account == null) continue;

                    Date start = reserHour(st.getStartDate());
                    Date end = reserHour(st.getEndDate());
                    Date lastProcessed = st.getLastProcessedDate() != null ? reserHour(st.getLastProcessedDate()) : null;

                    // PARTEA 1: Procesează failed dates din trecut (păstrezi această parte)
                    List<Date> failedDatesCopy = new ArrayList<>(st.getFailedDates());
                    List<Date> updatedFailedDates = new ArrayList<>();

                    for (Date failedDayRaw : failedDatesCopy) {
                        Date failedDay = reserHour(failedDayRaw);
                        if (failedDay.after(today)) {
                            updatedFailedDates.add(failedDayRaw);
                            continue;
                        }

                        if (st.getTransactionType() == TransactionType.EXPENSE) {
                            if (account.getCurrentBalance() >= st.getTransactionAmount()) {
                                account.setCurrentBalance(account.getCurrentBalance() - st.getTransactionAmount());
                                accountsUpdated = true;
                                st.setLastProcessedDate(failedDay);
                                if (!st.getSuccessDates().contains(failedDay)) {
                                    st.getSuccessDates().add(failedDay);
                                }
                                transactionsToUpdate.add(st.getIdTransaction());

                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "A previously scheduled transaction was successfully processed", Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                updatedFailedDates.add(failedDayRaw);
                            }
                        } else {
                            account.setCurrentBalance(account.getCurrentBalance() + st.getTransactionAmount());
                            accountsUpdated = true;
                            st.setLastProcessedDate(failedDay);
                            if (!st.getSuccessDates().contains(failedDay)) {
                                st.getSuccessDates().add(failedDay);
                            }
                            transactionsToUpdate.add(st.getIdTransaction());
                        }
                    }

                    st.setFailedDates(updatedFailedDates);

                    // PARTEA 2: Procesează zilele ratate + ziua curentă (NOUA LOGICĂ)
                    if (!today.before(start) && !today.after(end)) {
                        // Găsește toate zilele care trebuiau procesate între lastProcessed și today
                        List<Date> datesToProcess = getAllMissedProcessingDates(lastProcessed, today, st.getTransactionFrequency(), start);

                        for (Date dateToProcess : datesToProcess) {
                            // Skip dacă deja procesată cu succes
                            if (st.getSuccessDates().contains(dateToProcess)) {
                                continue;
                            }

                            // Skip dacă deja este în failed dates
                            boolean alreadyFailed = false;
                            for (Date failed : st.getFailedDates()) {
                                if (reserHour(failed).equals(dateToProcess)) {
                                    alreadyFailed = true;
                                    break;
                                }
                            }
                            if (alreadyFailed) {
                                continue;
                            }

                            if (st.getTransactionType() == TransactionType.EXPENSE) {
                                if (account.getCurrentBalance() >= st.getTransactionAmount()) {
                                    // Succes - procesează tranzacția
                                    account.setCurrentBalance(account.getCurrentBalance() - st.getTransactionAmount());
                                    accountsUpdated = true;
                                    st.setLastProcessedDate(dateToProcess);
                                    if (!st.getSuccessDates().contains(dateToProcess)) {
                                        st.getSuccessDates().add(dateToProcess);
                                    }
                                    transactionsToUpdate.add(st.getIdTransaction());
                                } else {
                                    // Eșec - adaugă la failed dates
                                    st.getFailedDates().add(dateToProcess);
                                    transactionsToUpdate.add(st.getIdTransaction());

                                    // Trimite notificare doar pentru ziua curentă
                                    if (dateToProcess.equals(today)) {
                                        sendFailedTransactionNotification(
                                                requireContext(),
                                                st.getIdAccount(),
                                                dateToProcess,
                                                st.getTransactionAmount()
                                        );
                                    }
                                }
                            } else {
                                // INCOME - întotdeauna reușește
                                account.setCurrentBalance(account.getCurrentBalance() + st.getTransactionAmount());
                                accountsUpdated = true;
                                st.setLastProcessedDate(dateToProcess);
                                if (!st.getSuccessDates().contains(dateToProcess)) {
                                    st.getSuccessDates().add(dateToProcess);
                                }
                                transactionsToUpdate.add(st.getIdTransaction());
                            }
                        }
                    }
                }
            }

            // Update toate tranzacțiile modificate
            for (String transactionId : transactionsToUpdate) {
                for (Transaction t : list) {
                    if (t instanceof ScheduledTransactions && t.getIdTransaction().equals(transactionId)) {
                        transactionViewModel.updateTransaction((ScheduledTransactions) t);
                        break;
                    }
                }
            }

            // Update conturile doar dacă au fost modificări
            if (accountsUpdated) {
                for (String accountId : accountMap.keySet()) {
                    FinancialAccount account = accountMap.get(accountId);
                    if (account != null) {
                        accountViewModel.updateAccount(account);
                    }
                }
                accountViewModel.reloadAllAccountsFromFirebase();
                reloadData();
            }
        });
    }

    // Metodă îmbunătățită pentru a găsi TOATE zilele ratate + ziua curentă
    private List<Date> getAllMissedProcessingDates(Date lastProcessed, Date today, String frequency, Date startDate) {
        List<Date> datesToProcess = new ArrayList<>();

        today = reserHour(today);
        startDate = reserHour(startDate);

        Calendar calendar = Calendar.getInstance();

        if (lastProcessed == null) {
            // Prima dată - începe de la startDate
            calendar.setTime(startDate);
        } else {
            // Continuă de la următoarea dată după lastProcessed
            calendar.setTime(reserHour(lastProcessed));
            addFrequencyToCalendar(calendar, frequency);
        }

        // Adaugă toate zilele până la today (inclusiv)
        while (!calendar.getTime().after(today)) {
            Date currentDate = reserHour(calendar.getTime());

            // Verifică că nu este înainte de startDate
            if (!currentDate.before(startDate)) {
                datesToProcess.add(currentDate);
            }

            addFrequencyToCalendar(calendar, frequency);
        }

        return datesToProcess;
    }

    // Metodă helper pentru adăugarea frecvenței la calendar
    private void addFrequencyToCalendar(Calendar calendar, String frequency) {
        switch (frequency) {
            case "Daily":
                calendar.add(Calendar.DATE, 1);
                break;
            case "Weekly":
                calendar.add(Calendar.DATE, 7);
                break;
            case "Monthly":
                calendar.add(Calendar.MONTH, 1);
                break;
            case "Annually":
                calendar.add(Calendar.YEAR, 1);
                break;
        }
    }

    private void sendFailedTransactionNotification(Context context, String accountId, Date failDate, double amount) {
        // Create a unique key for this notification to prevent duplicates
        String notificationKey = accountId + "_" + new SimpleDateFormat("yyyyMMdd").format(failDate) + "_" + amount;

        // Check if we've already sent this notification today
        if (sentNotifications.contains(notificationKey)) {
            return; // Skip sending duplicate notification
        }

        // Get the actual account name instead of using the ID
        FinancialAccount account = accountMap.get(accountId);
        String accountName = (account != null) ? account.getAccountName() : accountId;

        String message = amount + " RON could not be processed - " + accountName +
                " on " + new SimpleDateFormat("dd/MM/yyyy").format(failDate) +
                " due to insufficient funds.";

        String channel_id = "budgetwise_channel";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Starting with Android 8 (Oreo), notifications must be sent to a "channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "BudgetWise";
            String description = "Channel for scheduled transaction notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channel_id, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        //When the user taps the notification, the app opens
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        //the class that creates a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel_id)
                .setSmallIcon(R.drawable.baseline_error_24)
                .setContentTitle("Scheduled transaction failed")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Use a unique ID based on time and transaction details to avoid replacing notifications
        int notificationId = (accountId + amount).hashCode() + (int)System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());

        // Remember that we've sent this notification
        sentNotifications.add(notificationKey);
    }

    //I set the date so that hours, minutes, seconds, milliseconds do not appear
    private Date reserHour(Date d)
    {
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(d);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTime();
    }

}
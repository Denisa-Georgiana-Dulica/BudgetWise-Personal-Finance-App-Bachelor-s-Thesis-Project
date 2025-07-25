package com.example.budgetwise.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.budgetwise.MainActivity;
import com.example.budgetwise.R;
import com.example.budgetwise.adapter.AccountAdapter;
import com.example.budgetwise.adapter.LimitAdapter;
import com.example.budgetwise.classes.FinancialAccount;
import com.example.budgetwise.classes.Limit;
import com.example.budgetwise.classes.ScheduledTransactions;
import com.example.budgetwise.classes.Transaction;
import com.example.budgetwise.classes.TransactionType;
import com.example.budgetwise.viewmodel.AccountViewModel;
import com.example.budgetwise.viewmodel.CategoryViewModel;
import com.example.budgetwise.viewmodel.LimitViewModel;
import com.example.budgetwise.viewmodel.TransactionViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class LimitFragment extends Fragment {

    private ListView listView;
    private List<Limit> limitsList=new ArrayList<>();
    private LimitAdapter adapter;
    private CategoryViewModel categoryViewModel;
    private LimitViewModel limitViewModel;

    private TransactionViewModel transactionViewModel;

    public LimitFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setVisibilityFab(false);
        }
       reloadData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_limit, container, false);
        listView=view.findViewById(R.id.listViewLimit);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        limitViewModel= new ViewModelProvider(this).get(LimitViewModel.class);
        adapter=new LimitAdapter(getContext().getApplicationContext(),R.layout.limit_row,limitsList,getLayoutInflater(),categoryViewModel);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Limit selectedLimit=limitsList.get(position);
                showOptionsDialog(selectedLimit);
            }
        });

        observeData();
        reloadData();
        return view;

    }

    private void showOptionsDialog(Limit selectedLimit) {
        String[] options = {
                "Update the limit",
                "Delete the limit"
        };
        new AlertDialog.Builder(requireContext())
                .setTitle("Options")
                .setItems(options, ((dialog, which) -> {
                    switch (which) {
                        case 0:
                            ((MainActivity) requireActivity()).openUpdateLimit(selectedLimit);
                            break;
                        case 1:
                            showDeleteConfirmation(selectedLimit);
                            break;
                    }
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmation(Limit selectedLimit)
    {
        String message ="Are you sure you want to delete this limit?";

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Confirmation")
                .setMessage(message)
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteLimit(selectedLimit);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteLimit(Limit selectedLimit) {
        if (selectedLimit != null && selectedLimit.getIdLimit() != null) {
            limitViewModel.deleteLimit(selectedLimit.getIdLimit());

            // Observe the success status
            limitViewModel.getSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
                if (isSuccess != null && isSuccess) {
                    // Remove from local list if Firebase deletion was successful
                    limitsList.remove(selectedLimit);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(), "Limit deleted successfully", Toast.LENGTH_SHORT).show();
                }
            });

            // Observe errors
            limitViewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
                if (errorMsg != null && !errorMsg.isEmpty()) {
                    Toast.makeText(requireContext(), "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(requireContext(), "Cannot delete limit: Invalid ID", Toast.LENGTH_LONG).show();
        }
    }

    private void observeData() {
        categoryViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
        });

        limitViewModel.getLimits().observe(getViewLifecycleOwner(), limits -> {
            transactionViewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
                showLimits(limits, transactions);
            });
        });

        limitViewModel.getSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Log.d("Errors", "LimitFragment-Success state: " + success);
            }
        });

        limitViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e("Errors", "LimitFragment-: " + error);
            }
        });
    }
    private void showLimits(List<Limit> limits, List<Transaction> allTransactions) {
        limitsList.clear();

        for (Limit limit : limits) {
            List<Transaction> expanded = expandScheduledTransactionsForLimit(
                    allTransactions, limit.getDateStartLimit(), limit.getDateFinalLimit());

            double spent = 0.0;
            for (Transaction t : expanded) {
                if (t.getTransactionType() == TransactionType.EXPENSE &&
                        t.getTransactionCategory().getIdCategory().equals(limit.getIdCategory())) {
                    spent += Math.abs(t.getTransactionAmount());
                }
            }

            limit.setSpentAmount(spent);
            limitViewModel.updateLimit(limit);
            limitsList.add(limit);
        }

        adapter.notifyDataSetChanged();
    }

    public List<Transaction> expandScheduledTransactionsForLimit(List<Transaction> originalList, Date startDate, Date endDate)
    {
        List<Transaction> result=new ArrayList<>();

        for(Transaction t:originalList)
        {
            if(t instanceof ScheduledTransactions)
            {
                ScheduledTransactions st= (ScheduledTransactions) t;
                if(st.getSuccessDates().contains(st.getStartDate()) && !st.getStartDate().before(startDate) && !st.getStartDate().after(endDate))
                {
                    result.add(st);
                }

                for(Date d:st.getSuccessDates())
                {
                    if(d.equals(st.getStartDate()))
                    {
                        continue;
                    }
                    if(!d.before(startDate) && !d.after(endDate))
                    {
                        try{
                            ScheduledTransactions clone = (ScheduledTransactions) st.clone();
                            clone.setTransactionDate(d);
                            clone.setCloneInstance(true);
                            result.add(clone);
                        }catch (CloneNotSupportedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }else{
                //Normal transaction
                Date transactionDate = t.getTransactionDate();
                if (!transactionDate.before(startDate) && !transactionDate.after(endDate)) {
                    result.add(t);
                }
            }
        }
        return result;
    }


    public void reloadData() {
        categoryViewModel.loadCategories();
        transactionViewModel.loadAllTransactions();
        limitViewModel.loadLimits();
    }

}
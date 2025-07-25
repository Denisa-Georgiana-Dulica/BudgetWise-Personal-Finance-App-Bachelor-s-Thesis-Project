package com.example.budgetwise.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.budgetwise.MainActivity;
import com.example.budgetwise.R;
import com.example.budgetwise.adapter.AccountAdapter;
import com.example.budgetwise.classes.FinancialAccount;
import com.example.budgetwise.classes.Transaction;
import com.example.budgetwise.classes.TransactionType;
import com.example.budgetwise.viewmodel.AccountViewModel;
import com.example.budgetwise.viewmodel.TransactionViewModel;

import java.util.ArrayList;
import java.util.List;

public class AccountsFragment extends Fragment {
    private ListView listView;
    private List<FinancialAccount> accountsList;
    private AccountAdapter adapter;
    private AccountViewModel accountViewModel;
    private TextView totalAccounts;

    public AccountsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setVisibilityFab(false);
        }
        accountViewModel.reloadAllAccountsFromFirebase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accounts, container, false);
        listView=view.findViewById(R.id.listViewAccount);
        totalAccounts=view.findViewById(R.id.totalAccounts);
        totalAccounts.setVisibility(View.INVISIBLE);
        accountsList=new ArrayList<>();
        adapter=new AccountAdapter(getContext().getApplicationContext(),R.layout.account_row,accountsList,getLayoutInflater());
        listView.setAdapter(adapter);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        showAccounts();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FinancialAccount selectedAccount = accountsList.get(position);
                showOptionsDialog(selectedAccount);
            }
        });

        return view;
    }

    public void showAccounts()
    {
        accountViewModel.getAllAccounts();
        accountViewModel.getList().observe(getViewLifecycleOwner(),list->{
            double sum=0;
            accountsList.clear();
            accountsList.addAll(list);
            for (FinancialAccount f : accountsList) {
                sum += f.getCurrentBalance();
            }
            adapter.notifyDataSetChanged();
            totalAccounts.setText("Total: " + getString(R.string.expense_row_amount_template, sum));
            totalAccounts.setVisibility(View.VISIBLE);
            if (sum > 0) {
                totalAccounts.setTextColor(ContextCompat.getColor(requireContext(), R.color.income_color));
            } else if (sum <= 0) {
                totalAccounts.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_color));
            }
        });
    }

    private void showOptionsDialog(FinancialAccount account)
    {
        //requireContext - returns the activity context associated with the fragment //compared to getContext(), it never returns null, but throws an exception
        String[] options={
                "Update the account",
                "Delete the account"
        };
        if (!"Wallet".equalsIgnoreCase(account.getAccountName())){
            new AlertDialog.Builder(requireContext()).setTitle("Options").setItems(options,((dialog, which) -> {
                switch (which)
                {
                    case 0:
                        ((MainActivity) requireActivity()).openUpdateAccount(account);
                        break;
                    case 1:
                        showDeleteConfirmation(account);
                        break;
                    case 2:
                        // showTransactionDetails(transaction);
                        break;
                    default:
                        break;
                }
            })).setNegativeButton("Cancel",null).show();
        }
    }

    private void deleteAccount(FinancialAccount account)
    {
        accountViewModel.deleteAccount(account.getIdAccount());
        accountViewModel.getSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                accountsList.remove(account);
                adapter.notifyDataSetChanged();
                Toast.makeText(getContext(), R.string.account_deleted, Toast.LENGTH_SHORT).show();
                showAccounts();

            } else {
                Toast.makeText(getContext(), R.string.failed_to_delete_account, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showDeleteConfirmation(FinancialAccount account)
    {
        new AlertDialog.Builder(requireContext()).setTitle("Delete confirmation").setMessage("Are you sure you want to delete this account?\nAll associated transactions will be deleted.").setPositiveButton("Delete",((dialog, which) -> {
            deleteAccount(account);
        })).setNegativeButton("Cancel",null).show();
    }

}
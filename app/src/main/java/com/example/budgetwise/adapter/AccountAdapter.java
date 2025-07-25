package com.example.budgetwise.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.budgetwise.R;
import com.example.budgetwise.classes.FinancialAccount;
import com.example.budgetwise.classes.Transaction;
import com.example.budgetwise.classes.TransactionType;

import java.util.List;

public class AccountAdapter extends ArrayAdapter<FinancialAccount> {
    private Context context;
    private int resource;
    private List<FinancialAccount> accounts;
    private LayoutInflater inflater;
    public AccountAdapter(@NonNull Context context, int resource, @NonNull List<FinancialAccount> objects,LayoutInflater inflater) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.accounts = objects;
        this.inflater = inflater;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = inflater.inflate(resource, parent, false);
        FinancialAccount account=accounts.get(position);
        TextView accountName=view.findViewById(R.id.text_account_name);
        accountName.setText(account.getAccountName());
        TextView accountType=view.findViewById(R.id.text_account_type);
        accountType.setText(account.getAccountType().toString());
        TextView amount=view.findViewById(R.id.text_account_balance);
        amount.setText(String.format("%.2f RON", account.getCurrentBalance()));
        if (account.getCurrentBalance() >0) {
            amount.setTextColor(ContextCompat.getColor(context, R.color.income_color));
        } else {
            amount.setTextColor(ContextCompat.getColor(context, R.color.expense_color));
        }
        return view;
    }
}

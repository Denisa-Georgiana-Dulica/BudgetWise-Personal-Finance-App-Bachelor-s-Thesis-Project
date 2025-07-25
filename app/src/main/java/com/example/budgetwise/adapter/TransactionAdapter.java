package com.example.budgetwise.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.budgetwise.DateConverter;
import com.example.budgetwise.R;
import com.example.budgetwise.classes.FinancialAccount;
import com.example.budgetwise.classes.ScheduledTransactions;
import com.example.budgetwise.classes.Transaction;
import com.example.budgetwise.classes.TransactionType;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionAdapter extends ArrayAdapter<Transaction> {

    private Context context;
    private int resource;
    private List<Transaction> transactions;
    private LayoutInflater inflater;
    private Map<String, FinancialAccount> accountMap = new HashMap<>();

    public TransactionAdapter(@NonNull Context context, int resource, @NonNull List<Transaction> objects,LayoutInflater inflater, Map<String, FinancialAccount> accountMap) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.transactions = objects;
        this.inflater = inflater;
        this.accountMap = accountMap;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = inflater.inflate(resource, parent, false);
        Transaction transaction=transactions.get(position);
        ImageView imageView=view.findViewById(R.id.iconCategoryTransaction_img);
        int imageResId = context.getResources().getIdentifier(transaction.getTransactionCategory().getIconRes(), "drawable", context.getPackageName());
        if (imageResId != 0) {
            imageView.setImageResource(imageResId);
        } else {
            imageView.setImageResource(R.drawable.loading);
        }
        CardView cardView = view.findViewById(R.id.cardViewTransaction);
        if (transaction instanceof ScheduledTransactions) {
            ScheduledTransactions st = (ScheduledTransactions) transaction;
            if (st.isCloneInstanceLocalOnly()) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.scheduale));
            }else if(st.isProcessed()){
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.light_gray));
            }

        } else {
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
        }
        TextView titleTransaction=view.findViewById(R.id.titleTransaction);
        titleTransaction.setText(transaction.getTransactionCategory().getCategoryName());
        TextView accountName=view.findViewById(R.id.accountNameTransaction);
        FinancialAccount account = accountMap.get(transaction.getIdAccount());
        if (account != null) {
            accountName.setText(account.getAccountName());
        }
        TextView amount=view.findViewById(R.id.amountTransaction);
        amount.setText(context.getString(R.string.expense_row_amount_template,transaction.getTransactionAmount()));
        if (transaction.getTransactionType() == TransactionType.INCOME) {
            amount.setTextColor(ContextCompat.getColor(context, R.color.income_color));
        } else {
            amount.setTextColor(ContextCompat.getColor(context, R.color.expense_color));
        }
        TextView date=view.findViewById(R.id.dateTransaction);
        date.setText(DateConverter.fromDate(transaction.getTransactionDate()));
        return view;
    }


}

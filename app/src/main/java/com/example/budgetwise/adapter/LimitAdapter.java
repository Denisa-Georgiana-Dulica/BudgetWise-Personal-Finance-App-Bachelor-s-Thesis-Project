package com.example.budgetwise.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.budgetwise.DateConverter;
import com.example.budgetwise.R;
import com.example.budgetwise.classes.FinancialAccount;
import com.example.budgetwise.classes.Limit;
import com.example.budgetwise.classes.ScheduledTransactions;
import com.example.budgetwise.classes.Transaction;
import com.example.budgetwise.classes.TransactionType;
import com.example.budgetwise.viewmodel.CategoryViewModel;

import java.util.List;

public class LimitAdapter extends ArrayAdapter<Limit> {

    private Context context;
    private int resource;
    private List<Limit> limits;
    private LayoutInflater inflater;
    private CategoryViewModel categoryViewModel;
    private TextView warningText;

    public LimitAdapter(@NonNull Context context, int resource, @NonNull List<Limit> objects,LayoutInflater inflater,CategoryViewModel categoryViewModel) {
        super(context, resource, objects);
        this.context=context;
        this.resource=resource;
        this.limits=objects;
        this.inflater=inflater;
        this.categoryViewModel = categoryViewModel;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = inflater.inflate(resource, parent, false);
        Limit limit=limits.get(position);
        TextView categoryName = view.findViewById(R.id.text_category_name);
        TextView startDate = view.findViewById(R.id.text_start_date);
        TextView endDate = view.findViewById(R.id.text_end_date);
        TextView spentText = view.findViewById(R.id.text_spent);
        ProgressBar progressBar = view.findViewById(R.id.progress_limit);
        TextView maxText = view.findViewById(R.id.text_limit_max);
        TextView remainingValue = view.findViewById(R.id.text_remaining_value);
        TextView warningText=view.findViewById(R.id.text_limit_warning);

        String catName = categoryViewModel.getCategoryNameById(limit.getIdCategory());
        categoryName.setText(catName);

        startDate.setText(DateConverter.fromDate(limit.getDateStartLimit()));
        endDate.setText(DateConverter.fromDate(limit.getDateFinalLimit()));

        double max = limit.getMaxSum();
        double spent = limit.getSpentAmount(); ;
        double remaining = max - spent;
        int progress = (max > 0) ? (int) ((spent / max) * 100) : 0;

        spentText.setText(String.format("%.2f RON", spent));
        maxText.setText(String.format("%.2f RON", max));
        remainingValue.setText(String.format("%.2f RON", remaining));
        progressBar.setProgress(progress);

        if (spent > max) {
            progressBar.setProgressTintList(ContextCompat.getColorStateList(context, android.R.color.holo_red_dark));
            warningText.setVisibility(View.VISIBLE);
            double exceeded = spent - max;
            warningText.setText(String.format("The limit has been exceeded with %.2f RON", exceeded));
        } else {
            warningText.setVisibility(View.GONE);
        }

        return view;
    }
}

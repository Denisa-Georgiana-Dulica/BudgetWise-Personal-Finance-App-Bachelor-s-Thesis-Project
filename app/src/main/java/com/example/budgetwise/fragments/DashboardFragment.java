package com.example.budgetwise.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.budgetwise.MainActivity;
import com.example.budgetwise.R;
import com.example.budgetwise.adapter.TransactionAdapter;
import com.example.budgetwise.classes.FinancialAccount;
import com.example.budgetwise.classes.ScheduledTransactions;
import com.example.budgetwise.classes.Transaction;
import com.example.budgetwise.classes.TransactionType;
import com.example.budgetwise.viewmodel.AccountViewModel;
import com.example.budgetwise.viewmodel.TransactionViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private PieChart piechart;
    private ListView listView;
    private TransactionViewModel transactionViewModel;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList=new ArrayList<>();
    private Calendar currentCalendar;
    private ImageView centerImage;
    private Map<String, FinancialAccount> accountMap = new HashMap<>();
    private View emptyStateView;
    private AccountViewModel accountViewModel;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setVisibilityFab(false);
        }
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
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        piechart=view.findViewById(R.id.pieChartDashboard);
        listView=view.findViewById(R.id.lv_piechart);
        transactionAdapter=new TransactionAdapter(getContext(),R.layout.transaction_row,transactionList,getLayoutInflater(),accountMap);
        listView.setAdapter(transactionAdapter);
        emptyStateView=view.findViewById(R.id.emptyStateView);
        centerImage = view.findViewById(R.id.centerImage);

        piechart.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        centerImage.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.GONE);

        currentCalendar= Calendar.getInstance();
        transactionViewModel=new ViewModelProvider(this).get(TransactionViewModel.class);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

        accountViewModel.getList().observe(getViewLifecycleOwner(), accounts -> {
            accountMap.clear();
            for (FinancialAccount acc : accounts) {
                accountMap.put(acc.getIdAccount(), acc);
            }
            transactionAdapter.notifyDataSetChanged();
        });
        accountViewModel.reloadAllAccountsFromFirebase();
        getTransactionsForMonth(currentCalendar, transactions -> {
            transactionList.clear();
            transactionList.addAll(transactions);
            transactionAdapter.notifyDataSetChanged();
            showPieChart(transactionList);
        });


        return view;
    }

    private void getTransactionsForMonth(Calendar currentCalendar, OnTransactionsLoadedListener listener) {
        int selectedMonth = currentCalendar.get(Calendar.MONTH);
        int selectedYear = currentCalendar.get(Calendar.YEAR);

        transactionViewModel.loadAllTransactions();
        transactionViewModel.getTransactions().observe(getViewLifecycleOwner(), list -> {
            List<Transaction> expandedList = expandScheduledTransactions(list, selectedMonth, selectedYear);
            Collections.sort(expandedList, (o1, o2) -> o2.getTransactionDate().compareTo(o1.getTransactionDate()));
            listener.onTransactionsLoaded(expandedList);
        });
    }

    public interface OnTransactionsLoadedListener {
        void onTransactionsLoaded(List<Transaction> tranzactii);
    }

    private void showPieChart(List<Transaction> transactionList)
    {
        float sumIncome=0f;
        float sumExpense=0f;

        for(Transaction t:transactionList)
        {
            if(t.getTransactionType()==TransactionType.INCOME)
            {
                sumIncome+=(float)t.getTransactionAmount();
            }else {
                sumExpense+=(float)t.getTransactionAmount();
            }
        }

        if (transactionList.isEmpty()) {
            piechart.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
            centerImage.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
            return;
        } else {
            piechart.setVisibility(View.VISIBLE);
            listView.setVisibility(View.VISIBLE);
            centerImage.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
            centerImage.setImageResource(R.drawable.cost);
        }

        List<PieEntry> entries=new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        if(sumIncome>0)
        {
            entries.add(new PieEntry(sumIncome,"Income"));
            colors.add(ContextCompat.getColor(getContext(), R.color.income_color));
        }
        if(sumExpense>0)
        {
            entries.add(new PieEntry(sumExpense,"Expenses"));
            colors.add(ContextCompat.getColor(getContext(), R.color.expense_color));
        }

        PieDataSet dataSet=new PieDataSet(entries,"");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(15f);

        PieData data=new PieData(dataSet);
        piechart.setData(data);
        piechart.getDescription().setEnabled(false);
        piechart.setDrawHoleEnabled(true);//hole in the middle
        piechart.invalidate(); // Redraw
    }

    //for scheduled transactions that repeat (daily, weekly...) I need to display each time the transaction is successfully completed
    public List<Transaction> expandScheduledTransactions(List<Transaction> originalList, int month, int year)
    {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : originalList) {
            if(t instanceof ScheduledTransactions)
            {
                ScheduledTransactions st= (ScheduledTransactions) t;

                Calendar startCal = Calendar.getInstance();
                startCal.setTime(st.getStartDate());
                boolean isInMonth = startCal.get(Calendar.YEAR) == year && startCal.get(Calendar.MONTH) == month;

                if (isInMonth && st.getSuccessDates().contains(st.getStartDate())) {
                    st.setTransactionDate(st.getStartDate());
                    st.setCloneInstance(false);
                    st.setProcessed(true);
                    result.add(st);
                }

                for (Date d : st.getSuccessDates()) {
                    if (d.equals(st.getStartDate())) continue;
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month) {
                        ScheduledTransactions clone = null;
                        try {
                            clone = (ScheduledTransactions) st.clone();
                        } catch (CloneNotSupportedException e) {
                            throw new RuntimeException(e);
                        }
                        clone.setTransactionDate(d);
                        clone.setCloneInstance(true);
                        result.add(clone);
                    }
                }
            }else{
                Calendar cal = Calendar.getInstance();
                cal.setTime(t.getTransactionDate());
                if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month) {
                    result.add(t);
                }
            }
        }
        return result;
    }


}
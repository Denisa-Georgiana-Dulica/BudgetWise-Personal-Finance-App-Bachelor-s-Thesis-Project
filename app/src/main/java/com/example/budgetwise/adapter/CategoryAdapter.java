package com.example.budgetwise.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.budgetwise.R;
import com.example.budgetwise.classes.Transaction;
import com.example.budgetwise.classes.TransactionCategory;

import java.util.List;
import java.util.Locale;

public class CategoryAdapter extends ArrayAdapter<TransactionCategory> {
    private Context context;
    private int resource;
    private List<TransactionCategory> categories;
    private LayoutInflater inflater;

    public CategoryAdapter(@NonNull Context context, int resource, @NonNull List<TransactionCategory> objects, LayoutInflater inflater) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.categories = objects;
        this.inflater = inflater;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view=inflater.inflate(resource,parent,false);
        TransactionCategory transactionCategory=categories.get(position);

        TextView tvName=view.findViewById(R.id.iconCategory_tv);
        tvName.setText(transactionCategory.getCategoryName());

        ImageView imgCategory=view.findViewById(R.id.iconCategory_img);
        int imageResId = context.getResources().getIdentifier(transactionCategory.getIconRes(), "drawable", context.getPackageName());
        if (imageResId != 0) {
            imgCategory.setImageResource(imageResId);
        } else {
            imgCategory.setImageResource(R.drawable.loading);
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent); // Folosește exact același layout
    }

}

package com.example.budgetwise.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.budgetwise.MainActivity;
import com.example.budgetwise.R;
import com.example.budgetwise.adapter.CategoryAdapter;
import com.example.budgetwise.api.ApiClient;
import com.example.budgetwise.api.LineChartResponse;
import com.example.budgetwise.api.ReportApi;
import com.example.budgetwise.api.ReportResponse;
import com.example.budgetwise.classes.TransactionCategory;
import com.example.budgetwise.viewmodel.CategoryViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ReportFragment extends Fragment {

    private ImageView chart;
    private Spinner spinner;
    private CategoryViewModel categoryViewModel;
    private Call<ReportResponse> barChartCall;
    private Call<LineChartResponse> lineChartCall;
    private CheckBox checkboxBarChart;
    private CheckBox checkboxLineChart;



    public ReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_report, container, false);
        chart = view.findViewById(R.id.imageViewChart);
        spinner = view.findViewById(R.id.spinnerCategory);
        checkboxBarChart = view.findViewById(R.id.checkboxBarChart);
        checkboxLineChart = view.findViewById(R.id.checkboxLineChart);
        chart.setVisibility(View.GONE);
        spinner.setVisibility(View.GONE);

        checkboxBarChart.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkboxLineChart.setChecked(false);
                chart.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.GONE);
                loadBarChart();
            } else {
                chart.setImageDrawable(null);
                chart.setVisibility(View.GONE);
            }
        });

        checkboxLineChart.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkboxBarChart.setChecked(false);
                chart.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.VISIBLE);
                if (spinner.getSelectedItem() != null) {
                    String selectedCategory = spinner.getSelectedItem().toString();
                    loadLineChartForCategory(selectedCategory);
                }
            } else {
                chart.setImageDrawable(null);
                chart.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
            }
        });
        initializeViewModel();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setVisibilityFab(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelOngoingCalls();
    }

    private void loadBarChart() {
        if (barChartCall != null && !barChartCall.isCanceled()) { //an old request has been made
            barChartCall.cancel();
        }

        Retrofit retrofit = ApiClient.getRetrofit(true); //responsible for HTTP requests
        ReportApi api = retrofit.create(ReportApi.class); //get endpoints

        barChartCall = api.getBarChart(); //request to the barChart endpoint
        barChartCall.enqueue(new Callback<ReportResponse>() { //executes asynchronously, without blocking the UI
            @Override
            public void onResponse(Call<ReportResponse> call, Response<ReportResponse> response) { //the response from the server comes successfully 200
                if (response.isSuccessful() && response.body() != null) {
                    ReportResponse reportResponse = response.body();

                    if (reportResponse.barChart != null && !reportResponse.barChart.isEmpty()) {
                        displayBase64Image(reportResponse.barChart, chart);
                    } else {
                        Toast.makeText(getContext(), "No chart data received", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("Errors", "ReportFragment-Could not read error body: " + e.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ReportResponse> call, Throwable t) {
                if (!call.isCanceled()) {
                    t.printStackTrace();
                    Log.e("Errors", "ReportFragment-Bar chart error", t);
                    Toast.makeText(getContext(), "Couldn't load bar chart", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadLineChart(String category) {
        if (lineChartCall != null && !lineChartCall.isCanceled()) { //old request was made and not canceled
            lineChartCall.cancel();
        }

        Retrofit retrofit = ApiClient.getRetrofit(true); //you give it the address where it needs to go, IP emulator
        ReportApi api = retrofit.create(ReportApi.class); //interface, create a list of requests

        lineChartCall = api.getLineChart(category); //endpoint
        lineChartCall.enqueue(new Callback<LineChartResponse>() {
            @Override
            public void onResponse(Call<LineChartResponse> call, Response<LineChartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayBase64Image(response.body().lineChart, chart);
                } else {
                    chart.setImageDrawable(null);
                }
            }

            @Override
            public void onFailure(Call<LineChartResponse> call, Throwable t) {
                if (!call.isCanceled()) {
                    Log.e("Errors", "ReportFragment-Line chart failure", t);
                    Toast.makeText(getContext(), "Couldn't load line chart", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadLineChartForCategory(String categoryName) {
        try {
            //Unicode Transformation Format - 8-bit is a way of representing characters as strings of bits
            String encodedCategory = java.net.URLEncoder.encode(categoryName, "UTF-8"); //convert the category name to a URL-safe format (ex: /)
            loadLineChart(encodedCategory);
        } catch (Exception e) {
            Log.e("Errors", "ReportFragment-Could not read error body: " + e.getMessage());
        }
    }
    private void setupSpinner(List<TransactionCategory> categories) {
        List<TransactionCategory> categoryNames = new ArrayList<>();
        for (TransactionCategory category : categories) {
            if ("EXPENSE".equalsIgnoreCase(category.getType())) {
                categoryNames.add(category);
            }
        }

        if (!categoryNames.isEmpty()) {
            CategoryAdapter adapter = new CategoryAdapter(getContext(), R.layout.category_row, categoryNames, getLayoutInflater());
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (checkboxLineChart.isChecked()) {
                        TransactionCategory selected = categoryNames.get(position);
                        loadLineChartForCategory(selected.getCategoryName());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    chart.setImageDrawable(null);
                }
            });
            //is displayed by default for Food/Drinks (first category)
            if (!categoryNames.isEmpty() && checkboxLineChart.isChecked()) {
                loadLineChartForCategory(categoryNames.get(0).getCategoryName());
            }
        }
    }
    private void initializeViewModel() {
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);//An instance of CategoryViewModel is created
        categoryViewModel.loadCategories();//load the categories from the database
        categoryViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> { //getCategories() returns a LiveData that emits a list of transaction categories and observe(...) means that this fragment will listen when LiveData sends new data
            if (categories != null && !categories.isEmpty()) {
                setupSpinner(categories);
            }
        });
    }

    private void displayBase64Image(String base64String, ImageView imageView) {
        if (base64String == null || base64String.isEmpty()) {
            return;
        }

        try {
            String cleanBase64 = base64String;
            if (base64String.startsWith("data:image")) {
                int commaIndex = base64String.indexOf(','); //cut everything before , so that only the Base64 part remains
                if (commaIndex != -1) { // exist data:image/png;base64,
                    cleanBase64 = base64String.substring(commaIndex + 1);
                }
            }

            byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);//converts the bytes into an image (Bitmap) which is displayable by Android

            if (bitmap != null && getActivity() != null) {//UI display must be done on the main thread
                    getActivity().runOnUiThread(() -> {
                        imageView.setImageBitmap(bitmap);
                        imageView.setVisibility(View.VISIBLE);
                        imageView.requestLayout();
                    });
            }
        } catch (Exception e) {
            e.printStackTrace();

            if (getContext() != null) {
                Toast.makeText(getContext(), "Error displaying chart: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }


    private void cancelOngoingCalls() { //cancels Retrofit calls that are still in progress
        if (barChartCall != null && !barChartCall.isCanceled()) {
            barChartCall.cancel();
        }
        if (lineChartCall != null && !lineChartCall.isCanceled()) {
            lineChartCall.cancel();
        }
    }
}
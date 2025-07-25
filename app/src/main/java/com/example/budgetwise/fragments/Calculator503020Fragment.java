package com.example.budgetwise.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.budgetwise.R;
import com.example.budgetwise.api.ApiClient;
import com.example.budgetwise.api.CurrentMonthCalculatorResponse;
import com.example.budgetwise.api.ReportApi;
import com.example.budgetwise.classes.Calculator503020;
import com.example.budgetwise.viewmodel.Calculator503020ViewModel;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Calculator503020Fragment extends Fragment {

    private TextInputEditText monthlyIncome;
    private Button calculatedBtn;
    private TextView needsTv;
    private TextView whishesTv;
    private TextView savingsTv;
    private CheckBox checkboxCurrentMonth;
    private ImageView graphCalculator;
    private Calculator503020ViewModel calculatorViewModel;
    private Call<CurrentMonthCalculatorResponse> barChartCall;

    public Calculator503020Fragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_calculator503020, container, false);
        monthlyIncome=view.findViewById(R.id.inputIncome);
        calculatedBtn=view.findViewById(R.id.btnCalculate);
        needsTv=view.findViewById(R.id.resultNeeds);
        whishesTv=view.findViewById(R.id.resultWants);
        savingsTv=view.findViewById(R.id.resultSavings);
        checkboxCurrentMonth=view.findViewById(R.id.checkboxCurrentMonth);
        graphCalculator=view.findViewById(R.id.graphCalculator);
        calculatorViewModel = new ViewModelProvider(this).get(Calculator503020ViewModel.class);
        calculatorViewModel.getCalculator();//you call the method that requests the calculator data from Firebase. This causes the data to be set in LiveData<Calculator503020> in the ViewModel
        calculatorViewModel.getCalculatorData().observe(getViewLifecycleOwner(),calculator503020 -> { //Every time calculatorData changes (including the first time after getCalculator()), the UI updates
            if(calculator503020!=null)
            {
                monthlyIncome.setText(String.valueOf(calculator503020.getMonthlyIncome()));
                needsTv.setText(String.format("%.2f RON", calculator503020.getNeeds()));
                whishesTv.setText(String.format("%.2f RON", calculator503020.getWishes()));
                savingsTv.setText(String.format("%.2f RON", calculator503020.getSavings()));
            }
        });

        calculatedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String income=monthlyIncome.getText().toString().trim();
                if(!income.isEmpty())
                {
                    double incomeNew=Double.parseDouble(income);
                    Calculator503020 calculator503020=new Calculator503020(incomeNew);
                    needsTv.setText(String.format("%.2f RON", calculator503020.getNeeds()));
                    whishesTv.setText(String.format("%.2f RON", calculator503020.getWishes()));
                    savingsTv.setText(String.format("%.2f RON", calculator503020.getSavings()));
                    calculatorViewModel.saveCalculator(calculator503020);
                }
            }
        });

        checkboxCurrentMonth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    String needStr = needsTv.getText().toString().replace("RON", "").trim();
                    String wantStr = whishesTv.getText().toString().replace("RON", "").trim();
                    String savingStr = savingsTv.getText().toString().replace("RON", "").trim();

                    try {
                        double need = Double.parseDouble(needStr);
                        double want = Double.parseDouble(wantStr);
                        double saving = Double.parseDouble(savingStr);

                        Retrofit retrofit = ApiClient.getRetrofit(true); //responsible for HTTP requests
                        ReportApi api = retrofit.create(ReportApi.class); //get endpoints

                        barChartCall = api.getCurrentMonthChartCalculator(need,want,saving); //request to the barChart endpoint
                        barChartCall.enqueue(new Callback<CurrentMonthCalculatorResponse>() {
                            @Override
                            public void onResponse(Call<CurrentMonthCalculatorResponse> call, Response<CurrentMonthCalculatorResponse> response) {
                                if(response.isSuccessful() && response.body()!=null)
                                {
                                    displayBase64Image(response.body().currentMonthChartCalculator, graphCalculator);
                                }else{
                                    graphCalculator.setImageDrawable(null);
                                }
                            }

                            @Override
                            public void onFailure(Call<CurrentMonthCalculatorResponse> call, Throwable t) {
                                if (!call.isCanceled()) {
                                    Toast.makeText(getContext(), "Current month chart error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    catch (NumberFormatException e)
                    {
                        e.printStackTrace();
                    }

                }
            }
        });
        return view;
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
}
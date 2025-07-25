package com.example.budgetwise.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ReportApi {
    @GET("report")
    Call<ReportResponse> getBarChart(); //we wait for the server to send us back a response containing a ReportResponse

    @GET("report/{category}") //report/Food%2FDrinks
    Call<LineChartResponse> getLineChart
            (@Path(value = "category", encoded = true) String category);

    @GET("report/503020") //GET /report/503020?need=1500&want=900&saving=600
    Call<CurrentMonthCalculatorResponse> getCurrentMonthChartCalculator
            (@Query("need") double need,
             @Query("want") double want,
             @Query("saving") double saving);
}


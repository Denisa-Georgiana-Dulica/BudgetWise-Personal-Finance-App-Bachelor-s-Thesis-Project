package com.example.budgetwise.api;

import com.google.gson.annotations.SerializedName;

public class LineChartResponse {
    @SerializedName("line_chart") //the "line_chart" key in JSON must be mapped to the lineChart field in Java
    public String lineChart; //base64 encoded image content received from the server
}

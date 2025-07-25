package com.example.budgetwise.api;

import com.google.gson.annotations.SerializedName;

public class ReportResponse {
    @SerializedName("bar_chart")
    public String barChart; //Base64 encoded string from JSON, converted to java object via GSON library
}

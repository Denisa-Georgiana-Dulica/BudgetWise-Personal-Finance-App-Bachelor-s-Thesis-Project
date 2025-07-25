package com.example.budgetwise.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static final String EMULATOR_URL = "http://10.0.2.2:5000/";
    //connecting with a web server (API) Retrofit library in Android to send HTTP requests (GET /predict) to a server (like Flask)
    private static Retrofit emulatorRetrofit;
    public static Retrofit getRetrofit(boolean useEmulator) {
        if (emulatorRetrofit == null) {
            emulatorRetrofit = new Retrofit.Builder()
                    .baseUrl(EMULATOR_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return emulatorRetrofit;
    }
}

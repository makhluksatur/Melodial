package com.example;

import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://itunes.apple.com/";
    private static ITunesApiService instance;

    public static synchronized ITunesApiService getInstance() {
        if (instance == null) {
            Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build();
            instance = retrofit.create(ITunesApiService.class);
        }
        return instance;
    }
}

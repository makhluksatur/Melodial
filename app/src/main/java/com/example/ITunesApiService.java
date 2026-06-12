package com.example;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ITunesApiService {
    @GET("search?media=music")
    Call<ITunesResponse> searchSongs(@Query("term") String term);
}

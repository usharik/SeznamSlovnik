package com.usharik.seznamslovnik.service;

import com.usharik.seznamslovnik.model.LanguageServiceResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LanguageServiceInterface {
    @GET("service.py")
    Call<LanguageServiceResponse> doRequest(@Query("call") String call,
                                            @Query("lang") String lang,
                                            @Query("output") String output,
                                            @Query("text") String text,
                                            @Query("c1") String c1,
                                            @Query("c2") String c2,
                                            @Query("n") String n);
}

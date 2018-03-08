package com.usharik.seznamslovnik.service;

import com.usharik.seznamslovnik.model.Answer;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface APIInterface {

    @GET("/suggest/{from}_{to}?")
    Call<Answer> doGetSuggestions(@Path("from") String from,
                                  @Path("to") String to,
                                  @Query("phrase") String phrase,
                                  @Query("result") String result,
                                  @Query("highlight") Integer highlight,
                                  @Query("count") Integer count);

    @GET("/{from}-{to}?")
    Call<ResponseBody> doTranslate(@Path("from") String from,
                                   @Path("to") String to,
                                   @Query("q") String question);
}

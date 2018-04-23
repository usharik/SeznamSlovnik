package com.usharik.seznamslovnik.service;

import com.usharik.seznamslovnik.model.xml.Result;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SeznamSuggestionInterface {

    @GET("/slovnik/{from}_{to}?")
    Observable<Result> doGetSuggestions(@Path("from") String from,
                                        @Path("to") String to,
                                        @Query("phrase") String phrase,
                                        @Query("result") String result,
                                        @Query("highlight") Integer highlight,
                                        @Query("count") Integer count);
}

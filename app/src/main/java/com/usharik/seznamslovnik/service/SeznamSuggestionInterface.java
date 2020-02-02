package com.usharik.seznamslovnik.service;

import com.usharik.seznamslovnik.model.json.suggest.Suggest;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SeznamSuggestionInterface {

    @GET("/slovnik/mix_{from}_{to}")
    Observable<Suggest> doGetSuggestions(@Path("from") String from,
                                         @Path("to") String to,
                                         @Query("phrase") String phrase,
                                         @Query("format") String format,
                                         @Query("highlight") Integer highlight,
                                         @Query("count") Integer count);
}

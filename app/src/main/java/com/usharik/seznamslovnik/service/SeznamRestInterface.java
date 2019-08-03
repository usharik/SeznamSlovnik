package com.usharik.seznamslovnik.service;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface SeznamRestInterface {

    @GET("/api/slovnik")
    Observable<ResponseBody> doTranslate(@Query("dictionary") String dictionary,
                                         @Query("query") String query);
}

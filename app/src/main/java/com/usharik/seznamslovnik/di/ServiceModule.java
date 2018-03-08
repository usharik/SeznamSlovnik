package com.usharik.seznamslovnik.di;

import com.usharik.seznamslovnik.AppState;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by macbook on 09.02.18.
 */

@Module(includes = {AppModule.class})
class ServiceModule {

    @Provides
    @Singleton
    AppState provideAppState() {
        return new AppState();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl("https://slovnik.seznam.cz/suggest/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}

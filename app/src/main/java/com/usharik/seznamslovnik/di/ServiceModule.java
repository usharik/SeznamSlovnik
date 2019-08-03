package com.usharik.seznamslovnik.di;

import android.app.Application;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Vibrator;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.usharik.seznamslovnik.AppState;
import com.usharik.seznamslovnik.UrlRepository;
import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.dao.DatabaseManager;
import com.usharik.seznamslovnik.service.RemoteConfigService;
import com.usharik.seznamslovnik.service.TranslationService;
import com.usharik.seznamslovnik.service.NetworkService;
import com.usharik.seznamslovnik.service.WordInfoService;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.subjects.PublishSubject;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

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
    @Named("seznamRetrofit")
    Retrofit provideSeznamRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(UrlRepository.SEZNAM_TRANSLATE)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    @Named("seznamSuggestRetrofit")
    Retrofit provideSeznamSuggestRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(UrlRepository.SEZNAM_SUGGEST)
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    @Named("languageServiceRetrofit")
    Retrofit provideLanguageServiceRetrofit() {
        return new Retrofit.Builder()
                .baseUrl((UrlRepository.NLP_LANGUAGE_SERVICES))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    DatabaseManager provideDatabaseManager(Application application) {
        return new DatabaseManager(application);
    }

    @Provides
    @Singleton
    TranslationService provideTranslationService(DatabaseManager databaseManager,
                                                 AppState appState,
                                                 @Named("seznamRetrofit") Retrofit seznamRetrofit,
                                                 @Named("seznamSuggestRetrofit") Retrofit seznamSuggestionRetrofit,
                                                 NetworkService networkService,
                                                 PublishSubject<Action> executeActionSubject) {
        return new TranslationService(databaseManager, appState, seznamRetrofit, seznamSuggestionRetrofit, networkService, executeActionSubject);
    }

    @Provides
    @Singleton
    WordInfoService provideWordInfoService() {
        return new WordInfoService();
    }

    @Provides
    @Singleton
    NetworkService provideNetworkService(Application application) {
        return new NetworkService(application);
    }

    @Provides
    @Singleton
    PublishSubject<Action> provideExecuteActionSubject() {
        return PublishSubject.create();
    }

    @Provides
    @Singleton
    ClipboardManager provideClipboardManager(Application application) {
        return (ClipboardManager) application.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Provides
    @Singleton
    Vibrator provideVibrator(Application application) {
        return (Vibrator) application.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Provides
    @Singleton
    Resources provideResources(Application application) {
        return application.getResources();
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Application application) {
        return application.getSharedPreferences("seznam_slovnik.prefences", Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    FirebaseRemoteConfig firebaseRemoteConfig() {
        return FirebaseRemoteConfig.getInstance();
    }

    @Provides
    @Singleton
    RemoteConfigService remoteConfigService(FirebaseRemoteConfig firebaseRemoteConfig) {
        return new RemoteConfigService(firebaseRemoteConfig);
    }
}

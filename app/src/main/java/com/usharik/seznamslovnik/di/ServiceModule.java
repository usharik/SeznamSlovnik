package com.usharik.seznamslovnik.di;

import android.app.Application;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Vibrator;

import com.usharik.seznamslovnik.AppState;
import com.usharik.seznamslovnik.dao.AppDatabase;
import com.usharik.seznamslovnik.service.TranslationService;
import com.usharik.seznamslovnik.service.NetworkService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.subjects.PublishSubject;
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
                .baseUrl("https://slovnik.seznam.cz/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    AppDatabase provideAppDatabase(Application application) {
        return AppDatabase.getAppDatabase(application);
    }

    @Provides
    @Singleton
    TranslationService provideTranslationService(AppDatabase appDatabase,
                                             AppState appState,
                                             Retrofit retrofit,
                                             PublishSubject<String> toastShowSubject) {
        return new TranslationService(appDatabase, appState, retrofit, toastShowSubject);
    }

    @Provides
    @Singleton
    NetworkService provideNetworkService(Application application) {
        return new NetworkService(application);
    }

    @Provides
    @Singleton
    PublishSubject<String> provideToastShowSubject() {
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
}

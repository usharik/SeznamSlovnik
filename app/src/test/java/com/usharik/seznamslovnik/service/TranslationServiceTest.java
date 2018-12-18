package com.usharik.seznamslovnik.service;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.usharik.seznamslovnik.AppState;
import com.usharik.seznamslovnik.UrlRepository;
import com.usharik.seznamslovnik.adapter.TranslationResult;
import com.usharik.seznamslovnik.dao.AppDatabase;
import com.usharik.seznamslovnik.dao.DatabaseManager;
import com.usharik.seznamslovnik.dao.TranslationStorageDao;

import io.reactivex.subjects.PublishSubject;

import org.junit.Before;
import org.junit.Test;
import retrofit2.Retrofit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TranslationServiceTest {

    TranslationService translationService;

    @Before
    public void before() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UrlRepository.SEZNAM_TRANSLATE)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        AppState appState = new AppState();
        appState.isOfflineMode = false;

        TranslationStorageDao translationStorageDao = mock(TranslationStorageDao.class);
        AppDatabase appDatabase = mock(AppDatabase.class);
        when(appDatabase.translationStorageDao()).thenReturn(translationStorageDao);

        DatabaseManager databaseManager = mock(DatabaseManager.class);
        when(databaseManager.getActiveDbInstance()).thenReturn(appDatabase);

        translationService = new TranslationService(databaseManager,
                appState, retrofit, retrofit, mock(NetworkService.class), PublishSubject.create());
    }

    @Test
    public void testOnlineTranslation1() {
        TranslationResult translationResult = translationService.getOnlineTranslation("ahoj", "cz", "ru")
                .blockingGet();
        assertEquals(3, translationResult.getTranslations().size());
        assertEquals("ahoj", translationResult.getWord());
    }

    @Test
    public void testOnlineTranslation2() {
        final String[] A_TRANSLATIONS = new String[]{"и","да", "уж", "плюс", "плюс", "да", "и да́же", "а", "но", "да",
                "a to, a sice а и́менно", "да", "и (поэ́тому)", "и потому́", "а", "и"};

        TranslationResult translationResult = translationService.getOnlineTranslation("a", "cz", "ru")
                .blockingGet();
        assertEquals(A_TRANSLATIONS.length, translationResult.getTranslations().size());
        assertEquals("a", translationResult.getWord());
        for (int i=0; i<translationResult.getTranslations().size(); i++) {
            assertEquals(A_TRANSLATIONS[i], translationResult.getTranslations().get(i));
        }
    }
}

package com.usharik.seznamslovnik.service;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.usharik.seznamslovnik.AppState;
import com.usharik.seznamslovnik.UrlRepository;
import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.adapter.TranslationResult;
import com.usharik.seznamslovnik.dao.AppDatabase;
import com.usharik.seznamslovnik.dao.DatabaseManager;
import com.usharik.seznamslovnik.dao.TranslationStorageDao;
import com.usharik.seznamslovnik.dao.entity.Word;
import com.usharik.seznamslovnik.service.NetworkService;
import com.usharik.seznamslovnik.service.TranslationService;
import io.reactivex.subjects.PublishSubject;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TranslationServiceAndroidTest {

    Context appContext;
    DatabaseManager databaseManager;
    TranslationService translationService;
    NetworkService networkService;
    PublishSubject<Action> executeActionSubject;
    AppState appState;
    TranslationStorageDao translationStorageDao;

    @Before
    public void setUp() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        databaseManager = new DatabaseManager(appContext);
        networkService = mock(NetworkService.class);
        appState =new AppState();
        executeActionSubject = PublishSubject.create();
        translationService = new TranslationService(databaseManager, appState, provideSeznamRetrofit(),
                provideSeznamSuggestRetrofit(), networkService, executeActionSubject);

        appContext.deleteDatabase(AppDatabase.DB_NAME);
        translationStorageDao = databaseManager.getActiveDbInstance().translationStorageDao();
    }

    @Test
    public void useAppContext() {
        assertEquals("com.usharik.seznamslovnik", appContext.getPackageName());
    }

    @Test
    public void testTranslationAle() {
        String word = "ale";
        appState.isOfflineMode = false;
        TranslationResult translationResult = translationService.translate(word, "cz", "ru").blockingGet();

        Word wordObj = translationStorageDao.getWord(word, "cz").blockingGet();

        assertNotNull(translationResult);
        assertEquals(word, translationResult.getWord());
        assertEquals(word, wordObj.getWord());
        assertNotNull(wordObj.getJson());
        assertTrue(wordObj.getJson().length() > 0);

        appState.isOfflineMode = true;
        translationResult = translationService.translate(word, "cz", "ru").blockingGet();

        assertNotNull(translationResult);
    }

    @Test
    public void testTranslationPas() {
        String word = "pas";
        TranslationResult translationResult = translationService.translate(word, "cz", "ru").blockingGet();

        Word wordObj = translationStorageDao.getWord(word, "cz").blockingGet();

        assertNotNull(translationResult);
        assertEquals(word, translationResult.getWord());
        assertEquals(word, wordObj.getWord());
        assertNotNull(wordObj.getJson());
        assertTrue(wordObj.getJson().length() > 0);
    }

    @Test
    public void testTranslationDum() {
        String word = "dum";
        TranslationResult translationResult = translationService.translate(word, "cz", "ru").blockingGet();

        Word wordObj = translationStorageDao.getWord(word, "cz").blockingGet();

        assertNotNull(translationResult);
        assertEquals(word, translationResult.getWord());
        assertEquals(word, wordObj.getWord());
        assertNotNull(wordObj.getJson());
        assertTrue(wordObj.getJson().length() > 0);
    }

    @Test
    public void testTranslationDelat() {
        String word = "delat";
        TranslationResult translationResult = translationService.translate(word, "cz", "ru").blockingGet();

        Word wordObj = translationStorageDao.getWord(word, "cz").blockingGet();

        assertNotNull(translationResult);
        assertEquals(word, StringUtils.stripAccents(translationResult.getWord()));
        assertEquals(word, wordObj.getWord());
        assertNotNull(wordObj.getJson());
        assertTrue(wordObj.getJson().length() > 0);
    }

    @Test
    public void testSuggestions() {
        List<String> strings = translationService.getOnlineSuggestions("a", "cz", "ru", 50).blockingGet();

        assertEquals(51, strings.size());
    }

    private Retrofit provideSeznamRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(UrlRepository.SEZNAM_TRANSLATE)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    private Retrofit provideSeznamSuggestRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(UrlRepository.SEZNAM_SUGGEST)
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }
}

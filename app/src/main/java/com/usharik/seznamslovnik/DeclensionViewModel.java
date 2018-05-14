package com.usharik.seznamslovnik;

import android.databinding.Bindable;
import android.util.Log;

import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.action.OpenUrlInBrowserAction;
import com.usharik.seznamslovnik.adapter.DeclensionAdapter;
import com.usharik.seznamslovnik.dao.DatabaseManager;
import com.usharik.seznamslovnik.dao.TranslationStorageDao;
import com.usharik.seznamslovnik.dao.entity.CasesOfNoun;
import com.usharik.seznamslovnik.dao.entity.WordInfo;
import com.usharik.seznamslovnik.framework.ViewModelObservable;
import com.usharik.seznamslovnik.service.WordInfoService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class DeclensionViewModel extends ViewModelObservable {

    private final AppState appState;
    private final DatabaseManager databaseManager;
    private final PublishSubject<Action> executeActionSubject;
    private final WordInfoService wordInfoService;
    private final List<String> wordInfo;

    @Inject
    public DeclensionViewModel(final AppState appState,
                               final DatabaseManager databaseManager,
                               final PublishSubject<Action> executeActionSubject,
                               final WordInfoService wordInfoService) {
        this.appState = appState;
        this.databaseManager = databaseManager;
        this.executeActionSubject = executeActionSubject;
        this.wordInfoService = wordInfoService;
        this.wordInfo = new ArrayList<>();
    }

    public Observable<DeclensionAdapter> getAdapter() {
        return Observable.fromCallable(() -> prepareNounAdapter(appState.word));
    }

    private DeclensionAdapter prepareNounAdapter(String word) {
        TranslationStorageDao dao = databaseManager.getActiveDbInstance().translationStorageDao();
        HashMap<Integer, String> singularDeclensions = new HashMap<>();
        HashMap<Integer, String> pluralDeclensions = new HashMap<>();
        try {
            List<CasesOfNoun> single = dao.getCasesOfNoun(word, "nS");

            for (CasesOfNoun con : single) {
                singularDeclensions.put(con.getCaseNum(), con.getWord());
            }
            List<CasesOfNoun> plural = dao.getCasesOfNoun(word, "nP");
            for (CasesOfNoun con : plural) {
                pluralDeclensions.put(con.getCaseNum(), con.getWord());
            }
            this.wordInfo.clear();
            this.wordInfo.addAll(dao.getWordInfo(word));

            if ((single.size() == 7 && plural.size() == 7 && wordInfo.size() > 0) || appState.isOfflineMode) {
                notifyPropertyChanged(BR.wordInfo);
                return new DeclensionAdapter(singularDeclensions, pluralDeclensions);
            }
            WordInfoService.ParsedWordInfo wordInfo = wordInfoService.getWordInfoFromPriruckaUjcCas(word);
            if (wordInfoService.isNounWordInfo(wordInfo)) {
                Long wordId = dao.getWordId(word, "cz");
                List<CasesOfNoun> casesOfNoun = wordInfoService.parseCasesOfNoun(wordId, wordInfo);
                dao.insertCasesOfNoun(casesOfNoun.toArray(new CasesOfNoun[casesOfNoun.size()]));
                for (CasesOfNoun con : casesOfNoun) {
                    if (con.getNumber().equals("nS")) {
                        singularDeclensions.put(con.getCaseNum(), con.getWord());
                    } else {
                        pluralDeclensions.put(con.getCaseNum(), con.getWord());
                    }
                }
                List<WordInfo> wordInfos = wordInfoService.parsePolozky(wordId, wordInfo);
                dao.insertWordInfos(wordInfos.toArray(new WordInfo[wordInfos.size()]));
                this.wordInfo.clear();
                for (WordInfo wi : wordInfos) {
                    this.wordInfo.add(wi.getInfo());
                }
            }
        } catch (Exception ex) {
            Log.e(getClass().getName(), "!!!!!", ex);
        }
        notifyPropertyChanged(BR.wordInfo);
        return new DeclensionAdapter(singularDeclensions, pluralDeclensions);
    }

    @Bindable
    public String getLink() {
        return String.format("%s?slovo=%s", UrlRepository.PRIRUCKA_UJC_CAS, appState.word);
    }

    @Bindable
    public String getWordInfo() {
        StringBuilder sb = new StringBuilder();
        for (String str : this.wordInfo) {
            sb.append(str).append("\n");
        }
        return sb.toString();
    }

    public void onClick() {
        executeActionSubject.onNext(new OpenUrlInBrowserAction(getLink()));
    }
}

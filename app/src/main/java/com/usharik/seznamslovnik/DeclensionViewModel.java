package com.usharik.seznamslovnik;

import android.databinding.Bindable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.action.OpenUrlInBrowserAction;
import com.usharik.seznamslovnik.action.ShowToastAction;
import com.usharik.seznamslovnik.adapter.DeclensionAdapter;
import com.usharik.seznamslovnik.adapter.FormsOfVerbAdapter;
import com.usharik.seznamslovnik.dao.DatabaseManager;
import com.usharik.seznamslovnik.dao.TranslationStorageDao;
import com.usharik.seznamslovnik.dao.entity.CasesOfNoun;
import com.usharik.seznamslovnik.dao.entity.FormsOfVerb;
import com.usharik.seznamslovnik.dao.entity.WordInfo;
import com.usharik.seznamslovnik.framework.ViewModelObservable;
import com.usharik.seznamslovnik.service.WordInfoService;

import java.util.ArrayList;
import java.util.Collections;
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

    public Observable< RecyclerView.Adapter> getAdapter() {
        return Observable.fromCallable(() -> prepareWordFormsAdapter(appState.wordForDeclension));
    }

    private RecyclerView.Adapter prepareWordFormsAdapter(String word) {
        TranslationStorageDao dao = databaseManager.getActiveDbInstance().translationStorageDao();
        HashMap<Integer, String> singularForms = new HashMap<>();
        HashMap<Integer, String> pluralForms = new HashMap<>();
        RecyclerView.Adapter adapter = new DeclensionAdapter(Collections.emptyMap(), Collections.emptyMap());
        try {
            this.wordInfo.clear();
            this.wordInfo.addAll(dao.getWordInfo(word));

            List<CasesOfNoun> singleNoun = dao.getCasesOfNoun(word, "nS");
            for (CasesOfNoun con : singleNoun) {
                singularForms.put(con.getCaseNum(), con.getWord());
            }

            List<CasesOfNoun> pluralNoun = dao.getCasesOfNoun(word, "nP");
            for (CasesOfNoun con : pluralNoun) {
                pluralForms.put(con.getCaseNum(), con.getWord());
            }

            if (singleNoun.size() == 7 || pluralNoun.size() == 7) {
                return new DeclensionAdapter(singularForms, pluralForms);
            }

            singularForms.clear();
            pluralForms.clear();

            List<FormsOfVerb> singleVerb = dao.getFormsOfVerb(word, "nS");
            for (FormsOfVerb con : singleVerb) {
                singularForms.put(con.getFormNum(), con.getWord());
            }

            List<FormsOfVerb> pluralVerb = dao.getFormsOfVerb(word, "nP");
            for (FormsOfVerb con : pluralVerb) {
                pluralForms.put(con.getFormNum(), con.getWord());
            }

            if (singleVerb.size() == pluralVerb.size() && singleVerb.size() >= 9) {
                return new FormsOfVerbAdapter(singularForms, pluralForms);
            }

            if (appState.isOfflineMode) {
                return new DeclensionAdapter(Collections.emptyMap(), Collections.emptyMap());
            }

            WordInfoService.ParsedWordInfo wordInfo = wordInfoService.getWordInfoFromPriruckaUjcCas(word, appState.proxyInfo.getProxy());

            Long wordId = dao.getWordId(word, "cz");
            if (this.wordInfo.size() == 0) {
                List<WordInfo> wordInfos = wordInfoService.parsePolozky(wordId, wordInfo);
                dao.insertWordInfos(wordInfos.toArray(new WordInfo[wordInfos.size()]));
                this.wordInfo.clear();
                for (WordInfo wi : wordInfos) {
                    this.wordInfo.add(wi.getInfo());
                }
            }

            if (wordInfoService.isNounWordInfo(wordInfo)) {
                List<CasesOfNoun> casesOfNoun = wordInfoService.parseCasesOfNoun(wordId, wordInfo);
                dao.insertCasesOfNoun(casesOfNoun.toArray(new CasesOfNoun[casesOfNoun.size()]));
                for (CasesOfNoun con : casesOfNoun) {
                    if (con.getNumber().equals("nS")) {
                        singularForms.put(con.getCaseNum(), con.getWord());
                    } else {
                        pluralForms.put(con.getCaseNum(), con.getWord());
                    }
                }
                adapter = new DeclensionAdapter(singularForms, pluralForms);
            } else if (wordInfoService.isVerbWordInfo(wordInfo)) {
                List<FormsOfVerb> formsOfVerbs = wordInfoService.parseFormsOfVerb(wordId, wordInfo);
                dao.insertFormsOfVerb(formsOfVerbs.toArray(new FormsOfVerb[formsOfVerbs.size()]));
                for (FormsOfVerb fov : formsOfVerbs) {
                    if (fov.getNumber().equals("nS")) {
                        singularForms.put(fov.getFormNum(), fov.getWord());
                    } else {
                        pluralForms.put(fov.getFormNum(), fov.getWord());
                    }
                }
                adapter = new FormsOfVerbAdapter(singularForms, pluralForms);
            } else if (this.wordInfo.size() == 0) {
                adapter = new DeclensionAdapter(Collections.emptyMap(), Collections.emptyMap());
            }
        } catch (Exception ex) {
            Log.e(getClass().getName(), ex.getLocalizedMessage(), ex);
            executeActionSubject.onNext(new ShowToastAction(ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : ex.getClass().getName()));
        } finally {
            notifyPropertyChanged(BR.wordInfo);
        }
        return adapter;
    }

    @Bindable
    public String getLink() {
        return String.format("%s?slovo=%s", UrlRepository.PRIRUCKA_UJC_CAS, appState.getWord());
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

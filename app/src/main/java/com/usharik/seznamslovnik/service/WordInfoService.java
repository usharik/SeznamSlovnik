package com.usharik.seznamslovnik.service;

import com.usharik.seznamslovnik.UrlRepository;
import com.usharik.seznamslovnik.dao.entity.CasesOfNoun;
import com.usharik.seznamslovnik.dao.entity.FormsOfVerb;
import com.usharik.seznamslovnik.dao.entity.WordInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WordInfoService {

    private static final Map<String, Integer> verbForms = buildVerbFormsMap();
    public static final Map<Integer, String> verbFormsByNum = buildVerbFormsByNumMap();

    private static Map<String, Integer> buildVerbFormsMap() {
        Map<String, Integer> res = new HashMap<>();
        res.put("1. osoba", 1);
        res.put("2. osoba", 2);
        res.put("3. osoba", 3);
        res.put("rozkazovací způsob", 4);
        res.put("příčestí činné", 5);
        res.put("příčestí trpné", 6);
        res.put("přechodník přítomný, m.", 7);
        res.put("přechodník přítomný, ž. + s.", 8);
        res.put("přechodník minulý, m.", 9);
        res.put("přechodník minulý, ž. + s.", 10);
        res.put("verbální substantivum", 11);
        return Collections.unmodifiableMap(res);
    }

    private static Map<Integer,String> buildVerbFormsByNumMap() {
        Map<Integer,String> res = new HashMap<>();
        for (Map.Entry<String, Integer> entry : verbForms.entrySet()) {
            res.put(entry.getValue(), entry.getKey());
        }
        return Collections.unmodifiableMap(res);
    }

    public static class ParsedWordInfo {
        List<Element> polozky;
        List<Element> tables;

        ParsedWordInfo() {
            polozky = new ArrayList<>();
            tables = new ArrayList<>();
        }
    }

    public ParsedWordInfo getWordInfoFromPriruckaUjcCas(String word, Proxy proxy) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().proxy(proxy).build();
        Request req = new Request.Builder()
                .url(String.format("%s?slovo=%s", UrlRepository.PRIRUCKA_UJC_CAS, word))
                .build();
        Response response = client.newCall(req).execute();
        if (response.code() != 200) {
            throw new RuntimeException("Http error code " + response.code());
        }
        Document html = Jsoup.parse(response.body().string());
        ParsedWordInfo wordInfo = new ParsedWordInfo();
        wordInfo.polozky.addAll(html.select("p.polozky"));
        wordInfo.tables.addAll(html.select("table"));
        return wordInfo;
    }

    public boolean isNounWordInfo(ParsedWordInfo wordInfo) {
        if (wordInfo.polozky.size() < 2) {
            return false;
        }
        boolean isNoun = false;
        for (Element el : wordInfo.polozky) {
            isNoun |= el.text().contains("rod: ");
        }
        if (isNoun &&
                wordInfo.tables.size() >= 1 &&
                wordInfo.tables.get(0).select("tr").size() == 8) {
            return true;
        }
        return false;
    }

    public List<CasesOfNoun> parseCasesOfNoun(long wordId, ParsedWordInfo wordInfo) {
        List<CasesOfNoun> res = new ArrayList<>();
        Element table = wordInfo.tables.get(0);
        Elements rows = table.select("tr");
        for (int i=1; i<8; i++) {
            Elements columns = rows.get(i).select("td");
            if (columns.size() != 3) {
                throw new RuntimeException("Pars error");
            }
            for (int j=1; j<3; j++) {
                res.add(new CasesOfNoun(wordId, i, j==1 ? "nS" : "nP", columns.get(j).text()));
            }
        }
        return res;
    }

    public boolean isVerbWordInfo(ParsedWordInfo wordInfo) {
        if (wordInfo.tables.size() >= 1 &&
                wordInfo.tables.get(0).select("tr").size() >= 10) {
            return true;
        }
        return false;
    }

    public List<FormsOfVerb> parseFormsOfVerb(long wordId, ParsedWordInfo wordInfo) {
        List<FormsOfVerb> res = new ArrayList<>();
        Element table = wordInfo.tables.get(0);
        Elements rows = table.select("tr");
        String prevForm = null;
        for (int i=1; i<rows.size(); i++) {
            Elements columns = rows.get(i).select("td");
            Integer num = verbForms.get(columns.get(0).text());
            if (num == null) {
                continue;
            }
            if (columns.size() == 1) {
                res.add(new FormsOfVerb(wordId, i,"nS", ""));
                res.add(new FormsOfVerb(wordId, i,"nP", ""));
                prevForm = null;
            } else if (columns.size() == 2) {
                String form = columns.get(1).text();
                res.add(new FormsOfVerb(wordId, i,"nS", form));
                res.add(new FormsOfVerb(wordId, i,"nP", prevForm == null ? form : prevForm));
                prevForm = null;
            } else if (columns.size() == 3) {
                res.add(new FormsOfVerb(wordId, i,"nS", columns.get(1).text()));
                res.add(new FormsOfVerb(wordId, i,"nP", columns.get(2).text()));
                prevForm = columns.get(2).hasAttr("rowspan") ? columns.get(2).text() : null;
            } else {
                throw new RuntimeException("Pars error");
            }
        }
        return res;
    }

    public List<WordInfo> parsePolozky(long wordId, ParsedWordInfo wordInfo) {
        List<WordInfo> res = new ArrayList<>();
        for (Element el : wordInfo.polozky) {
            res.add(new WordInfo(wordId, el.text()));
        }
        return res;
    }
}


package com.usharik.seznamslovnik.service;

import com.usharik.seznamslovnik.UrlRepository;
import com.usharik.seznamslovnik.dao.entity.CasesOfNoun;
import com.usharik.seznamslovnik.dao.entity.WordInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WordInfoService {

    public static class ParsedWordInfo {
        List<Element> polozky;
        List<Element> tables;

        ParsedWordInfo() {
            polozky = new ArrayList<>();
            tables = new ArrayList<>();
        }
    }

    public ParsedWordInfo getWordInfoFromPriruckaUjcCas(String word) throws IOException {
        OkHttpClient client = new OkHttpClient();
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

    public List<WordInfo> parsePolozky(long wordId, ParsedWordInfo wordInfo) {
        List<WordInfo> res = new ArrayList<>();
        for (Element el : wordInfo.polozky) {
            res.add(new WordInfo(wordId, el.text()));
        }
        return res;
    }
}

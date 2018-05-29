package com.usharik.seznamslovnik.service;

import com.usharik.seznamslovnik.dao.entity.CasesOfNoun;
import com.usharik.seznamslovnik.dao.entity.FormsOfVerb;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.Proxy;
import java.util.List;

@RunWith(JUnit4.class)
public class WordInfoServiceTest {

    @Test
    public void wordInfoServiceNounTest() throws Exception {
        for (String word : new String[] {"slon", "dům", "kuře", "soudce", "pan", "muz", "stavení"}) {
            Thread.sleep(300);
            WordInfoService wordInfoService = new WordInfoService();
            WordInfoService.ParsedWordInfo wordInfo = wordInfoService.getWordInfoFromPriruckaUjcCas(word, Proxy.NO_PROXY);
            Assert.assertTrue(wordInfoService.isNounWordInfo(wordInfo));
            List<CasesOfNoun> casesOfNouns = wordInfoService.parseCasesOfNoun(11, wordInfo);
            Assert.assertNotNull(casesOfNouns);
            Assert.assertEquals(14, casesOfNouns.size());
        }
    }

    @Test
    public void wordInfoServiceVerbTest() throws Exception {
        for (String word : new String[] {"být", "delat"}) {
            Thread.sleep(300);
            WordInfoService wordInfoService = new WordInfoService();
            WordInfoService.ParsedWordInfo wordInfo = wordInfoService.getWordInfoFromPriruckaUjcCas(word, Proxy.NO_PROXY);
            Assert.assertTrue(wordInfoService.isVerbWordInfo(wordInfo));
            List<FormsOfVerb> formsOfVerbs = wordInfoService.parseFormsOfVerb(11, wordInfo);
            Assert.assertNotNull(formsOfVerbs);
        }
    }

    @Test
    public void wordInfoServiceIncorrectWordTest() throws Exception {
        for (String word : new String[] {"sdfsdf", "sdsdsdsdsd", "sdfsdf", "sdfsdf"}) {
            Thread.sleep(300);
            WordInfoService wordInfoService = new WordInfoService();
            WordInfoService.ParsedWordInfo wordInfo = wordInfoService.getWordInfoFromPriruckaUjcCas(word, Proxy.NO_PROXY);
            Assert.assertFalse(wordInfoService.isNounWordInfo(wordInfo));
        }
    }
}

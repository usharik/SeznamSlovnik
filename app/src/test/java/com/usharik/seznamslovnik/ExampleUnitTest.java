package com.usharik.seznamslovnik;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {
        System.out.println(StringUtils.stripAccents("čekát"));
        System.out.println(StringUtils.stripAccents("aa проду́кт"));
        System.out.println(StringUtils.stripAccents("aa привет"));

        assertEquals(4, 2 + 2);
    }
}
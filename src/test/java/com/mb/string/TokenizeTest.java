package com.mb.string;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

@Slf4j
public class TokenizeTest {

    @Test
    public void split_test() {
        String test = "DT@6020@00700@SO@00855815@20240822006    @     @        @  @        @KRW@000000010000000@60012139@ @주식회사 푸드올마켓 (FOOD ALL MARKET)         @                    @김종권                 @                                        @                                        @경기도 남양주시 화도읍 차산리 산 43-3   @                                                  @                                                  @FOOD ALL MARKET                                   @1588-2216           @031-573-5214        @00001000@                    @주식회사 푸드올마켓 차산리                  @경기도 남양주시 화도읍 차산리 산 43-3   @                                                  @                                                  @                                                  @010-5416-5195       @                    @차산리                 @                                                  @               @        @";

        String[] strings = test.split("@");
        int len = strings.length;
        for (int i = 0; i < len; i++) {
            log.info("[{}]{}[len:{}]", i+1, strings[i], strings[i].length());
        }

    }

    @Test
    public void token_test() {
        String test = "aaa@@asdasdasd@ @ sdfsd #@";
        StringTokenizer st = new StringTokenizer(test, "@" );

        while (st.hasMoreTokens()) {
            System.out.println("!" + st.nextToken() + "!");
        }

    }
}

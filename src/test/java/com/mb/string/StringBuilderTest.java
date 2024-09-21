package com.mb.string;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.*;

@Slf4j
public class StringBuilderTest {

    @Test
    public void charSequence_test() {
        StringBuilder bd = new StringBuilder("ABCDEFGHIJKLMNOP\n가나다라마바사");
        int idx = bd.indexOf("\n");
        CharSequence sequence = bd.subSequence(0, idx);
        //log.info("{}/{}", sequence.toString(), sequence.toString().contains("\n"));
        int len = sequence.length();
        String empty = "|";
        System.out.println("#" +empty.charAt(0));
        char c = '|';
        System.out.println(c);
    }

    @Test
    public void string_test2() {
        String header = "sigungu_code|esb_link_id_ap2|nursing_agency_name|phone_number|zip_code|total_doctor|sigungu_code_name|encryption_symbol|esb_link_stcd_mid|homepage_url|esb_link_stcd_ap2|sido_code_name|esb_link_init_time_ap2|eupmyeondong_name|address|sido_code|longtitude|esb_link_end_time_ap2|latitude|esb_link_seq|opening_date|category_code|category_code_name";
        String str = "230002|IFD2D_AP1_AP2_001240722081720001106|엔와이(NY)라임치과의원|053-962-2279|41072|2|대구동구|JDQ4MTYyMiM4MSMkMSMkMCMkNzIkMzgxMTkxIzIxIyQxIyQ5IyQ5MiQyNjEwMDIjNTEjJDEjJDIjJDgz|N||S|대구|2024-07-22|대림동|대구광역시 동구 메디밸리로 19, 4층 402호 (대림동)|230000|35.8742316|2024-07-22|128.7492991|1016501|2017-03-15|51|치과의원";
        String str2 = "\"230002\"|   \"IFD2D_AP1_AP2_001240722081720001106\"      |\"엔와이(NY)라임치과의원\"|\"053-962-2279\"|\"41072\"|\"2\"|\"대구동구\"|\"JDQ4MTYyMiM4MSMkMSMkMCMkNzIkMzgxMTkxIzIxIyQxIyQ5IyQ5MiQyNjEwMDIjNTEjJDEjJDIjJDgz\"|\"N\"|\"\"|\"S\"|\"대구\"|\"2024-07-22\"|\"대림동\"|\"대구광역시 동구 메디밸리로 19, 4층 402호 (대림동)\"|\"230000\"|\"35.8742316\"|\"2024-07-22\"|\"128.7492991\"|\"1016501\"|    \"2017-03-15\"|\"51\"   |    \"치과의원\"    &crlf;";

        //String lineSeparator = "\n";
        String lineSeparator = "&crlf;";
        String delimiter = "|";
        String qualifier = "\"";

        int len = header.length();

        List<String> columns = new ArrayList<>();

        boolean open = false;
        boolean close = false;
        /*for (char c : header.toCharArray()) {
            if (buf.indexOf(qualifier) != -1) {
                System.out.println("ss");
            } else if (buf.indexOf(delimiter) != -1) {
                System.out.println(buf);
                columns.add(buf.toString());
                buf = new StringBuilder();

            } else if (buf.indexOf(lineSeparator) != -1) {
                buf = null;
                break;
            } else {
                buf.append(c);
            }
        }*/
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char c = header.charAt(i);

            if (buf.indexOf(qualifier) != -1) {

            }
            buf.append(c);
            if (buf.indexOf(delimiter) != -1) {
                buf.setLength(buf.length() - delimiter.length());
                columns.add(buf.toString());
                buf = new StringBuilder();

            } else if (buf.indexOf(lineSeparator) != -1) {
                buf = null;
                break;
            }
        }

        StringBuilder buf2 = new StringBuilder();
        int str2Len = str2.length();
        List<String> data = new ArrayList<>();

        boolean open2 = false;
        boolean close2 = false;
        boolean emptyQualifier = false;
        if (qualifier.isEmpty()) {
            emptyQualifier = true;
        }
        int redundantSize = 0;
        int qualifierLen = qualifier.length();

        for (int i = 0; i < str2Len; i++) {
            char c = str2.charAt(i);
            if (emptyQualifier) {

                buf2.append(c);
                if (buf2.indexOf(delimiter) != -1) {
                    buf2.setLength(buf2.length() - delimiter.length());
                    data.add(buf2.toString());
                    buf2 = new StringBuilder();
                }

                if (buf2.indexOf(lineSeparator) != -1) {
                    buf2.setLength(buf2.length() - lineSeparator.length());
                    data.add(buf2.toString());
                    buf2 = null;
                    break;
                }

            } else {

                buf2.append(c);

                //qualifier 가 처음 나왔을 때
                if (!open2 && buf2.indexOf(qualifier) != -1) {
                    open2 = true;
                    buf2.setLength(0);
                    //buffer를 초기상태로 만든다.
                    redundantSize = 0;
                    continue;
                }

                //qualifier 가 한 번 나온적이 있고 다시 나왔을 때
                if (open2 && buf2.indexOf(qualifier) != -1) {
                    buf2.setLength(buf2.length() - qualifierLen);
                    //buffer에서 qualifier를 지운다.
                    open2 = false;
                    redundantSize = 0;
                    continue;
                }

                if (open2)
                    continue;

                ++redundantSize;

                //qualifier 가 모두 닫히고 delimiter 가 나왔을 때
                if (!open2 && buf2.indexOf(delimiter) != -1) {
                    buf2.setLength(buf2.length() - redundantSize);
                    data.add(buf2.toString());
                    buf2.setLength(0);
                    redundantSize = 0;
                    continue;
                }

                if (!open2 && buf2.indexOf(lineSeparator) != -1) {
                    buf2.setLength(buf2.length() - redundantSize);
                    data.add(buf2.toString());
                    buf2.setLength(0);
                    redundantSize = 0;
                    break;
                }

            }


        }
        System.out.println(data);



    }

    @Test
    public void string_test3() {
        StringBuilder bd = new StringBuilder("ABCDEFGHIJKLMNOP가나다라마바사");

        int idx = bd.indexOf("ABC");
        System.out.println(idx);

        int lidx = bd.lastIndexOf("바사");
        System.out.println(lidx);
    }
}

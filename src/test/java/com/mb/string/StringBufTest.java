package com.mb.string;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class StringBufTest {

    @Test
    public void string_buffer_replace() {
        StringBuffer buf = new StringBuffer();
        buf.append("a");
        buf.append("\n");
        buf.append("b");
        buf.append("c");
        buf.append("d");
        buf.append("e");
        buf.append("f");
        buf.append("g");
        buf.append("h");
        buf.append("i");
        buf.append("j");
        buf.append("k");
        buf.append("l");
        buf.append("m");
        buf.append("n");
        buf.append("o");
        buf.append("\r");
        buf.append("p");
        buf.append("q");
        buf.append("r");
        buf.append("s");
        buf.append("t");
        buf.append("u");
        buf.append("v");
        buf.append("w");
        buf.append("x");
        buf.append("y");
        buf.append("z");
        buf.append("z");

        while(true) {
            int crIdx = buf.indexOf("\r");
            int lfIdx = buf.indexOf("\n");
            log.info("@{}@", crIdx);
            log.info("#{}#", lfIdx);

            if (crIdx != -1) {
                //buf.setCharAt(crIdx, '9');
                buf.deleteCharAt(crIdx);
                buf.insert(crIdx, "&cr;");
                continue;
            }
            if (lfIdx != -1) {
                //buf.setCharAt(lfIdx, '7');
                buf.deleteCharAt(lfIdx);
                buf.insert(lfIdx, "&lf;");
                continue;
            }
            if (crIdx == -1 && lfIdx == -1) {
                break;
            }
        }

        System.out.println(buf);
        //System.out.println((buf.toString()).replace("\n", "7").replace("\r", "3"));
    }

    @Test
    public void bytes_test() {
        byte[] bytes = new String("ABCDEFGHIJKLMNOPQRSTUVWXYZ").getBytes();
        System.out.println(bytes);
        StringBuffer buf = new StringBuffer();
        for (byte b : bytes) {
            System.out.println(b);
            buf.append(b).append(" ");
        }
        System.out.println();
        System.out.println(buf);

    }

    @Test
    public void bytes_test2() throws IOException {
        Path path = Paths.get("C:\\Users\\admin\\Pictures\\KakaoTalk_20240731_144727283.jpg");
        byte[] bytes = Files.readAllBytes(path);
        for (byte b : bytes) {
         //   System.out.println(b);
        }
        String i = "61";
        byte bb = 61;
        System.out.println(i.getBytes().length);
    }
}

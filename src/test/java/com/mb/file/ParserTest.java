package com.mb.file;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileParser;
import mb.dnm.access.file.FileParserTemplate;
import mb.dnm.code.DataType;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class ParserTest {

    private String recordSeparator = "\n";
    private String delimiter = "|";
    private String qualifier = "";
    private String replacementOfNullValue = "";
    private String replacementOfEmptyValue = "";
    private String replacementOfLineFeed = "&cr;";
    private String replacementOfCarriageReturn = "&lf;";
    private boolean headerExist = true;
    private boolean handleBinaryData = true;

    public FileParserTemplate getTemplate() {
        return new FileParserTemplate(recordSeparator, delimiter, qualifier,
                replacementOfNullValue, replacementOfEmptyValue, replacementOfLineFeed,
                replacementOfCarriageReturn, headerExist, handleBinaryData);
    }

    @Test
    public void readHeader_test() throws Exception {
        String data = new String(Files.readAllBytes(Paths.get("C:\\Projects\\indigo-dynamic\\src\\main\\resources\\FILE_SAMPLE.txt")), "UTF-8");
        List<String> columns = FileParser.readHeader(data, getTemplate());

        String originalCols = "sigungu_code,esb_link_id_ap2,nursing_agency_name,phone_number,zip_code,total_doctor,sigungu_code_name,encryption_symbol,esb_link_stcd_mid,homepage_url,esb_link_stcd_ap2,sido_code_name,esb_link_init_time_ap2,eupmyeondong_name,address,sido_code,longtitude,esb_link_end_time_ap2,latitude,esb_link_seq,opening_date,category_code,category_code_name";
        String[] originalColArr = originalCols.split(",");
        for (int i = 0; i < columns.size(); i++) {
            log.info("({}) Original: {} / Parsed: {}", i, originalColArr[i], columns.get(i));
            if (!originalColArr[i].equals(columns.get(i)))
                throw new Exception("Not equal");
        }
    }

    @Test
    public void readDataToList_test() throws Exception {
        //String data = new String(Files.readAllBytes(Paths.get("C:\\Projects\\indigo-dynamic\\src\\main\\resources\\FILE_SAMPLE_2.txt")), "UTF-8");
        String data = new String(Files.readAllBytes(Paths.get("C:\\Projects\\indigo-dynamic\\src\\main\\resources\\FILE_SAMPLE.txt")), "UTF-8");
        List<List<Object>> result = FileParser.readDataToList(data, getTemplate());

        for (int i = 0; i < result.size(); i++) {
            log.info("({}) Result: {}", i, result.get(i));
        }
    }

    @Test
    public void readRecord_test() throws Exception {
        System.out.println("asd");
    }

    public byte[] replaceByteArray(StringBuilder buffer) {
        byte[] result = null;
        if (buffer.indexOf("<![BINARY[") == 0 && ((buffer.lastIndexOf("]]>")) == (buffer.length() - "]]>".length())) ) {
            String[] byteStr = (buffer.substring("<![BINARY[".length(), buffer.length() - "]]>".length())).split(" ");
            int len = byteStr.length;
            result = new byte[len];
            for (int i = 0; i < len; i++) {
                result[i] = Byte.parseByte(byteStr[i]);
            }
        }

        return result;
    }

    @Test
    public void byte_parse_test() throws Exception {
        String text = "푸른하늘 은하수 하얀 쪽 배엔\n" +
                "계수나무 한 나무 토끼 한 마리\n" +
                "돛대도 아니 달고 삿대도 없이\n" +
                "가기도 잘도간다 서쪽 나라로\n" +
                "\n" +
                "은하수를 건너서 구름나라로\n" +
                "구름나라 지나선 어디로 가나\n" +
                "멀리서 반짝반짝 비치이는 건\n" +
                "샛별이 등대란다 길을 찾아라";
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        StringBuilder bd = new StringBuilder();
        bd.append("<![BINARY[");
        for (byte b : textBytes) {
            bd.append(b).append(" ");
        }
        bd.setLength(bd.length() - " ".length());
        bd.append("]]>");
        log.info("BINARY FORM: {}", bd.toString());

        byte[] bytes = replaceByteArray(bd);
        String parsedText = new String(bytes, "UTF-8");
        log.info("Equals: {}", parsedText.equals(text));
        log.info("Result String: {}", parsedText);
    }

}

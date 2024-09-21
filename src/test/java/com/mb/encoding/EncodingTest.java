package com.mb.encoding;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class EncodingTest {

    @Test
    public void ISO8859_1_test() throws Exception {
        String encoding = "ISO-8859-1";
        String korStr = "메추라기";
        byte[] ba1 = korStr.getBytes(encoding);
        byte[] ba2 = korStr.getBytes("UTF-8");
        byte[] ba3 = korStr.getBytes("EUC-KR");

        log.info("ISO-8859-1: {} / {}", ba1, new String(ba1, encoding));
        log.info("UTF-8: {} / {}", ba2, new String(ba2, "UTF-8"));
        log.info("EUC-KR: {} / {}", ba3, new String(ba3, "EUC-KR"));
    }

    @Test
    public void available_charset_test() {
        Map<String, Charset> map = Charset.availableCharsets();
        Charset.forName("ms949");

        for (Map.Entry<String, Charset> entry : map.entrySet()) {
            log.info("{}  ---  {}", entry.getKey(), entry.getValue());
        }
    }

    @Test
    public void encoding_test() {
        String data = "<![METADATA[{\n" +
                "  \"encoding\" : \"x-windows-949\",\n" +
                "  \"tx_id\" : \"FILE_WRITE_IF_TEST240921154750013428\",\n" +
                "  \"if_id\" : \"FILE_WRITE_IF_TEST\",\n" +
                "  \"service_id\" : \"FILE_WRITE\",\n" +
                "  \"add_header\" : true,\n" +
                "  \"delimiter\" : \"|\",\n" +
                "  \"qualifier\" : \"\",\n" +
                "  \"replacement_of_null_value\" : \"\",\n" +
                "  \"replacement_of_empty_value\" : \"\",\n" +
                "  \"replacement_of_line_feed\" : \"&cr;\",\n" +
                "  \"replacement_of_carriage_return\" : \"&lf;\",\n" +
                "  \"handle_binary_as_it_is\" : true,\n" +
                "  \"binary_data_wrapper\" : \"<![BINARY[]]>\",\n" +
                "  \"handle_binary_to_string\" : false\n" +
                "}]]>";

        log.info("UTF-8: {}/\n{}\n", data.length(), data.getBytes(StandardCharsets.UTF_8));
        log.info("UTF-16: {}/\n{}\n", data.length(), data.getBytes(Charset.forName("UTF-16")));
        log.info("UTF-32: {}/\n{}\n", data.length(), data.getBytes(Charset.forName("UTF-32")));
        log.info("MS949: {}/\n{}\n", data.length(), data.getBytes(Charset.forName("MS949")));
        log.info("ISO-8859-1: {}/\n{}\n", data.length(), data.getBytes(Charset.forName("ISO-8859-1")));
        log.info("ASCII: {}/\n{}\n", data.length(), data.getBytes(Charset.forName("ASCII")));
        log.info("CP949: {}/\n{}\n", data.length(), data.getBytes(Charset.forName("CP949")));
    }

    @Test
    public void encoding_test_2() {
        String data = "테스트<![METADATA[{\n" +
                "  \"encoding\" : \"x-windows-949\",\n" +
                "  \"tx_id\" : \"FILE_WRITE_IF_TEST240921154750013428\",\n" +
                "  \"if_id\" : \"FILE_WRITE_IF_TEST\",\n" +
                "  \"service_id\" : \"FILE_WRITE\",\n" +
                "  \"add_header\" : true,\n" +
                "  \"delimiter\" : \"|\",\n" +
                "  \"qualifier\" : \"\",\n" +
                "  \"replacement_of_null_value\" : \"\",\n" +
                "  \"replacement_of_empty_value\" : \"\",\n" +
                "  \"replacement_of_line_feed\" : \"&cr;\",\n" +
                "  \"replacement_of_carriage_return\" : \"&lf;\",\n" +
                "  \"handle_binary_as_it_is\" : true,\n" +
                "  \"binary_data_wrapper\" : \"<![BINARY[]]>\",\n" +
                "  \"handle_binary_to_string\" : false\n" +
                "}]]>";
        byte[] utf8Bytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] utf16Bytes = data.getBytes(Charset.forName("UTF-16"));

        if (new String(utf8Bytes, Charset.forName("UTF-8")).contains("\n")) {
            System.out.println("@@@@@");
        }
        if (new String(utf16Bytes, Charset.forName("UTF-16")).contains("\n")) {
            System.out.println("#####");
        }
    }
}

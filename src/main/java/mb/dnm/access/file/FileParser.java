package mb.dnm.access.file;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


public class FileParser {


    public static List<String> readHeader(String data, FileParserTemplate template) {
        String recordSeparator = template.getRecordSeparator();
        String qualifier = template.getQualifier();
        String delimiter = template.getDelimiter();

        int recordSeparatorIdx = data.indexOf(recordSeparator);
        if (recordSeparatorIdx == -1)
            throw new IllegalStateException("Invalid file content. Can not parse header columns. There is no record separator '" + recordSeparator +"'.");
        String recordLine = data.substring(0, recordSeparatorIdx);

        List<String> result = new ArrayList<>();

        StringBuilder buffer = new StringBuilder();
        int recordLineLen = recordLine.length();
        boolean open = false;
        boolean emptyQualifier = false;
        if (qualifier.isEmpty()) {
            emptyQualifier = true;
        }
        int redundantSize = 0;
        int qualifierLen = qualifier.length();

        for (int i = 0; i < recordLineLen; i++) {
            char c = recordLine.charAt(i);
            if (emptyQualifier) {

                buffer.append(c);
                if (buffer.indexOf(delimiter) != -1) {
                    buffer.setLength(buffer.length() - delimiter.length());
                    result.add(buffer.toString());
                    buffer.setLength(0);
                }

                if (buffer.indexOf(recordSeparator) != -1) {
                    buffer.setLength(buffer.length() - recordSeparator.length());
                    result.add(buffer.toString());
                    buffer.setLength(0);
                    break;
                }

            } else {

                buffer.append(c);

                //qualifier 가 처음 나왔을 때
                if (!open && buffer.indexOf(qualifier) != -1) {
                    open = true;
                    buffer.setLength(0);
                    //buffer를 초기상태로 만든다.
                    redundantSize = 0;
                    continue;
                }

                //qualifier 가 한 번 나온적이 있고 다시 나왔을 때
                if (open && buffer.indexOf(qualifier) != -1) {
                    buffer.setLength(buffer.length() - qualifierLen);
                    //buffer에서 qualifier를 지운다.
                    open = false;
                    redundantSize = 0;
                    continue;
                }

                if (open)
                    continue;

                ++redundantSize;

                //qualifier 가 모두 닫히고 delimiter 가 나왔을 때
                if (!open && buffer.indexOf(delimiter) != -1) {
                    buffer.setLength(buffer.length() - redundantSize);
                    result.add(buffer.toString());
                    buffer.setLength(0);
                    redundantSize = 0;
                    continue;
                }

                if (!open && buffer.indexOf(recordSeparator) != -1) {
                    buffer.setLength(buffer.length() - redundantSize);
                    result.add(buffer.toString());
                    buffer.setLength(0);
                    redundantSize = 0;
                    break;
                }

            }
        }
        
        return result;
    }




}

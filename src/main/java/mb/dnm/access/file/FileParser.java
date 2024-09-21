package mb.dnm.access.file;

import java.util.*;


public class FileParser {

    public static final String BINARY_DATA_WRAPPER = "<![BINARY[]]>";

    public static List<String> readHeader(String data, FileParserTemplate template) {
        String recordSeparator = template.getRecordSeparator();
        String qualifier = template.getQualifier();
        String delimiter = template.getDelimiter();

        int recordSeparatorIdx = data.indexOf(recordSeparator);
        if (recordSeparatorIdx == -1)
            throw new IllegalStateException("Invalid file content. Can not parse header columns. There is no record separator '" + recordSeparator +"'.");
        String recordLine = data.substring(0, recordSeparatorIdx + 1);

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

    public static List<List<Object>> readDataToList(String data, FileParserTemplate template) {
        String recordSeparator = template.getRecordSeparator();
        String qualifier = template.getQualifier();
        String delimiter = template.getDelimiter();
        String replacementOfEmptyValue = template.getReplacementOfEmptyValue();
        String replacementOfNullValue = template.getReplacementOfNullValue();
        String replacementOfCarriageReturn = template.getReplacementOfCarriageReturn();
        String replacementOfLineFeed = template.getReplacementOfLineFeed();
        boolean handleBinary = template.isHandleBinaryData();

        List<List<Object>> resultList = new ArrayList<>();
        List<Object> result = new ArrayList<>();

        StringBuilder buffer = new StringBuilder();

        boolean open = false;
        boolean emptyQualifier = false;
        if (qualifier.isEmpty()) {
            emptyQualifier = true;
        }
        int redundantSize = 0;
        int qualifierLen = qualifier.length();

        int dataLen = data.length();
        int initialColumnsSize = 0;
        int recordCount = 0;
        for (int i = 0; i < dataLen; i++) {
            char c = data.charAt(i);
            if (emptyQualifier) {
                buffer.append(c);
                if (buffer.indexOf(delimiter) != -1) {
                    buffer.setLength(buffer.length() - delimiter.length());

                    replaceNullReplacement(buffer, replacementOfNullValue);
                    replaceEmptyReplacement(buffer, replacementOfEmptyValue);
                    replaceCarriageReturnReplacement(buffer, replacementOfCarriageReturn);
                    replaceLineFeedReplacement(buffer, replacementOfLineFeed);
                    if (handleBinary) {
                        byte[] bytes = replaceByteArray(buffer);
                        if (bytes != null) {
                            result.add(bytes);
                        } else {
                            result.add(buffer.toString());
                        }
                    } else {
                        result.add(buffer.toString());
                    }

                    buffer.setLength(0);
                }

                if (buffer.indexOf(recordSeparator) != -1) {
                    buffer.setLength(buffer.length() - recordSeparator.length());

                    replaceNullReplacement(buffer, replacementOfNullValue);
                    replaceEmptyReplacement(buffer, replacementOfEmptyValue);
                    replaceCarriageReturnReplacement(buffer, replacementOfCarriageReturn);
                    replaceLineFeedReplacement(buffer, replacementOfLineFeed);
                    if (handleBinary) {
                        byte[] bytes = replaceByteArray(buffer);
                        if (bytes != null) {
                            result.add(bytes);
                        } else {
                            result.add(buffer.toString());
                        }
                    } else {
                        result.add(buffer.toString());
                    }

                    buffer.setLength(0);

                    ++recordCount;
                    if (initialColumnsSize == 0) {
                        initialColumnsSize = result.size();
                        if (initialColumnsSize == 0) {
                            throw new IllegalStateException("There are no columns to be parsed.");
                        }
                    } else {
                        if (initialColumnsSize != result.size()) {
                            throw new IllegalStateException("Columns size mismatch. Record number " + recordCount);
                        }
                    }
                    resultList.add(result);
                    result = new ArrayList<>();
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

                    replaceNullReplacement(buffer, replacementOfNullValue);
                    replaceEmptyReplacement(buffer, replacementOfEmptyValue);
                    replaceCarriageReturnReplacement(buffer, replacementOfCarriageReturn);
                    replaceLineFeedReplacement(buffer, replacementOfLineFeed);
                    if (handleBinary) {
                        byte[] bytes = replaceByteArray(buffer);
                        if (bytes != null) {
                            result.add(bytes);
                        } else {
                            result.add(buffer.toString());
                        }
                    } else {
                        result.add(buffer.toString());
                    }

                    buffer.setLength(0);
                    redundantSize = 0;
                    continue;
                }

                if (!open && buffer.indexOf(recordSeparator) != -1) {
                    buffer.setLength(buffer.length() - redundantSize);

                    replaceNullReplacement(buffer, replacementOfNullValue);
                    replaceEmptyReplacement(buffer, replacementOfEmptyValue);
                    replaceCarriageReturnReplacement(buffer, replacementOfCarriageReturn);
                    replaceLineFeedReplacement(buffer, replacementOfLineFeed);
                    if (handleBinary) {
                        byte[] bytes = replaceByteArray(buffer);
                        if (bytes != null) {
                            result.add(bytes);
                        } else {
                            result.add(buffer.toString());
                        }
                    } else {
                        result.add(buffer.toString());
                    }

                    buffer.setLength(0);
                    redundantSize = 0;

                    ++recordCount;
                    if (initialColumnsSize == 0) {
                        initialColumnsSize = result.size();
                        if (initialColumnsSize == 0) {
                            throw new IllegalStateException("There are no columns to be parsed.");
                        }
                    } else {
                        if (initialColumnsSize != result.size()) {
                            throw new IllegalStateException("Columns size mismatch. Record number " + recordCount);
                        }
                    }
                    resultList.add(result);
                    result = new ArrayList<>();
                }
            }

        }

        return resultList;
    }

    public static List<Map<String, Object>> readDataToRecord(String data, FileParserTemplate template) {
        return readDataToRecord(data, readHeader(data, template), template);
    }

    public static List<Map<String, Object>> readDataToRecord(String data, List<String> headers, FileParserTemplate template) {
        String recordSeparator = template.getRecordSeparator();
        String qualifier = template.getQualifier();
        String delimiter = template.getDelimiter();
        String replacementOfEmptyValue = template.getReplacementOfEmptyValue();
        String replacementOfNullValue = template.getReplacementOfNullValue();
        String replacementOfCarriageReturn = template.getReplacementOfCarriageReturn();
        String replacementOfLineFeed = template.getReplacementOfLineFeed();
        boolean handleBinary = template.isHandleBinaryData();

        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> result = new LinkedHashMap<>();

        StringBuilder buffer = new StringBuilder();

        boolean open = false;
        boolean emptyQualifier = false;
        if (qualifier.isEmpty()) {
            emptyQualifier = true;
        }
        int redundantSize = 0;
        int qualifierLen = qualifier.length();

        int dataLen = data.length();
        int initialColumnsSize = 0;
        int recordCount = 0;
        int columnIdx = 0;
        for (int i = 0; i < dataLen; i++) {
            char c = data.charAt(i);
            if (emptyQualifier) {
                buffer.append(c);
                if (buffer.indexOf(delimiter) != -1) {
                    buffer.setLength(buffer.length() - delimiter.length());

                    replaceNullReplacement(buffer, replacementOfNullValue);
                    replaceEmptyReplacement(buffer, replacementOfEmptyValue);
                    replaceCarriageReturnReplacement(buffer, replacementOfCarriageReturn);
                    replaceLineFeedReplacement(buffer, replacementOfLineFeed);
                    if (handleBinary) {
                        byte[] bytes = replaceByteArray(buffer);
                        if (bytes != null) {
                            result.put(headers.get(columnIdx), bytes);
                        } else {
                            result.put(headers.get(columnIdx), buffer.toString());    
                        }
                    } else {
                        result.put(headers.get(columnIdx), buffer.toString());
                    }

                    buffer.setLength(0);
                    ++columnIdx;
                }

                if (buffer.indexOf(recordSeparator) != -1) {
                    buffer.setLength(buffer.length() - recordSeparator.length());

                    replaceNullReplacement(buffer, replacementOfNullValue);
                    replaceEmptyReplacement(buffer, replacementOfEmptyValue);
                    replaceCarriageReturnReplacement(buffer, replacementOfCarriageReturn);
                    replaceLineFeedReplacement(buffer, replacementOfLineFeed);
                    if (handleBinary) {
                        byte[] bytes = replaceByteArray(buffer);
                        if (bytes != null) {
                            result.put(headers.get(columnIdx), bytes);
                        } else {
                            result.put(headers.get(columnIdx), buffer.toString());    
                        }
                    } else {
                        result.put(headers.get(columnIdx), buffer.toString());
                    }

                    buffer.setLength(0);

                    ++recordCount;
                    if (initialColumnsSize == 0) {
                        initialColumnsSize = result.size();
                        if (initialColumnsSize == 0) {
                            throw new IllegalStateException("There are no columns to be parsed.");
                        }
                    } else {
                        if (initialColumnsSize != result.size()) {
                            throw new IllegalStateException("Columns size mismatch. Record number " + recordCount);
                        }
                    }
                    resultList.add(result);
                    result = new LinkedHashMap<>();
                    columnIdx = 0;
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

                    replaceNullReplacement(buffer, replacementOfNullValue);
                    replaceEmptyReplacement(buffer, replacementOfEmptyValue);
                    replaceCarriageReturnReplacement(buffer, replacementOfCarriageReturn);
                    replaceLineFeedReplacement(buffer, replacementOfLineFeed);
                    if (handleBinary) {
                        byte[] bytes = replaceByteArray(buffer);
                        if (bytes != null) {
                            result.put(headers.get(columnIdx), bytes);
                        } else {
                            result.put(headers.get(columnIdx), buffer.toString());    
                        }
                    } else {
                        result.put(headers.get(columnIdx), buffer.toString());
                    }

                    buffer.setLength(0);
                    redundantSize = 0;
                    ++columnIdx;
                    continue;
                }

                if (!open && buffer.indexOf(recordSeparator) != -1) {
                    buffer.setLength(buffer.length() - redundantSize);

                    replaceNullReplacement(buffer, replacementOfNullValue);
                    replaceEmptyReplacement(buffer, replacementOfEmptyValue);
                    replaceCarriageReturnReplacement(buffer, replacementOfCarriageReturn);
                    replaceLineFeedReplacement(buffer, replacementOfLineFeed);
                    if (handleBinary) {
                        byte[] bytes = replaceByteArray(buffer);
                        if (bytes != null) {
                            result.put(headers.get(columnIdx), bytes);
                        } else {
                            result.put(headers.get(columnIdx), buffer.toString());    
                        }
                    } else {
                        result.put(headers.get(columnIdx), buffer.toString());
                    }

                    buffer.setLength(0);
                    redundantSize = 0;

                    ++recordCount;
                    if (initialColumnsSize == 0) {
                        initialColumnsSize = result.size();
                        if (initialColumnsSize == 0) {
                            throw new IllegalStateException("There are no columns to be parsed.");
                        }
                    } else {
                        if (initialColumnsSize != result.size()) {
                            throw new IllegalStateException("Columns size mismatch. Record number " + recordCount);
                        }
                    }
                    resultList.add(result);
                    result = new LinkedHashMap<>();
                    columnIdx = 0;
                }
            }

        }

        return resultList;
    }

    private static StringBuilder replaceNullReplacement(StringBuilder buffer, String replacement) {
        if (buffer.indexOf(replacement) == 0 && buffer.length() == replacement.length()) {
            buffer.setLength(0);
        }
        return buffer;
    }

    private static StringBuilder replaceEmptyReplacement(StringBuilder buffer, String replacement) {
        if (buffer.indexOf(replacement) == 0 && buffer.length() == replacement.length()) {
            buffer.setLength(0);
        }
        return buffer;
    }

    private static StringBuilder replaceCarriageReturnReplacement(StringBuilder buffer, String replacement) {
        int idx = -1;
        while ((idx = buffer.indexOf(replacement)) != -1) {
            buffer.delete(idx, idx + replacement.length());
            buffer.insert(idx, "\r");
        }
        return buffer;
    }

    private static StringBuilder replaceLineFeedReplacement(StringBuilder buffer, String replacement) {
        int idx = -1;
        while ((idx = buffer.indexOf(replacement)) != -1) {
            buffer.delete(idx, idx + replacement.length());
            buffer.insert(idx, "\n");
        }
        return buffer;
    }

    private static byte[] replaceByteArray(StringBuilder buffer) {
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
}

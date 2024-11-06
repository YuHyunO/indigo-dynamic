package mb.dnm.access.file;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class FileParserTemplate implements Serializable {
    private static final long serialVersionUID = -8496697747879161631L;
    private final String recordSeparator;
    private final String delimiter;
    private final String qualifier;
    private final String replacementOfNullValue;
    private final String replacementOfEmptyValue;
    private final String replacementOfLineFeed;
    private final String replacementOfCarriageReturn;
    private final boolean headerExist;
    private final boolean handleBinaryData;

    public FileParserTemplate(String recordSeparator, String delimiter, String qualifier,
                              String replacementOfNullValue, String replacementOfEmptyValue,
                              String replacementOfLineFeed, String replacementOfCarriageReturn,
                              boolean headerExist, boolean handleBinaryData) {

        this.recordSeparator = recordSeparator;
        this.delimiter = delimiter;
        this.qualifier = qualifier;
        this.replacementOfNullValue = replacementOfNullValue;
        this.replacementOfEmptyValue = replacementOfEmptyValue;
        this.replacementOfLineFeed = replacementOfLineFeed;
        this.replacementOfCarriageReturn = replacementOfCarriageReturn;
        this.headerExist = headerExist;
        this.handleBinaryData = handleBinaryData;
    }

}

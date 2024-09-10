package mb.dnm.mapper;

import java.io.IOException;

@Deprecated
public class MapperParsingException extends IOException {
    public MapperParsingException(String message) {
        super("Mapper parsing failed. " + message);
    }
}

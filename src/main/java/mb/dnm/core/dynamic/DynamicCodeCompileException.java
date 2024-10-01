package mb.dnm.core.dynamic;

public class DynamicCodeCompileException extends Exception {
    public DynamicCodeCompileException(int line, String message) {
        super("line " + line + ": " + message);
    }

    public DynamicCodeCompileException(String message) {
        super(message);
    }


    public DynamicCodeCompileException(String message, Throwable cause) {
        super(message, cause);
    }
}

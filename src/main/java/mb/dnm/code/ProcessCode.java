package mb.dnm.code;

public enum ProcessCode {
    SERVICE_NOT_FOUND("NF"),
    NOT_STARTED("N"),
    IN_PROCESS("P"),
    SUCCESS("S"),
    FAILURE("F"),
    SERVICE_FAILURE("B"),
    DYNAMIC_CODE_FAILURE("DF"),
    ENFORCED_FAILURE("EF")
    ;

    private final String processCode;

    ProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public String getProcessCode() {
        return processCode;
    }
}

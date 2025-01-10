package mb.dnm.code;

/**
 * The enum Process code.
 */
public enum ProcessCode {
    /**
     * Service not found process code.
     */
    SERVICE_NOT_FOUND("NF"),
    /**
     * Not started process code.
     */
    NOT_STARTED("N"),
    /**
     * In process process code.
     */
    IN_PROCESS("P"),
    /**
     * Success process code.
     */
    SUCCESS("S"),
    /**
     * Failure process code.
     */
    FAILURE("F"),
    /**
     * Service failure process code.
     */
    SERVICE_FAILURE("B"),
    /**
     * Dynamic code failure process code.
     */
    DYNAMIC_CODE_FAILURE("DF"),
    /**
     * Enforced failure process code.
     */
    ENFORCED_FAILURE("EF")
    ;

    private final String processCode;

    ProcessCode(String processCode) {
        this.processCode = processCode;
    }

    /**
     * Gets process code.
     *
     * @return the process code
     */
    public String getProcessCode() {
        return processCode;
    }
}

package com.mb.code;

public enum ProcessCode {

    NOT_STARTED("N"),
    IN_PROCESS("P"),
    SUCCESS("S"),
    FAILURE("F"),
    SERVICE_FAILURE("B")
    ;

    private final String processCode;

    ProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public String getProcessCode() {
        return processCode;
    }
}

package com.drugsafety.service;

public class AgentQueryException extends RuntimeException {
    private final String code;
    private final int httpStatus;

    public AgentQueryException(String code, int httpStatus) {
        super(code);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() { return code; }
    public int getHttpStatus() { return httpStatus; }
}

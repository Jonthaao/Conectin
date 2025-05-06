package com.conectin.conectin.payload;

public class SuccessMessage {
    private final String message;
    private final String successCode;

    public SuccessMessage(String message, String successCode) {
        this.message = message;
        this.successCode = successCode;
    }

    public String getMessage() {
        return message;
    }

    public String getSuccessCode() {
        return successCode;
    }
}

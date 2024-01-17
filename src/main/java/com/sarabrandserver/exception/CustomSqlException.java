package com.sarabrandserver.exception;

public class CustomSqlException extends RuntimeException {

    public CustomSqlException(String reason) {
        super(reason);
    }

}

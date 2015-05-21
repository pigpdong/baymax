 package com.tongbanjie.baymax.exception;

import org.springframework.dao.UncategorizedDataAccessException;

public class UncategorizedBayMaxException extends UncategorizedDataAccessException {
    private static final long serialVersionUID = -5001927974502714777L;

    public UncategorizedBayMaxException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

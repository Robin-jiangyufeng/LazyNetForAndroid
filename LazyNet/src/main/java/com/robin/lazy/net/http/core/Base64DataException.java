package com.robin.lazy.net.http.core;

import java.io.IOException;

public class Base64DataException extends IOException {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1375405953042872273L;

    public Base64DataException(String detailMessage) {
        super(detailMessage);
    }
}
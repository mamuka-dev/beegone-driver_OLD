package com.gotaxi.driver.Models;

public class docResposne {
    private boolean error;
    private String message;

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public docResposne(boolean error, String message) {
        this.error = error;
        this.message = message;
    }
}

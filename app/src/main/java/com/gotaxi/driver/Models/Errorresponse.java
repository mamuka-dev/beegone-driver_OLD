package com.gotaxi.driver.Models;

public class Errorresponse {
    private boolean error;

    public boolean isError() {
        return error;
    }

    public Errorresponse(boolean error) {
        this.error = error;
    }
}

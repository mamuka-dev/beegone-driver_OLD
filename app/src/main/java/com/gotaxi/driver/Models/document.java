package com.gotaxi.driver.Models;

public class document {
    private String did, dname, dtype;

    public String getDid() {
        return did;
    }

    public String getDname() {
        return dname;
    }

    public String getDtype() {
        return dtype;
    }

    public document(String did, String dname, String dtype) {
        this.did = did;
        this.dname = dname;
        this.dtype = dtype;
    }
}

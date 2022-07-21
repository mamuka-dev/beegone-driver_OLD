package com.gotaxi.driver.Models;

import java.util.List;

public class ConstDataResponse {
    private List<ConstData> data;

    public List<ConstData> getData() {
        return data;
    }

    public ConstDataResponse(List<ConstData> data) {
        this.data = data;
    }
}

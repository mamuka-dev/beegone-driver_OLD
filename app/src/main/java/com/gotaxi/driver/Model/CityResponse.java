package com.gotaxi.driver.Model;

import com.gotaxi.driver.Models.City;

import java.util.List;

public class CityResponse {
    private boolean verification;
    private List<City> city;

    public boolean isVerification() {
        return verification;
    }

    public List<City> getCity() {
        return city;
    }

    public CityResponse(boolean verification, List<City> city) {
        this.verification = verification;
        this.city = city;
    }
}

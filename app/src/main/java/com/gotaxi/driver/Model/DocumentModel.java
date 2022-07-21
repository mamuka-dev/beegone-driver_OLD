package com.gotaxi.driver.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DocumentModel {

    @SerializedName("driver")
    @Expose
    private List<DriverVehicleModel> driver = null;
    @SerializedName("vehicle")
    @Expose
    private List<DriverVehicleModel> vehicle = null;

    public List<DriverVehicleModel> getDriver() {
        return driver;
    }

    public void setDriver(List<DriverVehicleModel> driver) {
        this.driver = driver;
    }

    public List<DriverVehicleModel> getVehicle() {
        return vehicle;
    }

    public void setVehicle(List<DriverVehicleModel> vehicle) {
        this.vehicle = vehicle;
    }
}

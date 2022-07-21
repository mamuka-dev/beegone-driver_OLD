package com.gotaxi.driver.Models;

import java.util.List;

public class Servicelistresponse {
    private boolean error;
    private List<Servicelist> serviceslist;

    public boolean isError() {
        return error;
    }

    public List<Servicelist> getServiceslist() {
        return serviceslist;
    }

    public Servicelistresponse(boolean error, List<Servicelist> serviceslist) {
        this.error = error;
        this.serviceslist = serviceslist;
    }
}

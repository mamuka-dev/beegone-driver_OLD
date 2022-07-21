package com.gotaxi.driver.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DriverVehicleModel implements Serializable {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("provider_document")
    @Expose
    private ProviderDocumentsModel providerDocument;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ProviderDocumentsModel getProviderDocument() {
        return providerDocument;
    }

    public void setProviderDocument(ProviderDocumentsModel providerDocument) {
        this.providerDocument = providerDocument;
    }
}

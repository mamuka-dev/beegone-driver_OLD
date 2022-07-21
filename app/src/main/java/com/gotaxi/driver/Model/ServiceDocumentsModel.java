package com.gotaxi.driver.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ServiceDocumentsModel {
    @SerializedName("services")
    @Expose
    private List<ServiceModel> services = null;
    @SerializedName("selectedService")
    @Expose
    private SelectedServiceModel selectedService;
    @SerializedName("documents")
    @Expose
    private DocumentModel documents;

    public List<ServiceModel> getServices() {
        return services;
    }

    public void setServices(List<ServiceModel> services) {
        this.services = services;
    }

    public SelectedServiceModel getSelectedService() {
        return selectedService;
    }

    public void setSelectedService(SelectedServiceModel selectedService) {
        this.selectedService = selectedService;
    }

    public DocumentModel getDocuments() {
        return documents;
    }

    public void setDocuments(DocumentModel documents) {
        this.documents = documents;
    }
}

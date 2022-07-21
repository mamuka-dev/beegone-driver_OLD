package com.gotaxi.driver.Models;

import java.util.List;

public class DocumentResponse {
    private boolean error;
    private List<document> documents;

    public boolean isError() {
        return error;
    }

    public List<document> getDocuments() {
        return documents;
    }

    public DocumentResponse(boolean error, List<document> documents) {
        this.error = error;
        this.documents = documents;
    }
}

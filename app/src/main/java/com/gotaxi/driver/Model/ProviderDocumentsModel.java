package com.gotaxi.driver.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProviderDocumentsModel implements Serializable {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("provider_id")
    @Expose
    private Integer providerId;
    @SerializedName("document_id")
    @Expose
    private String documentId;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("unique_id")
    @Expose
    private Object uniqueId;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("expires_at")
    @Expose
    private Object expiresAt;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProviderId() {
        return providerId;
    }

    public void setProviderId(Integer providerId) {
        this.providerId = providerId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Object getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Object uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Object expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ProviderDocumentsModel{" +
                "id=" + id +
                ", providerId=" + providerId +
                ", documentId='" + documentId + '\'' +
                ", url='" + url + '\'' +
                ", uniqueId=" + uniqueId +
                ", status='" + status + '\'' +
                ", expiresAt=" + expiresAt +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}

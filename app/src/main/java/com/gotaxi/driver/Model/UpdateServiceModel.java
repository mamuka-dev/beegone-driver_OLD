package com.gotaxi.driver.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateServiceModel {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private UpdateServiceDataModel data;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public UpdateServiceDataModel getData() {
        return data;
    }

    public void setData(UpdateServiceDataModel data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "UpdateServiceModel{" +
                "success=" + success +
                ", data=" + data +
                '}';
    }
}

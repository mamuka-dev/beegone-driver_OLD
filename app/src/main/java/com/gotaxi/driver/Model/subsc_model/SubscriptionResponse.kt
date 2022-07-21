package com.cheetah.driver.Model.subsc_model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SubscriptionResponse {
    @SerializedName("status")
    @Expose
    var status: Int? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("packages")
    @Expose
    var packages: List<Package>? = null
}
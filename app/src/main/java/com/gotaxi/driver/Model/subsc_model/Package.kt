package com.cheetah.driver.Model.subsc_model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Package {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("pkg_id")
    @Expose
    var pkgId: String? = null

    @SerializedName("is_sub")
    @Expose
    var isSub: Boolean? = false

    @SerializedName("title")
    @Expose
    var title: String? = null

    @SerializedName("amount")
    @Expose
    var amount: String? = null

    @SerializedName("rides_count")
    @Expose
    var ridesCount: String? = null
}
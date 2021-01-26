package com.seemsnerdy.trading.api.objects

import com.google.gson.annotations.SerializedName

enum class OrderStatus {
    @SerializedName("New")
    New,

    @SerializedName("PartiallyFill")
    PartiallyFill,

    @SerializedName("Fill")
    Fill,

    @SerializedName("Cancelled")
    Cancelled,

    @SerializedName("Replaced")
    Replaced,

    @SerializedName("PendingCancel")
    PendingCancel,

    @SerializedName("Rejected")
    Rejected,

    @SerializedName("PendingReplace")
    PendingReplace,

    @SerializedName("PendingNew")
    PendingNew
}
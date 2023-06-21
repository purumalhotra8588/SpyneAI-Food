package com.spyneai.shootapp.data.model

import com.google.gson.annotations.SerializedName

data class Credits(
    var total : CreditItem,
    @SerializedName("exterior")
    var exterior : CreditItem = CreditItem(),
    @SerializedName("interior")
    var interior : CreditItem = CreditItem(),
    @SerializedName("miscellanous")
    var miscellanous : CreditItem = CreditItem()
)
package com.example.bikepark.Data

data class Request(var rid: String?,
                    var latitude: String?,
                    var longitude: String?,
                    var address: String?,
                    var number: Int?,
                    var maxNumber: Int,
                    var status: String?)

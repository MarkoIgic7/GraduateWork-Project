package com.example.bikepark.Data

data class Spot(val sid: String,
                var user: String,
                val parking: String,
                var free: Boolean,
                var time: String,
                val qrCodeData: String)

package com.example.bikepark.Data

data class User(val uid:String?,
                val email:String,
                var password: String,
                val name: String?= null,
                val surname: String ?= null,
                val number: String ?= null,
                val imgName:String,
                val mySpots: ArrayList<String> = ArrayList<String>(),
                val myRequests: ArrayList<String> = ArrayList<String>(),
                var banTime: String)

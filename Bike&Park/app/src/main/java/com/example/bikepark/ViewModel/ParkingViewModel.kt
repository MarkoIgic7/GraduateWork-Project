package com.example.bikepark.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikepark.Data.Parking
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson

class ParkingViewModel : ViewModel() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    val _parkings: MutableLiveData<ArrayList<Parking>> = MutableLiveData<ArrayList<Parking>>()
    var _parkingsObject  = ArrayList<Parking>()

    init {
        databaseReference = FirebaseDatabase.getInstance("https://bikepark-b1873-default-rtdb.firebaseio.com/").getReference("Parkings")
        storageReference = FirebaseStorage.getInstance().reference
    }
    fun addParking(p: Parking)
    {
        _parkings.value?.add(p)
        databaseReference.child(p.pid!!).setValue(p).addOnCompleteListener {  }
    }
    fun getAllParkings()
    {
        val parkingsList: ArrayList<Parking> = ArrayList<Parking>()
        databaseReference.get().addOnSuccessListener {
            if (it.getValue() != null) {
                val q: Map<String, Object> = it.getValue() as HashMap<String, Object>
                q.forEach { (key, value) ->
                    run {

                        val gson = Gson()
                        val json = gson.toJson(value)
                        val ps = gson.fromJson(json, Parking::class.java)
                        parkingsList.add(ps)
                    }
                }
            } else {
            }
            parkingsList.sortBy { spot: Parking -> spot.pid  }
            _parkingsObject = parkingsList
            _parkings.value = parkingsList
            _parkings.value!!.sortByDescending { it.rating }
        }
    }
    fun updateParking(parking: Parking){

        val liveDB =FirebaseDatabase.getInstance("https://bikepark-b1873-default-rtdb.firebaseio.com/").getReference("Parkings/").child(parking.pid!!)
        //_parkings.value!!.removeIf { it.pid==parking.pid }
        //_parkings.value!!.add(parking)
        //_spots.value!!.sortByDescending { it.points }
        val update = mapOf<String,Any>("currentNum" to parking.currentNum!!.toInt(),"numOfUsings" to parking.numOfUsings!!.toInt(),"sum" to parking.sum!!,"rating" to parking.rating!!)

        liveDB.updateChildren(update).addOnSuccessListener {

        }
    }
}
package com.example.bikepark.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikepark.Data.Parking
import com.example.bikepark.Data.Spot
import com.example.bikepark.Data.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson

class SpotViewModel : ViewModel() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    val _spots: MutableLiveData<ArrayList<Spot>> = MutableLiveData<ArrayList<Spot>>()
    var _spotsObject  = ArrayList<Spot>()

    init {
        databaseReference = FirebaseDatabase.getInstance("https://bikepark-b1873-default-rtdb.firebaseio.com/").getReference("Spots")
        storageReference = FirebaseStorage.getInstance().reference
    }
    fun addSpot(s: Spot)
    {
        _spots.value?.add(s)
        databaseReference.child(s.sid!!).setValue(s).addOnCompleteListener {  }
    }
    fun getAllSpots()
    {
        val spotsList: ArrayList<Spot> = ArrayList<Spot>()
        databaseReference.get().addOnSuccessListener {
            if (it.getValue() != null) {
                val q: Map<String, Object> = it.getValue() as HashMap<String, Object>
                q.forEach { (key, value) ->
                    run {

                        val gson = Gson()
                        val json = gson.toJson(value)
                        val ps = gson.fromJson(json, Spot::class.java)
                        spotsList.add(ps)
                    }
                }
            } else {
            }
            spotsList.sortBy { spot: Spot -> spot.sid  }
            _spotsObject = spotsList
            _spots.value = spotsList
            _spots.value!!.sortByDescending { it.sid }
        }
    }
    fun updateSpot(spot: Spot){

        val liveDB =FirebaseDatabase.getInstance().getReference("Spots/").child(spot.sid!!)
        //_users.value!!.removeIf { it.uid==user.uid }
        //_users.value!!.add(user)
        //_users.value!!.sortByDescending { it.points }
        val update= mapOf<String,Any>("free" to spot.free,"user" to spot.user,"time" to spot.time)
        liveDB.updateChildren(update).addOnSuccessListener {}
    }
}
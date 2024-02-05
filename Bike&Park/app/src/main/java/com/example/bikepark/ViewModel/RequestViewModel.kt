package com.example.bikepark.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikepark.Data.Parking
import com.example.bikepark.Data.Request
import com.example.bikepark.Data.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson

class RequestViewModel: ViewModel() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    val _requests: MutableLiveData<ArrayList<Request>> = MutableLiveData<ArrayList<Request>>()
    var _requestsObject  = ArrayList<Request>()

    init {
        databaseReference = FirebaseDatabase.getInstance("https://bikepark-b1873-default-rtdb.firebaseio.com/").getReference("Requests")
        storageReference = FirebaseStorage.getInstance().reference
    }
    fun addRequest(r: Request)
    {
        _requests.value?.add(r)
        databaseReference.child(r.rid!!).setValue(r).addOnCompleteListener {  }
    }
    fun getAllRequestss()
    {
        val requestsList: ArrayList<Request> = ArrayList<Request>()
        databaseReference.get().addOnSuccessListener {
            if (it.getValue() != null) {
                val q: Map<String, Object> = it.getValue() as HashMap<String, Object>
                q.forEach { (key, value) ->
                    run {

                        val gson = Gson()
                        val json = gson.toJson(value)
                        val r = gson.fromJson(json, Request::class.java)
                        requestsList.add(r)
                    }
                }
            } else {
            }
            requestsList.sortBy { request: Request -> request.rid  }
            _requestsObject = requestsList
            _requests.value = requestsList
            _requests.value!!.sortByDescending { it.rid }
        }
    }
    fun updateRequest(request: Request){

        val liveDB =FirebaseDatabase.getInstance().getReference("Requests/").child(request.rid!!)
        val requestList = _requests.value ?: ArrayList()
        val indexOfRequest = requestList.indexOfFirst { it.rid == request.rid }
        if(indexOfRequest != -1 ){
            requestList[indexOfRequest] = request
        }
        _requests.value = requestList
        //_requests.value!!.removeIf { it.rid==request.rid }
        //_requests.value!!.add(request)
        //_users.value!!.sortByDescending { it.points }
        val update= mapOf<String,Any>("number" to request.number.toString(),"status" to request.status.toString())
        liveDB.updateChildren(update).addOnSuccessListener {}
    }
}
package com.example.bikepark.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikepark.Data.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await

class UserViewModel: ViewModel() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    val _users: MutableLiveData<ArrayList<User>> = MutableLiveData<ArrayList<User>>()
    var _usersObject  = ArrayList<User>()


    fun addUser(u: User)
    {
        _users.value?.add(u)
        databaseReference.child(u.uid!!).setValue(u).addOnCompleteListener {  }
    }
    fun getAllUsers() {
        val userList: ArrayList<User> = ArrayList<User>()
        databaseReference.get().addOnSuccessListener {
            if (it.getValue() != null) {
                val q: Map<String, Object> = it.getValue() as HashMap<String, Object>
                q.forEach { (key, value) ->
                    run {
                        val gson = Gson()
                        val json = gson.toJson(value)
                        val u = gson.fromJson(json, User::class.java)
                        userList.add(u)
                    }
                }
            } else { }
            userList.sortBy { user: User -> user.name  }
            _usersObject = userList
            _users.value = userList

        }
    }

    fun updateUser(user: User){

        val liveDB =FirebaseDatabase.getInstance().getReference("Users/").child(user.uid!!)
        //_users.value!!.removeIf { it.uid==user.uid }
        //_users.value!!.add(user)
        //_users.value!!.sortByDescending { it.points }
        val update= mapOf<String,Any>("myRequests" to user.myRequests,"mySpots" to user.mySpots,"password" to user.password,"banTime" to user.banTime)
        liveDB.updateChildren(update).addOnSuccessListener {}
    }

    init {
        databaseReference = FirebaseDatabase.getInstance("https://bikepark-b1873-default-rtdb.firebaseio.com/").getReference("Users")
        storageReference = FirebaseStorage.getInstance().reference
    }
}
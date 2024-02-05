package com.example.bikepark

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.bikepark.Data.Spot
import com.example.bikepark.Data.User
import org.w3c.dom.Text

class SpotAdapter(context: Context, spotsArrayList: ArrayList<Spot>, usersArrayList:ArrayList<User>): ArrayAdapter<Spot>(context,R.layout.spot_item,spotsArrayList) {

    private val usersList: ArrayList<User> = usersArrayList

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val listData = getItem(position)

        if(view== null){
            view = LayoutInflater.from(context).inflate(R.layout.spot_item,parent,false)
        }

        val counter = view!!.findViewById<TextView>(R.id.spotItem_counter)
        val state = view!!.findViewById<TextView>(R.id.spotItem_State)
        val user = view!!.findViewById<TextView>(R.id.spotItem_User)
        val time = view!!.findViewById<TextView>(R.id.spotItem_Time)

        counter.text = (position+1).toString()
        //Toast.makeText(context,listData!!.free.toString(),Toast.LENGTH_SHORT).show()
        state.text = if(listData!!.free == true) "Slobodno" else "Zauzeto"
            if(listData.free == true) {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.enough_requests))
            } else {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.not_enough_requests))
            }
        val userOfSpot = usersList.find {u-> u.uid==listData.user }
        if(userOfSpot==null){
            user.text = "Korisnik"
        } else {
            user.text = userOfSpot.name + " " + userOfSpot.surname
        }
        //time setovati
        time.text = listData.time


        return view!!
    }
}
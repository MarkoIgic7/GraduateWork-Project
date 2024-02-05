package com.example.bikepark

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bikepark.Data.Parking
import com.example.bikepark.Data.Spot
import com.example.bikepark.ViewModel.ParkingViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ParkingAdapter(
    private val parkingList: ArrayList<Parking?>,
    private val spotList: ArrayList<Spot?>,
    private val parkingViewModel: ParkingViewModel,
    context: Context
) : RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder>(){

    class ParkingViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val counter: TextView = itemView.findViewById(R.id.parkingItem_counter)
        val img: ImageView = itemView.findViewById(R.id.parkingItem_image)
        val address: TextView = itemView.findViewById(R.id.parkingItem_address)
        val ratingBar: RatingBar = itemView.findViewById(R.id.parkingItem_ratingBar)
        val availableSpots: TextView = itemView.findViewById(R.id.parkingItem_greenBike)
        val busySpots: TextView = itemView.findViewById(R.id.parkingItem_redBike)
        val soonAvailable: TextView = itemView.findViewById(R.id.parkingItem_yellowBike)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ParkingAdapter.ParkingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.parking_item, parent, false)
        return ParkingAdapter.ParkingViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ParkingAdapter.ParkingViewHolder, position: Int) {
        val parking = parkingList[position]
        holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.purple_700))

        //Counter
        holder.counter.text = (position+1).toString()
        //Image
        Glide.with(holder.itemView.context).load(parking!!.img).into(holder.img)
        //Address
        val wholeAddress = parking!!.address
        val addressFirstPart =  wholeAddress!!.substringBefore(", Serbia")
        val addressFinalPart = addressFirstPart.substringAfter("Adresa : ")
        holder.address.text = addressFinalPart
        //Rating
        holder.ratingBar.rating = parking.rating!!.toFloat()
        //Available,NotAvailble,SoonAvailable
        var availableSpots = 0
        var notAvailableSpots = 0
        var soonAvailableSpots = 0
        val spotsOfParking =  ArrayList<Spot>()
        spotList.forEach { it->
            if(it!!.parking==parking.pid){
                spotsOfParking.add(it)
            }
        }

        spotsOfParking.forEach { it->
            if(it.free==true){
                availableSpots++
            } else{
                val spotTime = LocalDateTime.parse(it.time,DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
                val currentTime = LocalDateTime.now()
                val minutesDifference = ChronoUnit.MINUTES.between(currentTime, spotTime)
                if(minutesDifference<15){
                    soonAvailableSpots++
                } else{
                    notAvailableSpots++
                }
            }
        }
        holder.availableSpots.text = availableSpots.toString()
        holder.busySpots.text = notAvailableSpots.toString()
        holder.soonAvailable.text = soonAvailableSpots.toString()





    }

    override fun getItemCount(): Int {
        return parkingList.size
    }

}
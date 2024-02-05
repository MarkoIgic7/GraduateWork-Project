package com.example.bikepark.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import com.example.bikepark.Data.Parking
import com.example.bikepark.Data.Spot
import com.example.bikepark.Data.User
import com.example.bikepark.R
import com.example.bikepark.SpotAdapter
import com.example.bikepark.ViewModel.ParkingViewModel
import com.example.bikepark.ViewModel.RequestViewModel
import com.example.bikepark.ViewModel.SpotViewModel
import com.example.bikepark.ViewModel.UserViewModel


class ParkingFragment : Fragment() {


    private val parkingViewModel: ParkingViewModel by activityViewModels()
    private val spotViewModel: SpotViewModel by activityViewModels()
    private val requestViewModel: RequestViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    var spotsArrayList = ArrayList<Spot>()
    var usersArrayList = ArrayList<User>()


    private lateinit var originLatLng: String
    private lateinit var destinationLatLng: String

    private lateinit var directionsButton: Button
    private lateinit var listView: ListView
    private lateinit var availableSpotsLabel: TextView

    private lateinit var parkingID: String
    private lateinit var spotAdapter: SpotAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_parking, container, false)

        directionsButton = view.findViewById(R.id.parkingF_directionsBtn)
        listView = view.findViewById(R.id.parkingF_listview)
        availableSpotsLabel = view.findViewById(R.id.parkingF_availableSpots)

        directionsButton.setOnClickListener({
            showDirections(this.originLatLng,this.destinationLatLng)
        })

        // ovo je asinhrono i parkingID se nabavlja naknadno tako da svo pribavljanje podataka
        // mora da bude u ovom bloku
        setFragmentResultListener("Lokacije"){requestKey,bundle ->
            this.originLatLng = bundle.getString("originLatLng").toString()
            this.destinationLatLng = bundle.getString("destinationLatLng").toString()
            this.parkingID = bundle.getString("parkingID").toString()
            Toast.makeText(context,this.originLatLng,Toast.LENGTH_SHORT).show()
            Toast.makeText(context,this.destinationLatLng,Toast.LENGTH_SHORT).show()
            Toast.makeText(context,this.parkingID,Toast.LENGTH_SHORT).show()

            parkingViewModel.getAllParkings()
            spotViewModel.getAllSpots()
            userViewModel.getAllUsers()
            //Toast.makeText(context!!,this.parkingID,Toast.LENGTH_LONG).show()
            var availableSpots: Int = 0
            val parking = parkingViewModel._parkings.value!!.find { parking: Parking -> parking.pid == this.parkingID }
            val address = parking!!.address!!.substringBefore(", Serbia")
            activity!!.setTitle(address)
            spotViewModel._spots.value!!.forEach{s->
                if(s.parking == parking!!.pid){
                    spotsArrayList.add(s)
                    if(s.free === true){
                        availableSpots++
                    }
                }
            }
            userViewModel._users.value!!.forEach { u ->
                usersArrayList.add(u)
            }
            availableSpotsLabel.text = "Dostupno :" +" "+availableSpots.toString()+"/"+parking!!.maxNum.toString()
//            spotViewModel._spots.observe(viewLifecycleOwner, Observer {
//                if(spotViewModel._spots.value != null){
//                    spotViewModel._spots.value!!.forEach{s->
//                        if(s.parking == parking!!.pid){
//                            spotsArrayList.add(s)
//                            if(s.free === true){
//                                availableSpots++
//                            }
//                        }
//
//                    }
//
//                }
//                availableSpotsLabel.text = "Dostupno :" +" "+availableSpots.toString()+"/"+parking!!.maxNum.toString()
//            })


        }





        spotAdapter = SpotAdapter(context!!,spotsArrayList,usersArrayList)
        view.findViewById<ListView>(R.id.parkingF_listview).adapter = spotAdapter
        return view
    }

    fun showDirections(origin: String,destination: String){
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${origin}&destination=${destination}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        // Check if the Google Maps app is installed
        if (intent.resolveActivity(activity!!.packageManager) != null) {
            startActivity(intent)
        } else {
            // If Google Maps app is not installed, open the maps website in a browser
            val mapsWebsiteUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${origin}&destination=${destination}")
            val mapsWebsiteIntent = Intent(Intent.ACTION_VIEW, mapsWebsiteUri)
            startActivity(mapsWebsiteIntent)
        }
    }


}
package com.example.bikepark.Fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bikepark.Data.Parking
import com.example.bikepark.Data.Spot
import com.example.bikepark.OnItemClickListener
import com.example.bikepark.ParkingAdapter
import com.example.bikepark.R
import com.example.bikepark.ViewModel.ParkingViewModel
import com.example.bikepark.ViewModel.SpotViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener


class LocationsFragment : Fragment() {
    private val parkingViewModel: ParkingViewModel by activityViewModels()
    private val spotViewModel: SpotViewModel by activityViewModels()
    private lateinit var parkingAdapter: ParkingAdapter


    private lateinit var recyclerView: RecyclerView


    var parkingsArrayList= ArrayList<Parking?>()
    var spotsArayList = ArrayList<Spot?>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parkingViewModel.getAllParkings()
        spotViewModel.getAllSpots()



    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_locations, container, false)
        recyclerView = view.findViewById(R.id.locationsF_recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context!!)
        activity!!.setTitle("Parking stajalista")

        spotViewModel._spots.observe(viewLifecycleOwner,Observer{
            if(spotViewModel._spots.value!=null){
                spotsArayList.clear()
                spotViewModel._spots.value!!.forEach {
                    spotsArayList.add(it)
                }
            }
        })
        parkingViewModel._parkings.observe(viewLifecycleOwner, Observer{
            if(parkingViewModel._parkings.value!=null){
                parkingsArrayList.clear()
                parkingViewModel._parkings.value!!.forEach {
                    parkingsArrayList.add(it)
                }
            }
        })

        parkingAdapter = ParkingAdapter(parkingsArrayList,spotsArayList,parkingViewModel, context!!)
        parkingAdapter.setOnItemClickListener(object : OnItemClickListener{
            override fun onItemClick(position: Int) {
                Toast.makeText(context,"Kliknuo na stavku $position",Toast.LENGTH_SHORT).show()
//                val bundle: Bundle = Bundle()
//                bundle.putString("originLatLng",origin)
//                bundle.putString("destinationLatLng",destination)
//                bundle.putString("parkingID",pid)
//                setFragmentResult("Lokacije",bundle)
//
//                val navController = findNavController()
//                navController.navigate(R.id.action_fragment_locations_to_fragment_parking)
            }
        })
        recyclerView.adapter = parkingAdapter
        return view
    }


}
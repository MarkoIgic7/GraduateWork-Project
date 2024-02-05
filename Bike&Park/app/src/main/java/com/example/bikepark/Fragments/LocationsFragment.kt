package com.example.bikepark.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bikepark.Data.Parking
import com.example.bikepark.Data.Spot
import com.example.bikepark.ParkingAdapter
import com.example.bikepark.R
import com.example.bikepark.ViewModel.ParkingViewModel
import com.example.bikepark.ViewModel.SpotViewModel


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
        recyclerView.adapter = parkingAdapter
        return view
    }


}
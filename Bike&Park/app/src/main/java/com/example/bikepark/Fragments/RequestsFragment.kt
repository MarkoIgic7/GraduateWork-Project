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
import com.example.bikepark.Data.Request
import com.example.bikepark.R
import com.example.bikepark.RequestAdapter
import com.example.bikepark.ViewModel.ParkingViewModel
import com.example.bikepark.ViewModel.RequestViewModel
import com.example.bikepark.ViewModel.SpotViewModel
import com.example.bikepark.ViewModel.UserViewModel


class RequestsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var requestAdapter: RequestAdapter

    private val parkingViewModel: ParkingViewModel by activityViewModels()
    private val spotViewModel: SpotViewModel by activityViewModels()
    private val requestViewModel: RequestViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    var requestsArrayList= ArrayList<Request?>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestViewModel.getAllRequestss()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_requests, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewRequests)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context!!)

        requestViewModel._requests.observe(viewLifecycleOwner, Observer {
            if(requestViewModel._requests.value != null){
                requestsArrayList.clear()
                requestViewModel._requests.value!!.forEach{
                    requestsArrayList.add(it)
                }
            }
        })

        requestAdapter = RequestAdapter(requestsArrayList,requestViewModel, context!!)
        recyclerView.adapter = requestAdapter
        return view
    }

}
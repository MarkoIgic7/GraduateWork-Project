package com.example.bikepark

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.bikepark.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var mMap: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
        val navController = navHostFragment.navController

        if(auth.currentUser!!.uid == "4i5ySP7t2kc5UsY5az5b6BgIM062"){
            //ADMIN
            //binding.bottomNavigationBarClient.menu.findItem(R.id.profile).isVisible = false
            binding.bottomNavigationBarClient.menu.findItem(R.id.mySpot).isVisible = false
        }else {
            //NOT ADMIN
            binding.bottomNavigationBarClient.menu.findItem(R.id.requests).isVisible = false
        }




        binding.bottomNavigationBarClient.setOnItemSelectedListener {it ->
            when(it.itemId){
                R.id.map -> {navController.navigate(R.id.fragment_map)}
                R.id.profile -> {navController.navigate(R.id.fragment_profile)}
                R.id.requests -> {navController.navigate(R.id.fragment_requests)}
                R.id.locations -> {navController.navigate(R.id.fragment_locations)}
                R.id.mySpot -> {navController.navigate(R.id.fragment_mySpot)}
                else -> {

                }
            }
            true
        }
        // za Google mape
        /*val mapFragment = supportFragmentManager.findFragmentById(R.id.fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)*/
    }

    /*override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        val defaultLocation = LatLng(43.3209,21.8958)
        val defaultZoom = 17.0f
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation,defaultZoom))
    }*/
}
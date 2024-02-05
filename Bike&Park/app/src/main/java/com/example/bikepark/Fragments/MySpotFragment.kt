package com.example.bikepark.Fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.bikepark.Data.Parking
import com.example.bikepark.Data.Spot
import com.example.bikepark.Data.User
import com.example.bikepark.R
import com.example.bikepark.Services.MyFirebaseMessagingService
import com.example.bikepark.ViewModel.ParkingViewModel
import com.example.bikepark.ViewModel.RequestViewModel
import com.example.bikepark.ViewModel.SpotViewModel
import com.example.bikepark.ViewModel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.integration.android.IntentIntegrator
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MySpotFragment : Fragment() {

    private val parkingViewModel: ParkingViewModel by activityViewModels()
    private val spotViewModel: SpotViewModel by activityViewModels()
    private val requestViewModel: RequestViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var auth: FirebaseAuth

    private lateinit var scanBtn: Button
    private lateinit var removeBtn: Button
    private lateinit var cont: ViewGroup

    private val handler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        spotViewModel.getAllSpots()
        parkingViewModel.getAllParkings()
        userViewModel.getAllUsers()
        auth = FirebaseAuth.getInstance()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_spot, container, false)

        activity!!.setTitle("Moje mesto za biciklu")
        scanBtn = view.findViewById(R.id.mySpotF_scanBtn)
        removeBtn = view.findViewById(R.id.mySpotF_remove)
        cont = view.findViewById(R.id.mySpotF_container)


        val mySpotContainer = view.findViewById<ConstraintLayout>(R.id.mySpotF_container)
        // Set the background color dynamically
        val newBackgroundColor = resources.getColor(R.color.grey)
        mySpotContainer.setBackgroundColor(newBackgroundColor)

        removeBtn.isEnabled = false
        val currentUser = userViewModel._users.value!!.find{it -> it.uid == auth.currentUser!!.uid}
        if(currentUser!!.mySpots.size > 1){
            val spotID = currentUser.mySpots[1]
            val spot = spotViewModel._spots.value!!.find { it-> it.sid==spotID }
            val parkingID = spot!!.parking
            loadMySpotLayout(parkingID,spotID,currentUser.uid!!)
        }

        scanBtn.setOnClickListener({
                    startQRCodeScanner()
        })
        return view
    }

    private fun showReleaseDialog(parkingID: String,spotID: String,userID: String) {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.dialog_release)
        val dialog = builder.create()
        dialog.show()
        dialog.setCancelable(true)
        val ratingBar: RatingBar = dialog.findViewById(R.id.releaseDialog_ratingBar)
        val noBtn: Button = dialog.findViewById(R.id.releaseDialog_noBtn)
        val yesBtn: Button = dialog.findViewById(R.id.releaseDialog_yesBtn)

        val parking = parkingViewModel._parkings.value!!.find { it-> it.pid == parkingID }
        val spot = spotViewModel._spots.value!!.find { it-> it.sid ==spotID }
        val user = userViewModel._users.value!!.find { it-> it.uid==userID }

        noBtn.setOnClickListener({
            dialog.dismiss()
        })

        yesBtn.setOnClickListener({
            var ratingValue:Float = ratingBar.rating
            if(ratingValue > 0){
                parking!!.currentNum = parking.currentNum!!-1
                parking.numOfUsings = parking.numOfUsings!!+1
                parking.sum = parking.sum!! + ratingValue.toInt()
                parking.rating = parking!!.sum!!.toDouble() / parking.numOfUsings!!
                parkingViewModel.updateParking(parking)
                spot!!.free = true
                spot.time = "Vreme"
                spot.user = "Korisnicko ime"
                spotViewModel.updateSpot(spot)
                user!!.mySpots.removeAt(1)
                userViewModel.updateUser(user)
                dialog.dismiss()
                loadInitialLayout()
            } else{
                Toast.makeText(context!!,"Morate dati ocenu ",Toast.LENGTH_SHORT).show()
            }
        })

    }



    // ova funkcija inicijalizuje skeniranje i postavljanje parametara
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startQRCodeScanner() {

        val currentUser = userViewModel._users.value!!.find { it -> it.uid == auth.currentUser!!.uid }
        if (currentUser?.banTime == "noBan") {
            IntentIntegrator.forSupportFragment(this).apply {
                setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                setBeepEnabled(true)
                setPrompt("Skenirajte QR kod sa odabranog stajalista")
                setOrientationLocked(true)
                initiateScan()
            }
        } else {
            val currentTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            val banTime = LocalDateTime.parse(currentUser?.banTime, formatter)
            if (currentTime.isAfter(banTime)) {
                // Moze da koristi & updatuje banTime na noBan
                currentUser?.banTime = "noBan"
                userViewModel.updateUser(currentUser!!)
                IntentIntegrator.forSupportFragment(this).apply {
                    setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                    setBeepEnabled(true)
                    setPrompt("Skenirajte QR kod sa odabranog stajalista")
                    setOrientationLocked(false)
                    initiateScan()
                }
            } else {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Nepropisno koriscenje aplikacije")
                builder.setMessage("Niste napustili parking mesto u predvidjeno vreme zbog cega Vam je onemoguceno skeniranje QR koda do : " + currentUser?.banTime)
                builder.show()
            }
        }


    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val scannedData = result.contents
                handleScannedData(scannedData)
            } else {
                // Handle case when the user canceled the scan
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleScannedData(scannedData: String) {
        // Display the scanned data or use it as needed
        //Toast.makeText(context!!,scannedData,Toast.LENGTH_SHORT).show()
        val spot = spotViewModel._spots.value!!.find { s -> s.sid == scannedData }
        if(spot!!.free == true){
            loadAvailableLayout()
            cont.setBackgroundColor(resources.getColor(R.color.enough_requests))
            val statusLabel = cont.findViewById<TextView>(R.id.mySpotF_container_statusLabel)
            statusLabel.text = "SLOBODNO"
            var parking = parkingViewModel._parkings.value!!.find { parking -> parking.pid ==spot.parking }
            val address = cont.findViewById<TextView>(R.id.mySpotF_container_address)
            val addressFirstPart =  parking!!.address!!.substringBefore(", Serbia")
            val addressFinalPart = addressFirstPart.substringAfter("Adresa : ")
            address.text = addressFinalPart
            val rating = cont.findViewById<RatingBar>(R.id.mySpotF_container_ratingBar)
            rating.rating = parking.rating!!.toFloat()
            val spinner: Spinner = cont.findViewById(R.id.mySpot_container_spinner)
            val spinnerData = listOf<String>("1h (60min)","2h (120min)","4h (240min)","8h (480min)")
            val adapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,spinnerData)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            var parkingTime = 0
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedOption = spinnerData[position]
                    parkingTime = selectedOption.get(0).toInt()
                    // Handle the selected option
                    Toast.makeText(requireContext(), "Selected: $selectedOption", Toast.LENGTH_SHORT).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Handle the case when nothing is selected
                    Toast.makeText(requireContext(), "Moras selektoati", Toast.LENGTH_SHORT).show()
                }
            }
            val zauzmiBtn: Button = cont.findViewById(R.id.mySpotF_container_zauzmiBtn)
            zauzmiBtn.setOnClickListener({
                // sacuvaj sledece
                // PARKING(currentNum,numOfUsings)
                parking.currentNum = parking.currentNum!! + 1
                //parking.numOfUsings = parking.numOfUsings!! + 1
                parkingViewModel.updateParking(parking)
                // USER (mySpots)
                val user = userViewModel._users.value!!.find { u -> u.uid==auth.currentUser!!.uid }
                user!!.mySpots.add(spot.sid)
                userViewModel.updateUser(user)
                // SPOT (free,time,user)
                spot.free = false
                spot.user = user.uid.toString()
                val currentTime = LocalDateTime.now()
                val spotTime = currentTime.plusHours(parkingTime.toLong())
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                val formatedDateTime = spotTime.format(formatter)
                spot.time = formatedDateTime
                spotViewModel.updateSpot(spot)
                scheduleNotification(120*1000,spot)//parkingTime.toLong()*60*60*1000 - 15*60*1000
                scheduleEmptySpot(300*1000,parking,spot,user)//parkingTIme.toLong()*60*60*1000
                loadMySpotLayout(parking.pid!!,spot.sid,user.uid!!)


            })

            //slobodno je
        } else {
            //zauzeto je
            loadBusyLayout(spot.sid)

        }

    }
    private fun loadAvailableLayout() {
        // Inflate the custom layout XML
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customLayout = inflater.inflate(R.layout.myspotf_available, null)

        // Add the custom layout to the 'cont' view

        cont.removeAllViews()
        cont.addView(customLayout)
        scanBtn.isEnabled = true
        removeBtn.isEnabled = false

    }
    private fun loadBusyLayout(spotID: String) {
        // Inflate the custom layout XML
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customLayout = inflater.inflate(R.layout.myspotf_initial, null)

        // Add the custom layout to the 'cont' view
        cont.removeAllViews()
        cont.addView(customLayout)
        removeBtn.isEnabled = false
        scanBtn.isEnabled = true
        fillBusyLayout(spotID)
    }
    private fun loadInitialLayout() {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customLayout = inflater.inflate(R.layout.myspotf_initial, null)

        // Add the custom layout to the 'cont' view
        cont.removeAllViews()
        cont.addView(customLayout)
        removeBtn.isEnabled = false
        scanBtn.isEnabled = true
        scanBtn.isVisible = true
        cont.setBackgroundColor(resources.getColor(R.color.grey))

    }
    private fun loadMySpotLayout(parkingID: String, spotID: String,userID :String) {
        // Inflate the custom layout XML
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customLayout = inflater.inflate(R.layout.myspotf_initial, null)

        // Add the custom layout to the 'cont' view
        cont.removeAllViews()
        cont.addView(customLayout)
        removeBtn.isEnabled = true
        scanBtn.isEnabled = false
        scanBtn.isVisible = false
        fillMySpotLayout()
        removeBtn.setOnClickListener({
            showReleaseDialog(parkingID,spotID,userID)
        })
    }

    private fun fillMySpotLayout() {
        val currentUser = userViewModel._users.value!!.find{it -> it.uid == auth.currentUser!!.uid}
        val spot = spotViewModel._spots.value!!.find { s -> s.sid == currentUser!!.mySpots[1] }
        cont.setBackgroundColor(resources.getColor(R.color.in_proceess_requests))

        val statusLabel = cont.findViewById<TextView>(R.id.mySpotF_container_statusLabel)
        statusLabel.text = "MOJE MESTO"

        val parking = parkingViewModel._parkings.value!!.find { parking -> parking.pid ==spot!!.parking }

        val address = cont.findViewById<TextView>(R.id.mySpotF_container_address)
        val addressFirstPart =  parking!!.address!!.substringBefore(", Serbia")
        val addressFinalPart = addressFirstPart.substringAfter("Adresa : ")
        address.text = addressFinalPart

        val rating = cont.findViewById<RatingBar>(R.id.mySpotF_container_ratingBar)
        rating.rating = parking.rating!!.toFloat()

        val user = cont.findViewById<TextView>(R.id.mySpotF_container_user)
        val userAcc = userViewModel._users.value!!.find { it -> it.uid == spot!!.user }
        user.text = userAcc!!.email

        val time = cont.findViewById<TextView>(R.id.mySpotF_container_time)
        time.text = spot!!.time
    }
    private fun fillBusyLayout(spotID: String){

        val spot = spotViewModel._spots.value!!.find { s -> s.sid == spotID }
        val currentUser = userViewModel._users.value!!.find{it -> it.uid == spot!!.user}
        cont.setBackgroundColor(resources.getColor(R.color.not_enough_requests))

        val statusLabel = cont.findViewById<TextView>(R.id.mySpotF_container_statusLabel)
        statusLabel.text = "ZAUZETO"

        val parking = parkingViewModel._parkings.value!!.find { parking -> parking.pid ==spot!!.parking }

        val address = cont.findViewById<TextView>(R.id.mySpotF_container_address)
        val addressFirstPart =  parking!!.address!!.substringBefore(", Serbia")
        val addressFinalPart = addressFirstPart.substringAfter("Adresa : ")
        address.text = addressFinalPart

        val rating = cont.findViewById<RatingBar>(R.id.mySpotF_container_ratingBar)
        rating.rating = parking.rating!!.toFloat()

        val user = cont.findViewById<TextView>(R.id.mySpotF_container_user)
        val userAcc = userViewModel._users.value!!.find { it -> it.uid == spot!!.user }
        user.text = userAcc!!.email

        val time = cont.findViewById<TextView>(R.id.mySpotF_container_time)
        time.text = spot!!.time
    }

    private fun scheduleNotification(delayMillis: Long,spot: Spot) {
        handler.postDelayed({
            // Call the function to show the notification here
            val latestSpot = spotViewModel._spots.value!!.find { it ->it.sid ==spot.sid }
            if(!latestSpot!!.free){
                showDelayedNotification("Bike & Park","Parking mesto Vase bicikle istice za 15 minuta")
            }
        }, delayMillis)
    }
    private fun scheduleEmptySpot(delayMillis: Long,parking: Parking,spot: Spot,user: User){
        handler.postDelayed({
            // Call the function to show the notification here
            //latestSpot pribavljam isti taj objekat zato sto u medjuvremenu moze da mu se promene atributi zbog funkcionalnosti "Oslobodi mesto"
            val latestSpot = spotViewModel._spots.value!!.find { it ->it.sid ==spot.sid }
            if(!latestSpot!!.free){
                emptySpot(parking,spot,user)
                showDelayedNotification("Parking Vam je istekao","Niste oslobodili biciklu, Vasa bicikla vise nije zakljucana")
            }
        }, delayMillis)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun emptySpot(parking: Parking, spot: Spot, user: User) {
        parking.currentNum = parking.currentNum!! - 1
        parking.numOfUsings = parking.numOfUsings!! + 1
        parking.sum = parking.sum!! + 5
        parking.rating = parking!!.sum!!.toDouble() / parking.numOfUsings!!
        parkingViewModel.updateParking(parking)
        spot!!.free = true
        spot.time = "Vreme"
        spot.user = "Korisnicko ime"
        spotViewModel.updateSpot(spot)
        user!!.mySpots.removeAt(1)
        val banTime = LocalDateTime.now().plusSeconds(50)
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        val formatedDateTime = banTime.format(formatter)
        user.banTime = formatedDateTime
        userViewModel.updateUser(user)
        loadInitialLayout()
    }

    private fun showDelayedNotification(title: String, message: String) {
        // Call the function to generate and show the notification
        MyFirebaseMessagingService.generateNotification(requireContext(), title, message)
    }




}
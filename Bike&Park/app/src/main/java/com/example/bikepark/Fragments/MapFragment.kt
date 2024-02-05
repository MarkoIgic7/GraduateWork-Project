package com.example.bikepark.Fragments

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.bikepark.Data.Parking
import com.example.bikepark.Data.Request
import com.example.bikepark.Data.Spot
import com.example.bikepark.R
import com.example.bikepark.RegisterActivity
import com.example.bikepark.ViewModel.ParkingViewModel
import com.example.bikepark.ViewModel.RequestViewModel
import com.example.bikepark.ViewModel.SpotViewModel
import com.example.bikepark.ViewModel.UserViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class MapFragment : Fragment(), OnMapReadyCallback {

    private val parkingViewModel: ParkingViewModel by activityViewModels()
    private val spotViewModel: SpotViewModel by activityViewModels()
    private val requestViewModel: RequestViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()


    private lateinit var storageReference: FirebaseStorage


    private lateinit var googleMap: GoogleMap

    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback


    private var locationPermissionGranted: Boolean = false
    var lastKnownLocation: Location? = null

    private var longitude: Double = 43.3209
    private var latitude: Double = 21.8958

    private lateinit var geocoder: Geocoder

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.to_bottom_anim) }


    private lateinit var fabSearch: FloatingActionButton
    private lateinit var fabFilter: FloatingActionButton
    private lateinit var fabRadius: FloatingActionButton
    private lateinit var fabShowHide: FloatingActionButton

    private var clicked: Boolean = false

    private lateinit var navController: NavController

    lateinit var parkingPhotoPath: String
    private lateinit var parkingPicUri: Uri
    private lateinit var parkingImgView: ImageView
    private lateinit var parkingMaxNum: TextView
    private lateinit var parkingAddress: TextView

    private lateinit var progressDialog: ProgressDialog
    private lateinit var addParkingDialog: AlertDialog


    private var parkingSwitch:Boolean = true
    private var requestSwitch: Boolean = true



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = getContext()!!.getSystemService(LOCATION_SERVICE) as LocationManager
        setHasOptionsMenu(true)
        geocoder = Geocoder(context!!, Locale.getDefault())
        parkingViewModel.getAllParkings()
        spotViewModel.getAllSpots()
        requestViewModel.getAllRequestss()
        userViewModel.getAllUsers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        fabFilter = view.findViewById(R.id.floatingActionButton_filter)
        fabRadius = view.findViewById(R.id.floatingActionButton_radius)
        fabSearch = view.findViewById(R.id.floatingActionButton_search)
        fabShowHide = view.findViewById(R.id.floatingActionButton_showHide)
        storageReference = FirebaseStorage.getInstance()
        activity!!.setTitle("Mapa parkinga")


        return view
    }
    private fun addMarkerWithCustomIconUri(context: Context, googleMap: GoogleMap, latLng: LatLng, iconUriString: String,pid: String) {
        val iconUri = Uri.parse(iconUriString)

        Glide.with(context)
            .asBitmap()
            .load(iconUri)
            .apply(RequestOptions().override(125,125).transform(CircleCrop()))
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // Convert Bitmap to BitmapDescriptor
                    val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(resource)
                    // Add marker to the map with the custom icon
                    val marker = googleMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .icon(bitmapDescriptor)
                            .alpha(0.99F)

                        // Add other marker options as needed
                    )
                    marker.tag = "Parking/"+pid
                    /*googleMap.setOnMarkerClickListener { clickedMarker ->
                        if (clickedMarker == marker) {
                            // Handle marker click event
                            Toast.makeText(context, "Kliknuo", Toast.LENGTH_SHORT).show()
                            true
                        } else {
                            // Return false to allow the default behavior for other markers
                            false
                        }
                    }*/

                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle case where the image loading was canceled
                }
            })
    }
    private fun drawAllParkings(spots: ArrayList<Parking>) {
        spots.forEach {
            val pinLatLng: LatLng = LatLng(it.latitude!!.toDouble(),it.longitude!!.toDouble())
            val pin: MarkerOptions = MarkerOptions().position(pinLatLng)
            pin.title(it.address)
            val pinUri: Uri = Uri.parse(it.img)
            //addCustomMarker(context!!, googleMap, pinLatLng,Uri.parse(it.img))
            addMarkerWithCustomIconUri(context!!,googleMap,pinLatLng,it.img!!,it.pid!!)
        }
    }
    private fun drawAllRequests(requests: ArrayList<Request>) {
        requests.forEach {
            val pinLatLng: LatLng = LatLng(it.latitude!!.toDouble(),it.longitude!!.toDouble())
            val pin: MarkerOptions = MarkerOptions().position(pinLatLng)
            pin.title(it.address)
            pin.snippet(it.number.toString()+"/"+it.maxNumber.toString())
            //addCustomMarker(context!!, googleMap, pinLatLng,Uri.parse(it.img))
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo_request)
            val resizedBitmap = resizeBitmap(bitmap,125,125)
            val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(resizedBitmap)
            val markerOptions = MarkerOptions()
                .position(pinLatLng) // Set the marker position
                .icon(bitmapDescriptor) // Set the marker icon
                .alpha(1.0F)
            val markerRequest = googleMap.addMarker(markerOptions)
            markerRequest.tag = "Request/"+it.rid



            //addMarkerWithCustomIconImage(context!!,googleMap,pinLatLng,it.img!!)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        mapFragment.getMapAsync(this)
        checkLocationStatus() // proveravanje Lokacije iz Settings-a
        navController = findNavController()

        ActivityCompat.requestPermissions(activity!!, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_REQUEST_CODE)


        fabSearch.setOnClickListener({
            onSearchButtonClicked()
        })
        fabRadius.setOnClickListener({
            showRadiusFilterDialog()
        })
        fabFilter.setOnClickListener({
            showRatingFilterDialog()
        })
        fabShowHide.setOnClickListener({
            showShowAndHideDialog()
        })

    }
    private fun showRadiusFilterDialog()
    {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.dialog_radius)
        val dialog = builder.create()
        dialog.show()

        val metersEditText: EditText = dialog.findViewById(R.id.radiusDialog_distance)
        val btn: Button = dialog.findViewById(R.id.radiusDialog_primeniBtn)
        btn.setOnClickListener({
            if(!metersEditText.text.isEmpty()){
                val distance = metersEditText.text.toString().toFloat()
                val result = floatArrayOf(1F)
                var listaParking = ArrayList<Parking>()
                parkingViewModel._parkingsObject.forEach { it->
                    Location.distanceBetween(this.latitude,this.longitude, it.latitude!!.toDouble(),it.longitude!!.toDouble(),result)
                    if(result[0]<distance){
                        listaParking.add(it)
                    }
                }
                googleMap.clear()
                drawAllParkings(listaParking)
                dialog.dismiss()

                //Toast.makeText(context!!,distance.toString(),Toast.LENGTH_SHORT).show()

            } else{
                Toast.makeText(context!!,"Morate uneti radius",Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun showRatingFilterDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.dialog_rating)
        val dialog = builder.create()
        dialog.show()

        val ratingBar: RatingBar = dialog.findViewById(R.id.ratingDialog_ratingBar)
        val btn: Button = dialog.findViewById(R.id.ratingDialog_primeniBtn)
        var listaParking: ArrayList<Parking> = ArrayList<Parking>()
        btn.setOnClickListener({
            val ratingValue = ratingBar.rating
            if(ratingValue>0){
                //Toast.makeText(context!!,ratingValue.toString(),Toast.LENGTH_SHORT).show()
                parkingViewModel._parkingsObject.forEach { it->
                    if(it.rating!!.toFloat()>ratingValue.toFloat()){
                        listaParking.add(it)
                    }
                }
                googleMap.clear()
                drawAllParkings(listaParking)
                dialog.dismiss()


            } else{
                Toast.makeText(context!!,"Morate izabrati ocenu",Toast.LENGTH_SHORT).show()
            }
        })
    }


    //FAB
    private fun onSearchButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        isClickable(clicked)
        clicked = !clicked
    }
    private fun setAnimation(clicked: Boolean) {
        if(!clicked){
            fabShowHide.startAnimation(fromBottom)
            fabFilter.startAnimation(fromBottom)
            fabRadius.startAnimation(fromBottom)
            fabSearch.startAnimation(rotateOpen)
        } else{
            fabShowHide.startAnimation(toBottom)
            fabFilter.startAnimation(toBottom)
            fabRadius.startAnimation(toBottom)
            fabSearch.startAnimation(rotateClose)
        }
    }
    private fun setVisibility(clicked: Boolean) {
        if(!clicked){
            fabShowHide.visibility = View.VISIBLE
            fabFilter.visibility = View.VISIBLE
            fabRadius.visibility = View.VISIBLE
        } else{
            fabShowHide.visibility = View.INVISIBLE
            fabFilter.visibility = View.INVISIBLE
            fabRadius.visibility = View.INVISIBLE
        }
    }
    private fun isClickable(clicked: Boolean){
        if(!clicked){
            fabShowHide.isClickable = true
            fabFilter.isClickable = true
            fabRadius.isClickable = true
        } else{
            fabShowHide.isClickable = false
            fabFilter.isClickable = false
            fabRadius.isClickable = false
        }
    }

    private fun getMyLocation() {
        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
            override fun isCancellationRequested() = false
        }).addOnSuccessListener { location: Location? ->
            if (location == null)
                Toast.makeText(context, "Cannot get location.", Toast.LENGTH_SHORT).show()
            else {
                googleMap.isMyLocationEnabled = true
                this.latitude = location.latitude
                this.longitude = location.longitude
                //Toast.makeText(context, this.latitude.toString()+"/"+this.longitude.toString(), Toast.LENGTH_SHORT).show()
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(this.latitude,this.longitude),15f))
            }

        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get and show current location
                getMyLocation()
            } else {
                // Permission denied
                Toast.makeText(context,"Morate omoguciti koriscenje lokacije za ovu aplikaciju",Toast.LENGTH_SHORT).show()
            }
        }
        if(requestCode == NOTIFICATION_REQUEST_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get and show current location
                Toast.makeText(context,"Omogucena notifikacija",Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                Toast.makeText(context,"Morate omoguciti koriscenje lokacije za ovu aplikaciju",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getMyLocation()
    }
    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        getMyLocation()
        parkingViewModel._parkings.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(it != null){
                drawAllParkings(it)
            }
        })
        requestViewModel._requests.observe(viewLifecycleOwner,androidx.lifecycle.Observer {
            if(it != null){
                drawAllRequests(it)
            }
        })
        googleMap.setOnMarkerClickListener { marker ->
            onMarkerClick(marker)
            true
        }
        /*googleMap.setOnMarkerClickListener { marker ->
            if(marker != null && marker.alpha==1.0F){
                Toast.makeText(context!!,"REQUEST",Toast.LENGTH_SHORT).show()
            } else if( marker!= null && marker.alpha==0.99F){
                Toast.makeText(context,"PARKING",Toast.LENGTH_SHORT).show()
            }
            false
        }*/
    }

    private fun onMarkerClick(marker: Marker) {
        val type:String  = marker.tag.toString().split("/")[0].trim()
        val id: String = marker.tag.toString().split("/")[1].trim()
        when (type) {
            "Request" -> {
                showRequestDialog(id)
            }
            "Parking" -> {
                val parking = parkingViewModel._parkings.value!!.find { it.pid == id }
                val origin = this.latitude.toString()+","+this.longitude.toString()
                val destination = parking!!.latitude+","+parking.longitude

                val bundle: Bundle = Bundle()
                bundle.putString("originLatLng",origin)
                bundle.putString("destinationLatLng",destination)
                bundle.putString("parkingID",parking.pid)
                setFragmentResult("Lokacije",bundle)

//                val fragmentTransaction = parentFragmentManager.beginTransaction()
//                fragmentTransaction.replace(R.id.fragment_container_view,ParkingFragment())
//                //fragmentTransaction.addToBackStack("Dodaj")
//                fragmentTransaction.commit()
                val navController = findNavController()
                navController.navigate(R.id.action_fragment_map_to_fragment_parking)

            }

        }

    }


    private fun showRequestDialog(id: String) {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.dialog_request)
        val dialog = builder.create()
        dialog.show()
        val btnNo: Button = dialog.findViewById(R.id.dialogRequest_No)
        val btnYes: Button = dialog.findViewById(R.id.dialogRequest_Yes)
        val current: TextView = dialog.findViewById(R.id.dialogRequest_current)
        val status: TextView = dialog.findViewById(R.id.dialogRequest_status)
        var user = userViewModel._users.value!!.find {it.uid == FirebaseAuth.getInstance().currentUser!!.uid}
        var request = requestViewModel._requests.value!!.find {it.rid == id}
        current.text = request!!.number.toString()+"/"+request!!.maxNumber.toString()
        status.text = "Status : " + request.status
        if(user!!.myRequests.contains(request!!.rid)){
            btnYes.isEnabled = false
        }
        btnNo.setOnClickListener({
            dialog.dismiss()
        })
        btnYes.setOnClickListener({
            user.myRequests.add(id)
            userViewModel.updateUser(user)
            request.number = request.number!! + 1
            requestViewModel.updateRequest(request)
            btnYes.isEnabled = false
            current.text = request!!.number.toString()+"/"+request!!.maxNumber.toString()
            dialog.dismiss()

        })
    }

    //LOCATION ENABLING
    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private fun requestLocationEnabled() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }
    private fun checkLocationStatus() {
        if (!isLocationEnabled()) {
            Toast.makeText(context,"Ova aplikacija zahteva koriscenje lokacije",Toast.LENGTH_SHORT).show()
            requestLocationEnabled()
        }
    }

    //MENIII
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_nav,menu)
        if(FirebaseAuth.getInstance().currentUser!!.uid == "4i5ySP7t2kc5UsY5az5b6BgIM062"){
            //ADMIN
            menu.findItem(R.id.nav_top_AddRequest).isVisible = false
        }else {
            //NOT ADMIN
            menu.findItem(R.id.nav_top_AddParking).isVisible = false
        }
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_top_AddParking -> {
                showAddParkingDialog()
            }
            R.id.nav_top_AddRequest -> {
                showAddRequestDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showShowAndHideDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.dialog_show_and_hide)
        val dialog = builder.create()
        dialog.show()

        val switchParking = dialog.findViewById<Switch>(R.id.showAndHideDialog_switchParkings)
        val switchRequest = dialog.findViewById<Switch>(R.id.showAndHideDialog_switchRequests)
        val btn = dialog.findViewById<Button>(R.id.showAndHideDialog_primeniBtn)

        switchParking.isChecked = this.parkingSwitch
        switchRequest.isChecked = this.requestSwitch

        btn.setOnClickListener({
            if(switchParking.isChecked==true && switchRequest.isChecked==true){
                //Toast.makeText(context!!,"Parking = ${switchParking.isChecked}, Zahtev = ${switchRequest.isChecked}",Toast.LENGTH_SHORT).show()
                this.parkingSwitch = switchParking.isChecked
                this.requestSwitch = switchRequest.isChecked
                googleMap.clear()
                drawAllParkings(parkingViewModel._parkingsObject)
                drawAllRequests(requestViewModel._requestsObject)
            } else if (switchParking.isChecked==true && switchRequest.isChecked==false){
                //Toast.makeText(context!!,"Parking = ${switchParking.isChecked}, Zahtev = ${switchRequest.isChecked}",Toast.LENGTH_SHORT).show()
                this.parkingSwitch = switchParking.isChecked
                this.requestSwitch = switchRequest.isChecked
                googleMap.clear()
                drawAllParkings(parkingViewModel._parkingsObject)
            } else if(switchParking.isChecked==false && switchRequest.isChecked==false){
                //Toast.makeText(context!!,"Parking = ${switchParking.isChecked}, Zahtev = ${switchRequest.isChecked}",Toast.LENGTH_SHORT).show()
                this.parkingSwitch = switchParking.isChecked
                this.requestSwitch = switchRequest.isChecked
                googleMap.clear()
            } else {
                //Toast.makeText(context!!,"Parking = ${switchParking.isChecked}, Zahtev = ${switchRequest.isChecked}",Toast.LENGTH_SHORT).show()
                this.parkingSwitch = switchParking.isChecked
                this.requestSwitch = switchRequest.isChecked
                googleMap.clear()
                drawAllRequests(requestViewModel._requestsObject)
            }
            dialog.dismiss()
        })
    }

    private fun showAddRequestDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.dialog_add_request)
        val dialog = builder.create()
        dialog.show()
        val dialogAddRequestAddress: TextView = dialog.findViewById(R.id.dialogAddRequest_address)
        val dialogAddRequestCancel: Button = dialog.findViewById(R.id.dialogAddRequest_Cancel)
        val dialogAddRequestAdd: Button = dialog.findViewById(R.id.dialogAddRequest_Add)


        val address = geocoder.getFromLocation(this.latitude,this.longitude,1)
        dialogAddRequestAddress.text = address!![0].getAddressLine(0)

        dialogAddRequestCancel.setOnClickListener({
            dialog.dismiss()
        })
        dialogAddRequestAdd.setOnClickListener({
            val r: Request = Request(UUID.randomUUID().toString(),this.latitude.toString(),this.longitude.toString(),dialogAddRequestAddress.text.toString(),1,5,"Prikupljanje zahteva")
            requestViewModel.addRequest(r)
            var user  = userViewModel._users.value!!.find { it.uid == FirebaseAuth.getInstance().currentUser!!.uid }
            user!!.myRequests.add(r.rid!!)
            userViewModel.updateUser(user)
            dialog.dismiss()
            googleMap.clear()
            parkingViewModel._parkings.value?.let { it1 -> drawAllParkings(it1) }
            requestViewModel._requests.value?.let { it1 -> drawAllRequests(it1) }
        })

    }
    private fun showAddParkingDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.dialog_add_parking)
        val dialog = builder.create()
        dialog.show()
        addParkingDialog = dialog
        val dialogImg: ImageView = dialog.findViewById(R.id.dialogAddParking_addImage)
        parkingImgView = dialogImg
        val dialogMaxNum = dialog.findViewById<TextView>(R.id.dialogAddParking_maxNum)
        parkingMaxNum = dialogMaxNum
        val dialogCnc: Button = dialog.findViewById<Button>(R.id.dialogAddParking_cancel)
        val dialogAdd: Button = dialog.findViewById<Button>(R.id.dialogAddParking_add)
        val dialogLat: TextView = dialog.findViewById<TextView>(R.id.dialogAddParking_latitude)
        val dialogLong: TextView = dialog.findViewById<TextView>(R.id.dialogAddParking_longitude)
        val dialogAddress: TextView = dialog.findViewById(R.id.dialogAddParking_address)
        parkingAddress = dialogAddress
        dialogLat.text = "Latitude : " + this.latitude.toString()
        dialogLong.text = "Longitude : " + this.longitude.toString()
        val address = geocoder.getFromLocation(this.latitude,this.longitude,1)

        dialogAddress.text = "Adresa : " + address!![0].getAddressLine(0)
        dialogCnc.setOnClickListener({
            dialog.dismiss()
        })
        dialogAdd.setOnClickListener({
            if(!dialogMaxNum.text.isEmpty() && dialogImg.drawable!= null){
                progressDialog = ProgressDialog(context)
                progressDialog.setMessage("Dodavanje stajalista")
                progressDialog.setCancelable(false)
                progressDialog.show()
                uploadParking()
            }
            else{
                Toast.makeText(context,"Dodajte sliku i popunite polje",Toast.LENGTH_SHORT).show()
            }
        })
        dialogImg.setOnClickListener({
            dispatchTakePictureIntent()
        })
    }
    private fun uploadParking() {
        var profilePicPath: String = UUID.randomUUID().toString() //+ "URI-"+ profilePicUri.toString().replace("/","")
        storageReference.reference.child("Parkings/"+profilePicPath).putFile(parkingPicUri).addOnCompleteListener{
            if(it.isSuccessful){
                FirebaseStorage.getInstance().reference.child("Parkings/"+profilePicPath).downloadUrl.addOnCompleteListener {

                    val imgName = it.result.toString()

                    val maxNumValue = parkingMaxNum.text.toString().toInt()
                    val parking = Parking(profilePicPath,parkingMaxNum.text.toString().toInt(),0,this.latitude.toString(),this.longitude.toString(),parkingAddress.text.toString(),imgName,0.0,0,0)

                    parkingViewModel.addParking(parking)
                    //Dodavanje svakog mesta pojedinacno
                    var i =0
                    while(i<parkingMaxNum.text.toString().toInt()){
                        val spotID = UUID.randomUUID().toString()
                        val qrCodeText = spotID
                        val qrCodeBitmap = generateQRCode(qrCodeText,300,300)
                        qrCodeBitmap?.let { it->
                            uploadQRCodeToFirebaseStorage(it,spotID)
                        }
                        val spot = Spot(spotID,"Korisnicko ime",profilePicPath,true,"Vreme",spotID)
                        spotViewModel.addSpot(spot)
                        i+=1
                    }
                    progressDialog.dismiss()
                    Toast.makeText(context,"Dodat parking",Toast.LENGTH_LONG).show()
                    addParkingDialog.dismiss()
                    googleMap.clear()
                    parkingViewModel._parkings.value?.let { it1 -> drawAllParkings(it1) }
                    requestViewModel._requests.value?.let {it2 -> drawAllRequests(it2)}
                    //parentFragmentManager.popBackStack()

                }
            }
        }
    }
    //PRAVLJENJE SLIKE
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(context!!.packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        context!!,
                        "com.example.android.bikepark.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, RegisterActivity.CAMERA_PICK)
                }
            }
        }
    }
    private fun createImageFile(): File {
        val storageDir: File? = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("bikeParkParkingPic",".jpg",storageDir).apply {
            parkingPhotoPath = absolutePath
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //Toast.makeText(context, "on activity result", Toast.LENGTH_SHORT).show()
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RegisterActivity.CAMERA_PICK && resultCode == AppCompatActivity.RESULT_OK) {
            var f: File = File(parkingPhotoPath)
            parkingImgView.setImageURI(Uri.fromFile(f))
            parkingImgView.background = null
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                val f = File(parkingPhotoPath)
                mediaScanIntent.data = Uri.fromFile(f)
                context!!.sendBroadcast(mediaScanIntent) // da obavesti sistem da je novi fajl kreiran
            }
            parkingPicUri = Uri.fromFile(f)
        }
    }

    fun resizeBitmap(originalBitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, false)
    }

    companion object {
        val LOCATION_PERMISSION_REQUEST_CODE = 1
        val NOTIFICATION_REQUEST_CODE = 2
    }
    fun generateQRCode(text: String, width: Int, height: Int): Bitmap? {
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
            val barcodeEncoder = com.journeyapps.barcodescanner.BarcodeEncoder()
            return barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        return null
    }
    fun uploadQRCodeToFirebaseStorage(bitmap: Bitmap,spotID: String) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val qrCodeRef = storageRef.child("QR_Codes/${spotID}.jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = qrCodeRef.putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Handle successful upload
            val downloadUrl = taskSnapshot.storage.downloadUrl
            // Save downloadUrl or do anything else you need
        }.addOnFailureListener { exception ->
            // Handle failed upload
            exception.printStackTrace()
        }
    }


}



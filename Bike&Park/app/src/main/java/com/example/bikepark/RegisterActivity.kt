package com.example.bikepark

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.example.bikepark.Data.User
import com.example.bikepark.ViewModel.UserViewModel
import com.example.bikepark.databinding.ActivityMainBinding
import com.example.bikepark.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.IOException
import androidx.activity.viewModels
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storageReference: StorageReference

    private val userViewModel: UserViewModel by viewModels()


    private lateinit var progressDialog: ProgressDialog
    lateinit var currentPhotoPath: String
    private lateinit var profilePicUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        storageReference = FirebaseStorage.getInstance().reference

        binding.textViewRegisterRedirectLogin.setOnClickListener {
            val redirectIntent: Intent = Intent(this, LoginActivity::class.java)
            startActivity(redirectIntent)
            finish()
        }

        binding.imageViewRegisterProfilePicCamera.setOnClickListener ({
            showImageSourceDialog()
//            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//                dispatchTakePictureIntent()
//            } else {
//                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 107)
//            }
        })
        binding.buttonRegisterSignup.setOnClickListener({
            performRegister()
        })
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            107 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent()
                } else {
                    Toast.makeText(this,"Bicete uskraceni mogucnosti za dodavanje novih Parking stajalista",Toast.LENGTH_SHORT).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    private fun choosePhotoFromGallery()  {
        val galleryIntent: Intent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, GALLERY_PICK)
    }

    private fun performRegister() {
        val email: EditText = binding.editTextRegisterUsername//findViewById(R.id.editTextRegister_username)
        val pass: EditText = binding.editTextRegisterPassword//findViewById(R.id.editTextRegister_password)
        val pass2: EditText = binding.editTextRegisterPassword2//findViewById(R.id.editTextRegister_password2)
        val name: EditText = binding.editTextRegisterName//findViewById(R.id.editTextRegister_name)
        val surname: EditText = binding.editTextRegisterSurname//findViewById(R.id.editTextRegister_surname)
        val number: EditText = binding.editTextRegisterNumber//findViewById(R.id.editTextRegister_number)

        if(!email.text.isEmpty() && !pass.text.isEmpty() && !pass2.text.isEmpty() && !name.text.isEmpty() && !surname.text.isEmpty()
            && !number.text.isEmpty() && null != binding.imageViewRegisterProfilePic.drawable)
        {
            if(pass.text.toString() == pass2.text.toString())
            {
                val emailInput = email.text.toString()
                val passInput = pass.text.toString()

                auth.createUserWithEmailAndPassword(emailInput, passInput)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            uploadImage()
                            progressDialog = ProgressDialog(this)
                            progressDialog.setMessage("Kreiranje korisnika")
                            progressDialog.setCancelable(false)
                            progressDialog.show()
                        } else {
                            Toast.makeText(baseContext, "Greska prilikom kreiranja naloga", Toast.LENGTH_SHORT,).show()
                        }
                    }
            }
            else
            {
                Toast.makeText(this,"Passwordi nisu istu",Toast.LENGTH_SHORT).show()
            }
        }
        else
        {
            Toast.makeText(baseContext,"Morate pupniti sva polja",Toast.LENGTH_SHORT).show()
        }
    }
    private fun uploadImage() {
        var profilePicPath: String = auth.currentUser?.uid.toString() //+ "URI-"+ profilePicUri.toString().replace("/","")
        storageReference.child("Users/"+profilePicPath).putFile(profilePicUri).addOnCompleteListener{
            if(it.isSuccessful){
                FirebaseStorage.getInstance().reference.child("Users/"+profilePicPath).downloadUrl.addOnCompleteListener {

                    val imgName = it.result.toString()
                    val uid = auth.currentUser?.uid

                    val email  = findViewById<EditText>(R.id.editTextRegister_username).text.toString()
                    val pass  = findViewById<EditText>(R.id.editTextRegister_password).text.toString()
                    val firstName = findViewById<EditText>(R.id.editTextRegister_name).text.toString()
                    val lastName = findViewById<EditText>(R.id.editTextRegister_surname).text.toString()
                    val number = findViewById<EditText>(R.id.editTextRegister_number).text.toString()



                    val user = User(uid,email,pass,firstName,lastName,number,imgName, arrayListOf("inicijalizacija_mySpots"),
                        arrayListOf("inicijalizacija_Requests"),"noBan")
                    userViewModel.addUser(user)
                    FirebaseMessaging.getInstance().subscribeToTopic("NewLocations")

                    if(progressDialog.isShowing)
                        progressDialog.dismiss()
                    val intent: Intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_PICK && resultCode == RESULT_OK) {
            var f:File=File(currentPhotoPath)
            binding.imageViewRegisterProfilePic.setImageURI(Uri.fromFile(f))//choosePhoto.setImageURI(Uri.fromFile(f))
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                val f = File(currentPhotoPath)
                mediaScanIntent.data = Uri.fromFile(f)
                sendBroadcast(mediaScanIntent)
            }
            profilePicUri= Uri.fromFile(f)
        }
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK)
        {
            profilePicUri=data?.data!!
            binding.imageViewRegisterProfilePic.setImageURI(profilePicUri)//choosePhoto.setImageURI(profilePicUri)
        }
    }
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.bikepark.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA_PICK)
                }
            }
        }
    }
    private fun createImageFile(): File {
        val storageDir: File? = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("bikeParkProfilePic",".jpg",storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }
    private fun showImageSourceDialog() {
        val options = arrayOf("Kamera", "Galerija")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Izaberite izvor slike")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    // Kamera
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        dispatchTakePictureIntent()
                    } else {
                        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 107)
                    }
                }
                1 -> {
                    // Galerija
                    choosePhotoFromGallery()
                }
            }
        }
        builder.show()
    }
    companion object{
        val GALLERY_PICK = 100
        val CAMERA_PICK = 101
    }
}
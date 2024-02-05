package com.example.bikepark

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.bikepark.ViewModel.UserViewModel
import com.example.bikepark.databinding.ActivityLoginBinding
import com.example.bikepark.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //userViewModel.getAllUsers()
        auth = FirebaseAuth.getInstance()

        if(auth.currentUser!= null){
            Toast.makeText(this,auth!!.currentUser!!.uid,Toast.LENGTH_SHORT).show()
            FirebaseMessaging.getInstance().subscribeToTopic("NewLocations").addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(this,"subscribed",Toast.LENGTH_SHORT).show()
                } else{
                    Toast.makeText(this,"not subscribed",Toast.LENGTH_SHORT).show()
                }
            }
            val i: Intent = Intent(this,MainActivity::class.java)
            startActivity(i)
            finish()
        }


        binding.textViewLoginRedirectRegister.setOnClickListener {
            val redirectIntent: Intent = Intent(this,RegisterActivity::class.java)
            startActivity(redirectIntent)
            finish()
        }
        binding.buttonLoginLogin.setOnClickListener({
            performLogIn()
        })

    }
    private fun performLogIn() {

        if(binding.editTextLoginUsername.text.isEmpty() || binding.editTextLoginPassword.text.isEmpty())
        {
            Toast.makeText(this,"Morate uneti oba polja",Toast.LENGTH_SHORT).show()
            return
        }

        val userInput = binding.editTextLoginUsername.text.toString()
        val passInput = binding.editTextLoginPassword.text.toString()

        auth.signInWithEmailAndPassword(userInput, passInput)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent: Intent = Intent(this,MainActivity::class.java)
                    FirebaseMessaging.getInstance().subscribeToTopic("NewLocations").addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            Toast.makeText(this,"subscribed",Toast.LENGTH_SHORT).show()
                        } else{
                            Toast.makeText(this,"not subscribed",Toast.LENGTH_SHORT).show()
                        }
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(baseContext, "Navlidan mail ili password", Toast.LENGTH_SHORT,).show()

                }
            }


    }
}
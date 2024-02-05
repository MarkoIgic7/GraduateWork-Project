package com.example.bikepark.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.bikepark.LoginActivity
import com.example.bikepark.R
import com.example.bikepark.ViewModel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging


class ProfileFragment : Fragment() {

    private lateinit var databaseReference: DatabaseReference
    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var auth: FirebaseAuth
    private lateinit var profilePicImgView: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileMail: TextView
    private lateinit var profilePass: TextView
    private lateinit var logOutBtn: Button
    private lateinit var changePassBtn: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel.getAllUsers()
        auth = FirebaseAuth.getInstance()
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        activity!!.setTitle("Moj profil")
        logOutBtn = view.findViewById<Button>(R.id.profileF_logOut)
        profilePicImgView = view.findViewById(R.id.profileF_profilePic)
        profileName = view.findViewById(R.id.profileF_name)
        profileMail = view.findViewById(R.id.profileF_mail)
        profilePass = view.findViewById(R.id.profileF_password)
        changePassBtn = view.findViewById(R.id.profileF_changePassword)

        val user=userViewModel._users.value!!.find { it.uid==FirebaseAuth.getInstance().currentUser!!.uid }
        Glide.with(context!!).load(user!!.imgName).into(profilePicImgView)
        profileName.text = user.name.toString()+" "+user.surname.toString()
        profileMail.text = user.email.toString()
        profilePass.text = user.password
        //profilePass.text = user.password
        logOutBtn.setOnClickListener({
            auth.signOut()
            FirebaseMessaging.getInstance().unsubscribeFromTopic("NewLocations")
            val redirectToLogin: Intent = Intent(context,LoginActivity::class.java)
            redirectToLogin.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(redirectToLogin)
        })
        changePassBtn.setOnClickListener({
            val newPass: String = profilePass.text.toString()
            showChangePasswordDialog(newPass)

        })
        return view
    }

    private fun showChangePasswordDialog(newPassword: String) {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.dialog_change_password)
        val dialog = builder.create()
        dialog.show()
        dialog.setCancelable(true)

        val newPassLabel = dialog.findViewById<TextView>(R.id.changePasswordDialog_newPassword)
        val yesBtn = dialog.findViewById<Button>(R.id.changePasswordDialog_yesBtn)
        val noBtn = dialog.findViewById<Button>(R.id.changePasswordDialog_noBtn)

        newPassLabel.text = newPassword
        noBtn.setOnClickListener({
            dialog.dismiss()
        })
        yesBtn.setOnClickListener({
            val user=userViewModel._users.value!!.find { it.uid==FirebaseAuth.getInstance().currentUser!!.uid }
            val currentUser = auth.currentUser
            currentUser!!.updatePassword(newPassword).addOnCompleteListener { it->
                if(it.isSuccessful){
                    Toast.makeText(context!!,"Uspesno promenjena sifra",Toast.LENGTH_SHORT).show()
                } else{
                    Toast.makeText(context!!,"Password nije promenjen",Toast.LENGTH_SHORT).show()
                }
            }
            user!!.password = newPassword
            userViewModel.updateUser(user)
            dialog.dismiss()
        })
    }

}
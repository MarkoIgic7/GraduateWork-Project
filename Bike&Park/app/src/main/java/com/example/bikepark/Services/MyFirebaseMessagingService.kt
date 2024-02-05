package com.example.bikepark.Services

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.bikepark.MainActivity
import com.example.bikepark.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*


const val channelId = "notification_channel"
const val channelName = "notification_channel_name"
class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseMessagingService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Handle the received message
        if (remoteMessage.notification != null) {
            Log.d("NOTIFTITLE",remoteMessage.notification!!.title.toString())
            Log.d("NOTIFTEXT",remoteMessage.notification!!.body.toString())
            generateNotification(remoteMessage.notification!!.title!!,remoteMessage.notification!!.body!!)
        }
    }
    fun generateNotification(title: String, message: String){
        /*val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE)*/

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationManagerCompat =  NotificationManagerCompat.from(applicationContext)

        //channel id, channel name
        var builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.bike_request_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)

        //builder = builder.setContent(getRemoteView(title,message))

        //26+
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(channelId, channelName,NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description = "asdasdas"
            notificationManager.createNotificationChannel(notificationChannel)
        }
        //notificationManager.notify(Math.random().toInt(), builder.build())
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        notificationManagerCompat.notify(Random().nextInt(), builder.build())
    }

    companion object {
        fun generateNotification(context: Context, title: String, message: String) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationManagerCompat = NotificationManagerCompat.from(context)

            // Ostatak funkcije ostaje nepromijenjen

            var builder: NotificationCompat.Builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.logo_circle)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val notificationChannel = NotificationChannel(channelId, channelName,NotificationManager.IMPORTANCE_DEFAULT)
                notificationChannel.description = "asdasdas"
                notificationManager.createNotificationChannel(notificationChannel)
            }
            //notificationManager.notify(Math.random().toInt(), builder.build())
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }


            notificationManagerCompat.notify(Random().nextInt(), builder.build())
        }
    }

}
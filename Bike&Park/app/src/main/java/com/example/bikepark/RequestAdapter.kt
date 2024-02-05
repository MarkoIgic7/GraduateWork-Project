package com.example.bikepark

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bikepark.Data.Request
import com.example.bikepark.ViewModel.RequestViewModel
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.*
import java.util.*

class RequestAdapter(
    private val requestList: ArrayList<Request?>,
    private val requestViewModel: RequestViewModel,
    context: Context
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    private val context: Context? = context


    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val address: TextView = itemView.findViewById(R.id.requestItem_address)
        val num: TextView = itemView.findViewById(R.id.requestItem_numOfRequests)
        val status: TextView = itemView.findViewById(R.id.requestItem_status)
        val processBtn: Button = itemView.findViewById(R.id.requestItem_process)
        val addParkingBtn: Button = itemView.findViewById(R.id.requestItem_addedParking)

        /*fun updateChildRequest(requestViewModel: RequestViewModel,request: Request){
            request.status = "U obradi"
            requestViewModel.updateRequest(request)
            //status.text="U obradi"
            itemView.findViewById<TextView>(R.id.requestItem_status).text="nebitno"
            itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.in_proceess_requests))
            processBtn.isEnabled=false
            Log.d("reqId",request.rid.toString())
        }*/

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.request_item, parent, false)
        return RequestViewHolder(view)
    }

    override fun getItemCount(): Int {
        return requestList.size
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requestList[position]
        val addressFirstPart = request!!.address!!.substringBefore(", Serbia")
        val address = addressFirstPart.substringAfter("Adresa : ")
        holder.address.text = address
        holder.num.text =
            "Trenutni broj zahteva : " + request.number.toString() + "/" + request.maxNumber.toString()
        holder.status.text = "Status: " + request.status

        var bgColor: Int = ContextCompat.getColor(holder.itemView.context, R.color.black)
        if (request.status == "U obradi") {
            bgColor = ContextCompat.getColor(holder.itemView.context, R.color.in_proceess_requests)
        } else {
            if (request.number!! < request.maxNumber) {
                bgColor =
                    ContextCompat.getColor(holder.itemView.context, R.color.not_enough_requests)
            } else {
                bgColor = ContextCompat.getColor(holder.itemView.context, R.color.enough_requests)
            }
        }
        holder.itemView.setBackgroundColor(bgColor)
        if (request.status == "U obradi") {
            holder.processBtn.isEnabled = false
        }
        holder.addParkingBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    getTokenAndSendMessage(holder.address.text.toString(), context!!,"google_cred.json")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        holder.processBtn.setOnClickListener({
            if(request.number!!.toInt() < request.maxNumber.toInt()){
                Toast.makeText(context!!,"Nije prikupljeno dovoljno zahteva",Toast.LENGTH_SHORT).show()
            } else{
                request.status = "U obradi"
                requestViewModel.updateRequest(request)
                notifyItemChanged(position)
            }

            //holder.updateChildRequest(requestViewModel,request)

            //holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.in_proceess_requests))
            //holder.processBtn.isEnabled = false
            //holder.status.text = "Status: ${request.status}"// ova linija stvara problem (kako da osvezim prikaz textView-a)
            //Log.d("BROJ@",itemCount.toString())
            //notifyDataSetChanged()

        })

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun getTokenAndSendMessage(address: String,context: Context,fileName: String) {
        try {
            val token  = getAccessToken(context!!,fileName)
            //Log.d("AccessToken77", token)
            //val token = getToken()
            // Call sendMessage with the obtained token
            sendMessage(token,address)
        } catch (e: Exception) {
            Log.d("getTokenError",e.toString())
            e.printStackTrace()
            // Handle exception if token retrieval or sendMessage fails
        }
    }

    suspend fun sendMessage(token: String, address: String){
        withContext(Dispatchers.IO) {
            try {
                val fcmMessage = JSONObject()
                    .put("message", JSONObject()
                        .put("topic", "NewLocations")
                        .put("notification", JSONObject()
                            .put("title", "Dodato novo stajaliste")
                            .put("body", "${address}")
                        )
                        .put("data", JSONObject()
                            .put("story_id", "story_12345")
                        )
                    )

                /*val payload = JSONObject()
                    .put("key", "value")*/

                // create the request body (POST request)
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = fcmMessage.toString().toRequestBody(mediaType)
                /*val requestBody = JSONObject()
                    .put("token",token)
                    .put("data",payload)
                    .toString().toRequestBody(mediaType)*/
                val fileName = "google_cred.json"

                val request = okhttp3.Request.Builder()
                    .url("https://fcm.googleapis.com/v1/projects/bikepark-b1873/messages:send")
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/joson; UTF-8")
                    .build()
                val response = OkHttpClient().newCall(request).execute()
                val responseBody = response.body?.string()
                val httpCode = response.code


                Log.d("Request123",request.toString())
                Log.d("reposneMessage",response.message.toString())
                Log.d("responseBody", responseBody.toString())
                Log.d("httpCode", httpCode.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    /*@OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun getToken(): String = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Get the FCM registration token
                    val token = task.result
                    // Resume the coroutine with the token
                    continuation.resume(token, onCancellation = {})
                } else {
                    // Resume the coroutine with an exception
                    //continuation.resumeWithException(task.exception ?: RuntimeException("Token retrieval failed"))
                }
            }
    }*/
    suspend fun getAccessToken(context: Context, serviceAccountFileName: String): String {
        return withContext(Dispatchers.IO) {
            val fileNameWithPath = "${serviceAccountFileName}"
            val inputStream = context.assets.open(fileNameWithPath)
            val credentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"))

            // Authenticate and refresh the credentials to obtain an access token
            credentials.refresh()

            // Return the access token
            credentials.accessToken.tokenValue


        }
    }


}

package com.ext.custom_otp_input

import android.app.Activity
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.gms.auth.api.phone.SmsRetriever

object OtpHelper {

    /**
     * Start SMS Retriever API to automatically listen for OTP messages.
     */
    fun startSmsRetriever(activity: Activity) {
        val client = SmsRetriever.getClient(activity)
        val task = client.startSmsRetriever()
        task.addOnSuccessListener {
            // SMS Retriever started successfully
        }
        task.addOnFailureListener { e ->
            // Failed to start SMS Retriever
            e.printStackTrace()
        }
    }

    /**
     * Register the SMS receiver to listen for OTP messages.
     * Safe for Android 13+ using exported receivers.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun registerSmsReceiver(activity: Activity, receiver: SmsReceiver) {
        try {
            val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity.registerReceiver(receiver, intentFilter, Activity.RECEIVER_EXPORTED)
            } else {
                activity.registerReceiver(receiver, intentFilter)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Unregister the SMS receiver.
     */
    fun unregisterSmsReceiver(activity: Activity, receiver: SmsReceiver) {
        try {
            activity.unregisterReceiver(receiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

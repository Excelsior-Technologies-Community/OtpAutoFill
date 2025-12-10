package com.ext.custom_otp_input

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import java.util.regex.Pattern

class SmsReceiver : BroadcastReceiver() {

    private var listener: OtpReceivedListener? = null

    fun setOtpListener(listener: OtpReceivedListener) {
        this.listener = listener
    }

    interface OtpReceivedListener {
        fun onOtpReceived(otp: String)
        fun onOtpTimeout()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? com.google.android.gms.common.api.Status
            when (status?.statusCode) {
                com.google.android.gms.common.api.CommonStatusCodes.SUCCESS -> {
                    val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE)
                    message?.let {
                        val otp = parseOtp(it)
                        if (otp != null) listener?.onOtpReceived(otp)
                    }
                }
                com.google.android.gms.common.api.CommonStatusCodes.TIMEOUT -> {
                    listener?.onOtpTimeout()
                }
            }
        }
    }

    /**
     * Extracts OTP from the message using regex.
     * Assumes OTP is 4-6 digit number in the SMS.
     */
    private fun parseOtp(message: String): String? {
        val pattern = Pattern.compile("\\b\\d{4,6}\\b")
        val matcher = pattern.matcher(message)
        return if (matcher.find()) matcher.group(0) else null
    }
}

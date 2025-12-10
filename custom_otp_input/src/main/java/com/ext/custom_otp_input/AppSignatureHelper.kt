package com.ext.custom_otpinput

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import android.util.Log
import java.security.MessageDigest

class AppSignatureHelper(private val context: Context) {

    private val TAG = "AppSignatureHelper"

    /**
     * Returns a list of app hashes for SMS Retriever API
     * Format for SMS: "<#> Your OTP is 123456 ABCDEFGHIJK"
     */
    fun getAppSignatures(): List<String> {
        val hashList = mutableListOf<String>()
        try {
            val packageName = context.packageName
            val packageManager = context.packageManager

            val signatures: Array<Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                val signingInfo = packageInfo.signingInfo
                if (signingInfo?.hasMultipleSigners() == true) signingInfo.apkContentsSigners
                else signingInfo?.signingCertificateHistory ?: emptyArray()
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures ?: emptyArray()
            }

            for (signature in signatures) {
                generateHash(packageName, signature.toCharsString())?.let { hash ->
                    hashList.add(hash)
                    Log.e(TAG, "APP HASH → $hash")
                    Log.e(TAG, "Use this in your SMS: <#> Your OTP is 123456 $hash")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating app hash", e)
        }
        return hashList
    }

    private fun generateHash(packageName: String, signature: String): String? {
        return try {
            val input = "$packageName $signature"
            val messageDigest = MessageDigest.getInstance("SHA-256")
            messageDigest.update(input.toByteArray(Charsets.UTF_8))
            val hash = messageDigest.digest().copyOf(9) // first 9 bytes
            Base64.encodeToString(hash, Base64.NO_PADDING or Base64.NO_WRAP).substring(0, 11)
        } catch (e: Exception) {
            null
        }
    }
}

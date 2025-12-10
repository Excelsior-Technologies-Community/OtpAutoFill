package com.ext.custom_otpinput

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowInsetsController
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.ext.custom_otp_input.OtpHelper
import com.ext.custom_otp_input.OtpInputView
import com.ext.custom_otp_input.SmsReceiver

class MainActivity : AppCompatActivity() {

    // --- Views ---
    private lateinit var phoneInputLayout: ConstraintLayout
    private lateinit var etPhoneNumber: EditText
    private lateinit var btnSendOtp: Button
    private lateinit var etCountryCode: EditText

    private lateinit var otpVerifyLayout: ConstraintLayout
    private lateinit var otpInputView: OtpInputView
    private lateinit var btnVerify: Button
    private lateinit var btnClear: Button
    private lateinit var tvResendCode: TextView
    private lateinit var tvPhoneNumberDisplay: TextView
    private lateinit var tvChangeNumber: TextView
    private lateinit var tvTimer: TextView

    // --- OTP & Timer ---
    private lateinit var smsReceiver: SmsReceiver
    private var enteredPhoneNumber = ""
    private var generatedOtp = ""
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private val OTP_TIMER_MILLIS = 30_000L // 30 seconds

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- STATUS BAR ---
        window.statusBarColor = android.graphics.Color.BLACK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = 0
        }

        initializeViews()
        initializeSmsReceiver()
        setupCountryCodeFocus()
        setupPhoneInputScreen()
        setupOtpScreen()
        showPhoneInputScreen()
    }

    private fun initializeViews() {
        phoneInputLayout = findViewById(R.id.phoneInputLayout)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        btnSendOtp = findViewById(R.id.btnSendOtp)
        etCountryCode = findViewById(R.id.etCountryCode)

        otpVerifyLayout = findViewById(R.id.otpVerifyLayout)
        otpInputView = findViewById(R.id.otpInputView)
        btnVerify = findViewById(R.id.btnVerify)
        btnClear = findViewById(R.id.btnClear)
        tvResendCode = findViewById(R.id.tvResendCode)
        tvPhoneNumberDisplay = findViewById(R.id.tvPhoneNumber)
        tvChangeNumber = findViewById(R.id.tvChangeNumber)
        tvTimer = findViewById(R.id.tvTimer)
    }

    private fun setupCountryCodeFocus() {
        etCountryCode.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etCountryCode.background = getDrawable(com.ext.custom_otp_input.R.drawable.bg_edittext_focused)
            } else {
                etCountryCode.background = getDrawable(com.ext.custom_otp_input.R.drawable.bg_edittext_default)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeSmsReceiver() {
        smsReceiver = SmsReceiver()
        smsReceiver.setOtpListener(object : SmsReceiver.OtpReceivedListener {
            override fun onOtpReceived(otp: String) {
                runOnUiThread {
                    otpInputView.setOtp(otp)
                    Toast.makeText(this@MainActivity, "OTP Auto-filled ✅", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onOtpTimeout() {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "OTP Timeout ❌", Toast.LENGTH_SHORT).show()
                }
            }
        })

        // Register receiver dynamically
        OtpHelper.registerSmsReceiver(this, smsReceiver)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupPhoneInputScreen() {
        btnSendOtp.setOnClickListener {
            val phone = etPhoneNumber.text.toString().trim()
            if (phone.length != 10 || !phone.all { it.isDigit() }) {
                Toast.makeText(this, "Enter valid 10-digit number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val countryCode = etCountryCode.text.toString().ifBlank { "+91" }
            enteredPhoneNumber = countryCode + phone
            sendSmsOtp(enteredPhoneNumber)
            showOtpScreen()
            startCountdownTimer()
        }
    }

    private fun setupOtpScreen() {
        btnVerify.setOnClickListener {
            val enteredOtp = otpInputView.getOtp()
            if (enteredOtp.length in 4..6) {
                if (enteredOtp == generatedOtp) {
                    Toast.makeText(this, "OTP Verified ✅", Toast.LENGTH_LONG).show()
                    // Go back to phone input screen instead of closing app
                    resetScreens()
                } else {
                    Toast.makeText(this, "OTP is incorrect ❌", Toast.LENGTH_LONG).show()
                }

            } else {
                Toast.makeText(this, "Enter full OTP", Toast.LENGTH_SHORT).show()
            }
        }

        btnClear.setOnClickListener { otpInputView.clearOtp() }
        tvResendCode.setOnClickListener {
            if (isTimerRunning) return@setOnClickListener
            sendSmsOtp(enteredPhoneNumber)
            otpInputView.clearOtp()
            startCountdownTimer()
        }
        tvChangeNumber.setOnClickListener {
            stopCountdownTimer()
            showPhoneInputScreen()
            otpInputView.clearOtp()
        }
    }

    // ---------------- SMS SENDING ----------------
    private fun sendSmsOtp(phoneNumber: String) {
        generatedOtp = (100000..999999).random().toString()
        val appHash = AppSignatureHelper(this).getAppSignatures().firstOrNull() ?: "FA+AppHash"

        val message = "<#> Your AppName OTP is $generatedOtp $appHash"

        try {
            val smsManager = android.telephony.SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "SMS failed: ${e.message}", Toast.LENGTH_LONG).show()
        }

        OtpHelper.startSmsRetriever(this)
    }

    // ---------------- SCREEN UTILS ----------------
    private fun showPhoneInputScreen() {
        phoneInputLayout.visibility = View.VISIBLE
        otpVerifyLayout.visibility = View.GONE
    }

    private fun showOtpScreen() {
        phoneInputLayout.visibility = View.GONE
        otpVerifyLayout.visibility = View.VISIBLE
        tvPhoneNumberDisplay.text = enteredPhoneNumber
    }

    private fun startCountdownTimer() {
        stopCountdownTimer()
        isTimerRunning = true
        tvResendCode.isEnabled = false
        countDownTimer = object : CountDownTimer(OTP_TIMER_MILLIS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                tvTimer.text = "Wait... 00:${String.format("%02d", sec)}"
            }

            override fun onFinish() {
                isTimerRunning = false
                tvResendCode.isEnabled = true
                tvTimer.text = "Send again"
            }
        }.start()
    }

    private fun stopCountdownTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
    }

    private fun resetScreens() {
        stopCountdownTimer()
        otpInputView.clearOtp()
        etPhoneNumber.text.clear()
        etCountryCode.text.clear()
        showPhoneInputScreen()
        generatedOtp = ""
    }

    override fun onBackPressed() {
        if (otpVerifyLayout.visibility == View.VISIBLE) {
            stopCountdownTimer()
            showPhoneInputScreen()
            otpInputView.clearOtp()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCountdownTimer()
        OtpHelper.unregisterSmsReceiver(this, smsReceiver)
    }
}

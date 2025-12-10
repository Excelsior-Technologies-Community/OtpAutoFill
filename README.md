# **OtpAutoFill**

A simple Android library and sample app to handle **OTP verification** with a custom input view.  
Users can enter or automatically receive OTP via SMS, verify it, and handle resend with a timer.  
This project includes a **custom OtpInputView** and a sample app demonstrating its usage.

---

## ✨ **Features**

- Custom **6-digit OTP input view**  
- Supports auto-fill from SMS using **SMS Retriever API**  
- **Clear OTP** button functionality  
- **Resend OTP** with countdown timer  
- Display phone number with country code  
- Change phone number without leaving the app  
- Proper **activity back-stack handling**  
- Clean, minimal **black & white UI** with highlighted buttons  

---

# **Preview**
---
<p align="center">
  <img src="https://github.com/user-attachments/assets/59527436-8dec-4d83-a975-4e20b19c92df"
       alt="Demo GIF"
       width="200">

</p>


## ⚡ **Installation**

**Step 1:** Add JitPack repository to your root build.gradle:

```gradle
maven { url = uri("https://jitpack.io") }
```

**Step 2:** Add the dependency in your app `build.gradle` (example if hosted on JitPack):  

```gradle
dependencies {
    implementation("com.github.YourUsername:CustomOtpInput:1.0.0")
}
```
## ⚡ **Usage**

```
<com.ext.custom_otp_input.OtpInputView
    android:id="@+id/otpInputView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:otpLength="6"
    app:boxSpacing="12dp"
    app:boxCornerRadius="12dp" />
```
## ⚡ **Initialize and use in MainActivity.kt:**

```
class MainActivity : AppCompatActivity() {

    private lateinit var otpInputView: OtpInputView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        otpInputView = findViewById(R.id.otpInputView)

        // Set OTP programmatically
        otpInputView.setOtp("123456")

        // Get entered OTP
        val enteredOtp = otpInputView.getOtp()

        // Clear OTP
        otpInputView.clearOtp()
    }
}

```

## **📄 License**

**MIT License**  
```
Copyright (c) 2025 Excelsior Technologies

Permission is hereby granted, free of charge, to any person obtaining a copy  
of this software and associated documentation files (the "Software"), to deal  
in the Software without restriction, including without limitation the rights  
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
copies of the Software, and to permit persons to whom the Software is  
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all  
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED **"AS IS"**, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

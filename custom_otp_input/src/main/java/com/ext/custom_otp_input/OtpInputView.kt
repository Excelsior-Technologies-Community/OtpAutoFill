package com.ext.custom_otp_input

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText

class OtpInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    private var otpLength = 6
    private var boxSpacing = 16f
    private var cornerRadius = 12f

    private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cursorPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var boxWidth = 0f
    private var boxHeight = 0f

    private var cursorVisible = true
    private var blinkRunnable: Runnable? = null

    private var listener: ((String) -> Unit)? = null

    init {
        background = null
        inputType = InputType.TYPE_CLASS_NUMBER
        filters = arrayOf(InputFilter.LengthFilter(otpLength))
        isCursorVisible = false

        boxPaint.strokeWidth = 3f
        boxPaint.style = Paint.Style.STROKE

        textPaint.textSize = 48f
        textPaint.textAlign = Paint.Align.CENTER

        cursorPaint.strokeWidth = 4f

        startCursorBlink()
        disableSelection()
    }

    // ---------------- DRAWING ----------------

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalSpacing = boxSpacing * (otpLength - 1)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        boxWidth = (width - totalSpacing - paddingLeft - paddingRight) / otpLength
        boxHeight = boxWidth * 1.2f
        setMeasuredDimension(width, (boxHeight + paddingTop + paddingBottom).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        val otp = text?.toString() ?: ""

        for (i in 0 until otpLength) {
            val left = paddingLeft + i * (boxWidth + boxSpacing)
            val top = paddingTop.toFloat()
            val rect = RectF(left, top, left + boxWidth, top + boxHeight)

            val filled = i < otp.length
            val active = i == otp.length

            boxPaint.color = when {
                filled -> 0xFFBBDEFB.toInt()
                active -> 0xFF2196F3.toInt()
                else -> 0xFFE0E0E0.toInt()
            }

            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, boxPaint)

            if (filled) {
                textPaint.color = 0xFF000000.toInt()
                val x = rect.centerX()
                val y = rect.centerY() + textPaint.textSize / 3
                canvas.drawText(otp[i].toString(), x, y, textPaint)
            }

            if (active && cursorVisible && hasFocus()) {
                canvas.drawLine(
                    rect.centerX(),
                    rect.top + boxHeight * 0.3f,
                    rect.centerX(),
                    rect.bottom - boxHeight * 0.3f,
                    cursorPaint
                )
            }
        }
    }

    // ---------------- INPUT ----------------

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        invalidate()
        if (s?.length == otpLength) listener?.invoke(s.toString())
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DEL && text?.isNotEmpty() == true) {
            setText(text?.dropLast(1))
            setSelection(text?.length ?: 0)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // ---------------- PUBLIC API ----------------

    /**
     * Directly fill OTP and move cursor to end.
     * Will auto-populate all boxes.
     */
    fun setOtp(otp: String) {
        setText(otp.take(otpLength))
        setSelection(text?.length ?: 0)
        invalidate() // redraw immediately
    }

    fun clearOtp() {
        setText("")
    }

    fun getOtp(): String = text?.toString() ?: ""

    fun setOtpLength(length: Int) {
        otpLength = length
        filters = arrayOf(InputFilter.LengthFilter(length))
        requestLayout()
        invalidate()
    }

    fun setOtpCompleteListener(l: (String) -> Unit) {
        listener = l
    }

    // ---------------- HELPERS ----------------

    private fun startCursorBlink() {
        blinkRunnable = object : Runnable {
            override fun run() {
                cursorVisible = !cursorVisible
                invalidate()
                postDelayed(this, 500)
            }
        }
        post(blinkRunnable)
    }

    private fun disableSelection() {
        isLongClickable = false // prevent unwanted long click
    }

    /**
     * Optional: paste OTP from clipboard if needed
     */
    fun pasteOtpFromClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData? = clipboard.primaryClip
        val text = clip?.getItemAt(0)?.text?.toString()
        text?.let { setOtp(it.filter { c -> c.isDigit() }) }
    }
}

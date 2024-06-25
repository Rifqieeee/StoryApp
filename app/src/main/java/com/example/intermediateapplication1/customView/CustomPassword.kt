package com.example.intermediateapplication1.customView

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputLayout

class CustomPassword @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs), View.OnTouchListener {

    private val minimumPasswordLength = 8

    init {
        transformationMethod = PasswordTransformationMethod()
        val passwordTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                val password = s.toString()
                val parent = parent.parent as TextInputLayout
                if (password.length < minimumPasswordLength) {
                    if (parent.error == null) {
                        parent.error = "Password must be minimum $minimumPasswordLength characters"
                    }
                } else {
                    if (parent.error != null) {
                        parent.error = null
                    }
                }
            }
        }
        addTextChangedListener(passwordTextWatcher)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return false
    }
}

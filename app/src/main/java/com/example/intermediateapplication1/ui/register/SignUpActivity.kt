package com.example.intermediateapplication1.ui.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.intermediateapplication1.customView.CustomButton
import com.example.intermediateapplication1.databinding.ActivitySignUpBinding
import com.example.intermediateapplication1.ui.login.LoginActivity
import com.google.android.material.textfield.TextInputLayout

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val signUpViewModel: SignUpViewModel by viewModels()

    private lateinit var signUpButton: CustomButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupListeners()
        playAnimation()
    }

    private fun initViews() {
        signUpButton = binding.signupButton

        with(binding) {
            name.editText?.addTextChangedListener(createTextWatcher(name))
            email.editText?.addTextChangedListener(createTextWatcher(email))
            password.editText?.addTextChangedListener(createTextWatcher(password))
        }

        setSignUpButtonEnabled()
    }

    private fun setupListeners() {
        signUpButton.setOnClickListener {
            if (checkAllFields()) {
                val name = binding.name.editText?.text.toString()
                val email = binding.email.editText?.text.toString()
                val password = binding.password.editText?.text.toString()
                registerUser(name, email, password)
            }
        }
    }

    private fun setSignUpButtonEnabled() {
        val isNameFilled = binding.name.editText?.text?.isNotEmpty() == true
        val isEmailFilled = binding.email.editText?.text?.isNotEmpty() == true
        val isPasswordFilled = binding.password.editText?.text?.isNotEmpty() == true
        signUpButton.isEnabled = isNameFilled && isEmailFilled && isPasswordFilled
    }

    private fun createTextWatcher(field: TextInputLayout): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                field.error = null
                setSignUpButtonEnabled()
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun checkAllFields(): Boolean {
        return when {
            binding.name.editText?.text.isNullOrEmpty() -> {
                binding.name.error = "Name is required"
                false
            }
            binding.email.editText?.text.isNullOrEmpty() -> {
                binding.email.error = "Email is required"
                false
            }
            binding.password.editText?.text.isNullOrEmpty() -> {
                binding.password.error = "Password is required"
                false
            }
            binding.password.editText?.length() ?: 0 < 8 -> {
                binding.password.error = "Password must be minimum 8 characters"
                false
            }
            else -> true
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        showLoading(true)
        signUpViewModel.registerUser(name, email, password) { response ->
            showLoading(false)
            if (response != null && !response.error) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                val errorMessage = response?.message ?: "Registration failed!"
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                Log.e("SignUpActivity", "Failed to register: ${response?.message}")
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun playAnimation() {
        val imageViewAnim = ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        val nameAnim = ObjectAnimator.ofFloat(binding.name, View.ALPHA, 0f, 1f).setDuration(500)
        val pageAnim = ObjectAnimator.ofFloat(binding.page, View.ALPHA, 0f, 1f).setDuration(500)
        val signupAnim = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 0f, 1f).setDuration(500)
        val emailAnim = ObjectAnimator.ofFloat(binding.email, View.ALPHA, 0f, 1f).setDuration(500)
        val passwordAnim = ObjectAnimator.ofFloat(binding.password, View.ALPHA, 0f, 1f).setDuration(500)

        val fieldsAnimSet = AnimatorSet().apply {
            playTogether(nameAnim, emailAnim, passwordAnim)
        }

        AnimatorSet().apply {
            playSequentially(pageAnim, fieldsAnimSet, signupAnim)
            start()
        }

        imageViewAnim.start()
    }
}

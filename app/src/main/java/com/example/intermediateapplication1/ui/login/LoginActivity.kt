package com.example.intermediateapplication1.ui.login

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
import androidx.lifecycle.lifecycleScope
import com.example.intermediateapplication1.databinding.ActivityLoginBinding
import com.example.intermediateapplication1.injection.Injection
import com.example.intermediateapplication1.ui.MainActivity
import com.example.intermediateapplication1.ui.register.SignUpActivity
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(Injection.provideUserRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeUI()
        playAnimation()

        checkExistingToken()

        binding.loginButton.setOnClickListener {
            if (validateFields()) {
                val email = binding.email.editText?.text.toString()
                val password = binding.password.editText?.text.toString()
                loginUser(email, password)
            }
        }

        binding.signupButton.setOnClickListener {
            navigateToSignUpActivity()
        }
    }

    private fun initializeUI() {
        binding.email.editText?.addTextChangedListener(createTextWatcher(binding.email))
        binding.password.editText?.addTextChangedListener(createTextWatcher(binding.password))
        setLoginButtonState()
    }

    private fun setLoginButtonState() {
        val emailFilled = binding.email.editText?.text?.isNotEmpty() == true
        val passwordFilled = binding.password.editText?.text?.isNotEmpty() == true
        binding.loginButton.isEnabled = emailFilled && passwordFilled
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val fadeInAnimators = listOf(
            binding.page,
            binding.email,
            binding.password,
            binding.loginButton,
            binding.signupButton
        ).map { ObjectAnimator.ofFloat(it, View.ALPHA, 0f, 1f).setDuration(500) }

        AnimatorSet().apply {
            playSequentially(fadeInAnimators)
            start()
        }
    }

    private fun checkExistingToken() {
        lifecycleScope.launch {
            val token = loginViewModel.getToken().first()
            if (!token.isNullOrEmpty()) {
                navigateToMainActivity()
            }
        }
    }

    private fun validateFields(): Boolean {
        val emailEditText = binding.email.editText
        val passwordEditText = binding.password.editText

        var isValid = true

        if (emailEditText?.text?.isEmpty() == true) {
            binding.email.error = "Email is required"
            isValid = false
        }

        if (passwordEditText?.text?.isEmpty() == true) {
            binding.password.error = "Password is required"
            isValid = false
        }

        return isValid
    }

    private fun loginUser(email: String, password: String) {
        showLoading(true)
        loginViewModel.loginUser(email, password) { response ->
            showLoading(false)
            if (response != null && !response.error) {
                handleLoginSuccess(response.loginResult?.token ?: "")
            } else {
                handleLoginFailure(response?.message)
            }
        }
    }

    private fun handleLoginSuccess(token: String) {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            loginViewModel.saveToken(token)
            navigateToMainActivity()
        }
    }

    private fun handleLoginFailure(message: String?) {
        Toast.makeText(this, message ?: "Login failed!", Toast.LENGTH_SHORT).show()
        Log.e("LoginActivity", "Failed to login: ${message ?: "response is null"}")
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToSignUpActivity() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun createTextWatcher(field: TextInputLayout): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                field.error = null
            }

            override fun afterTextChanged(s: Editable?) {
                setLoginButtonState()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}

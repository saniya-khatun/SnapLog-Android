package com.example.snaplog

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.snaplog.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        auth = FirebaseAuth.getInstance()

        // If already logged in, skip login
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // ---------- SIGN UP ----------
        b.btnSignup.setOnClickListener {

            val email = b.etEmail.text.toString().trim()
            val pass = b.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                b.tvStatus.text = "Enter email and password"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                b.tvStatus.text = "Invalid email format"
                return@setOnClickListener
            }

            if (pass.length < 6) {
                b.tvStatus.text = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            b.tvStatus.text = "Creating account..."

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    b.tvStatus.text = "Account created!"
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    b.tvStatus.text = "Signup failed: ${e.message}"
                }
        }

        // ---------- LOGIN ----------
        b.btnLogin.setOnClickListener {

            val email = b.etEmail.text.toString().trim()
            val pass = b.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                b.tvStatus.text = "Enter email and password"
                return@setOnClickListener
            }

            b.tvStatus.text = "Logging in..."

            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    b.tvStatus.text = "Login successful"
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    b.tvStatus.text = "Login failed: ${e.message}"
                }
        }
    }
}

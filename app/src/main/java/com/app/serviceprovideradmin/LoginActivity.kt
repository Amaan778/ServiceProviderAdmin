package com.app.serviceprovideradmin

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var btn: Button
    private var diaog: Dialog?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email=findViewById(R.id.email)
        password=findViewById(R.id.passwordEditText)
        btn=findViewById(R.id.loginbtn)


//        For password visibility
        val passwordInputLayout = findViewById<TextInputLayout>(R.id.passwordInputLayout)

        // Set the initial icon to the "eye-off" drawable (password hidden)
        passwordInputLayout.endIconDrawable = ContextCompat.getDrawable(this, R.drawable.eyeclose)

        // Variable to keep track of password visibility state
        var isPasswordVisible = false

        // Toggle password visibility when the end icon is clicked
        passwordInputLayout.setEndIconOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                // Show password
                password.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordInputLayout.endIconDrawable = ContextCompat.getDrawable(this, R.drawable.eyeopen)
            } else {
                // Hide password
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordInputLayout.endIconDrawable = ContextCompat.getDrawable(this, R.drawable.eyeclose)
            }

            // Move the cursor to the end after toggling
            password.setSelection(password.text.length)
        }

//        from this setting mail and password

        val adminEmail = "admin@gmail.com"
        val adminPassword = "admin123"

        btn.setOnClickListener {
            showdialog()

            val enterdemail=email.text.toString()
            val enterdpassword=password.text.toString()

//             Check if the entered credentials match the admin credentials
            if (enterdemail == adminEmail && enterdpassword == adminPassword) {
                startActivity(Intent(this,Dashboard::class.java))
                finish()
                Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show()
                diaog?.dismiss()
            } else {
                Toast.makeText(this, "Invalid Admin Credentials", Toast.LENGTH_SHORT).show()
                diaog?.dismiss()
            }

        }


    }
    private fun showdialog(){
        diaog= Dialog(this)
        diaog?.setContentView(R.layout.progressdialog)
        diaog?.setCancelable(false)
        diaog?.show()
    }
}
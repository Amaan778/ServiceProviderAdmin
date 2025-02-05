package com.app.serviceprovideradmin

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.serviceprovideradmin.Category.CreateCategory

class Dashboard : AppCompatActivity() {
    private lateinit var category:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        category=findViewById(R.id.category)

        category.setOnClickListener {
            startActivity(Intent(this,CreateCategory::class.java))
        }

    }
}
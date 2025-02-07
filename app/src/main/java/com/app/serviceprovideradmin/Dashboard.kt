package com.app.serviceprovideradmin

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.serviceprovideradmin.Category.CreateCategory
import com.app.serviceprovideradmin.bookings.BookingActivity
import com.app.serviceprovideradmin.manageservice.ManageService

class Dashboard : AppCompatActivity() {
    private lateinit var category:TextView
    private lateinit var manage:TextView
    private lateinit var allbook:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        category=findViewById(R.id.category)
        manage=findViewById(R.id.manageservice)
        allbook=findViewById(R.id.allbokings)

        category.setOnClickListener {
            startActivity(Intent(this,CreateCategory::class.java))
        }

        manage.setOnClickListener {
            startActivity(Intent(this,ManageService::class.java))
        }

        allbook.setOnClickListener {
            startActivity(Intent(this,BookingActivity::class.java))
        }

    }
}
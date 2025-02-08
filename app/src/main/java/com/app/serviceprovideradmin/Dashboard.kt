package com.app.serviceprovideradmin

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.serviceprovideradmin.Category.CreateCategory
import com.app.serviceprovideradmin.bookings.BookingActivity
import com.app.serviceprovideradmin.manageservice.ManageService
import com.app.serviceprovideradmin.notification.CreateNotificatiion
import com.app.serviceprovideradmin.sliderimage.SliderImage

class Dashboard : AppCompatActivity() {
    private lateinit var category:LinearLayout
    private lateinit var manage:LinearLayout
    private lateinit var allbook:LinearLayout
    private lateinit var slider:LinearLayout
    private lateinit var notification:LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        category=findViewById(R.id.category)
        manage=findViewById(R.id.manageservice)
        allbook=findViewById(R.id.allbokings)
        slider=findViewById(R.id.slider)
        notification=findViewById(R.id.notification)

        category.setOnClickListener {
            startActivity(Intent(this,CreateCategory::class.java))
        }

        manage.setOnClickListener {
            startActivity(Intent(this,ManageService::class.java))
        }

        allbook.setOnClickListener {
            startActivity(Intent(this,BookingActivity::class.java))
        }

        slider.setOnClickListener {
            startActivity(Intent(this,SliderImage::class.java))
        }

        notification.setOnClickListener {
            startActivity(Intent(this,CreateNotificatiion::class.java))
        }

    }
}
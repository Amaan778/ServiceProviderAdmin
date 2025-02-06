package com.app.serviceprovideradmin.manageservice

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.serviceprovideradmin.Dashboard
import com.app.serviceprovideradmin.R
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class DetailService : AppCompatActivity() {
    private lateinit var title: EditText
    private lateinit var descript: EditText
    private lateinit var rating: EditText
    private lateinit var price: EditText
    private lateinit var offer: EditText
    private lateinit var coverimg: ImageView
    private lateinit var catgry: TextView
    private lateinit var updatedata: Button
    private lateinit var delte: ImageView
    private var category: String? = null

    val db= Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_service)

        val gintent=intent.getStringExtra("id")
        Log.d("check", "onCreate: "+gintent)

        title=findViewById(R.id.title)
        rating=findViewById(R.id.rating)
        descript=findViewById(R.id.description)
        price=findViewById(R.id.price)
        offer=findViewById(R.id.offer)
        coverimg=findViewById(R.id.coverimg)
        catgry=findViewById(R.id.category)
        updatedata=findViewById(R.id.updateBtn)
        delte=findViewById(R.id.delete)

        val db = FirebaseFirestore.getInstance() // Initialize Firestore
        db.collection("services").document(gintent.toString()).get()
            .addOnSuccessListener {
                if (it != null) {
                    val tilt = it.data?.get("title").toString()
                    val desc = it.data?.get("description").toString()
                    val rat = it.data?.get("rating").toString()
                    val pric = it.data?.get("price").toString()
                    val offe = it.data?.get("offer").toString()
                    val img = it.data?.get("coverImage").toString()
                    val cat = it.data?.get("category").toString()

                    // Store 'cat' in the class-level variable
                    category = cat

                    title.setText(tilt)
                    descript.setText(desc)
                    rating.setText(rat)
                    price.setText(pric)
                    offer.setText(offe)
                    catgry.text = cat

                    if (img.isNotEmpty()) {
                        Glide.with(this)
                            .load(img)
                            .placeholder(R.drawable.splashservice) // Optional placeholder
                            .into(coverimg)
                    }

                    // Log the category value
                    Log.d("check", "Category: $category")
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
            }

        Log.d("check", "onCreate: "+ category)

        delte.setOnClickListener {
            showDeleteConfirmationDialog(gintent.toString(), category.toString())
        }

        updatedata.setOnClickListener {

            val tite=title.text.toString()
            val des=descript.text.toString()
            val rat=rating.text.toString()
            val pric=price.text.toString()
            val off=offer.text.toString()
            val cat=catgry.text.toString()

            val updates= mapOf(
                "title" to tite,
                "description" to des,
                "rating" to rat,
                "price" to pric,
                "offer" to off
            )

            db.collection("services").document(gintent.toString()).update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this,"Data updated sucessfully", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this,"Data not updated successfully", Toast.LENGTH_SHORT).show()
                }


        }

    }
    private fun showDeleteConfirmationDialog(value:String, categ: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.deletedialogbox, null)

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogView)

        val alertDialog = dialogBuilder.create()

        // Remove the default dialog background
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        alertDialog.show()

        // Access views from the custom layout
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)

        // Set click listeners
        btnCancel.setOnClickListener { alertDialog.dismiss() }
        btnDelete.setOnClickListener {
            deleteItem(value,categ)
            alertDialog.dismiss()
        }
    }

    private fun deleteItem(intentvalue:String ,categ:String) {

        val db= FirebaseFirestore.getInstance()

        db.collection("services").document(intentvalue).delete()
            .addOnSuccessListener {
//                startActivity(Intent(this,Dashboard::class.java))
//                finish()

                db.collection(categ).document(intentvalue).delete()
                    .addOnSuccessListener {
                        startActivity(Intent(this, Dashboard::class.java))
                        finish()
                        Toast.makeText(this, "Item deleted 2", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Item not deleted", Toast.LENGTH_SHORT).show()
                    }
                Toast.makeText(this, "Item deleted 1", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Item not deleted", Toast.LENGTH_SHORT).show()
            }
    }

}
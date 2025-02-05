package com.app.serviceprovideradmin.Category

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.serviceprovideradmin.R
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID

class CreateCategory : AppCompatActivity() {
    private lateinit var imageViewProfile: ImageView
    private lateinit var buttonSelectImage: Button
    private lateinit var buttonUploadData: Button
    private lateinit var editTextData1: EditText
    private lateinit var spinner: Spinner
    private var imageUri: Uri? = null
    private lateinit var emptyMessage: TextView
    private var diaog: Dialog?=null

    private lateinit var recyclerView: RecyclerView
    private val categoryList = mutableListOf<Category>()
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var databaseReference: DatabaseReference

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_category)

        // Initialize views
        imageViewProfile = findViewById(R.id.imageViewProfile)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)
        buttonUploadData = findViewById(R.id.buttonUploadData)
        editTextData1 = findViewById(R.id.categoryname)
        emptyMessage=findViewById(R.id.emptyarraylist)

        // Set button click listeners
        buttonSelectImage.setOnClickListener {
            pickImageFromGallery()
        }

        buttonUploadData.setOnClickListener {
            showdialog()
            uploadData()
        }

        recyclerView = findViewById(R.id.recyler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        categoryAdapter = CategoryAdapter(this,categoryList)
        recyclerView.adapter = categoryAdapter

        databaseReference = FirebaseDatabase.getInstance().getReference("categories")
        fetchCategories()

        // Load categories into spinner
//        loadCategoriesIntoSpinner()
    }

    private fun fetchCategories() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()
                for (dataSnapshot in snapshot.children) {
                    val category = dataSnapshot.getValue(Category::class.java)
                    val key = dataSnapshot.key // Get the unique key for this category
                    if (category != null && key != null) {
                        categoryList.add(category.copy(name = category.name, imageUrl = category.imageUrl, key = key))
                    }
                }
                if (categoryList.isEmpty()) {
                    emptyMessage.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyMessage.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }

                categoryAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors here
                Toast.makeText(this@CreateCategory,"failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val sourceUri = data.data ?: return
            val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg"))

            val options = UCrop.Options().apply {
                setCircleDimmedLayer(true)    // Circular outline
                setShowCropGrid(false)        // Hide the grid
                setShowCropFrame(false)       // Hide the crop frame
            }

            // Start uCrop with defined source and destination URIs
            UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f)     // Square aspect ratio for circular crop
                .withOptions(options)
                .start(this)

        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            imageUri = UCrop.getOutput(data!!) ?: return
            Glide.with(this).load(imageUri).circleCrop().into(imageViewProfile)
        }
    }

    private fun uploadData() {
        val data1 = editTextData1.text.toString().trim()

        if (imageUri == null || data1.isEmpty()) {
            Toast.makeText(this, "Please provide image and data", Toast.LENGTH_SHORT).show()
            diaog?.dismiss()
            return
        }

        // Upload image to Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference.child("images/${System.currentTimeMillis()}.jpg")
        storageRef.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()
                    uploadDataToDatabase(data1, imageUrl)
                    diaog?.dismiss()

                    // Reset fields after uploading
                    imageViewProfile.setImageResource(R.drawable.splashservice) // Set your default image here
                    editTextData1.text.clear()
                    imageUri = null
                }
            }
            .addOnFailureListener {
                diaog?.dismiss()
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadDataToDatabase(data1: String,  imageUrl: String) {
        val database = FirebaseDatabase.getInstance()
        val categoriesRef = database.getReference("categories")

        val categoryId = UUID.randomUUID().toString()
        val categoryData = mapOf(
            "id" to categoryId,
            "name" to data1,
            "imageUrl" to imageUrl
        )

        categoriesRef.child(categoryId).setValue(categoryData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data uploaded successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload data", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showdialog(){
        diaog= Dialog(this)
        diaog?.setContentView(R.layout.progressdialog)
        diaog?.setCancelable(false)
        diaog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        diaog?.show()
    }
}
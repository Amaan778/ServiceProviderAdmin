package com.app.serviceprovideradmin.manageservice

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.serviceprovideradmin.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID

class AddService : AppCompatActivity() {
    private lateinit var coverImageView: ImageView
    private lateinit var image1: ImageView
    private lateinit var image2: ImageView
    private lateinit var image3: ImageView
    private lateinit var titleInput: EditText
    private lateinit var ratingInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var priceInput: EditText
    private lateinit var offerInput: EditText
    private lateinit var spinner: Spinner
    private lateinit var uploadDataButton: Button
    private lateinit var imagePickerButton: Button

    private val selectedImages = mutableListOf<Uri>()
    private val galleryRequestCode = 101
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var coverImageUri: Uri? = null // Cover image URI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service)

        // Initialize views
        coverImageView = findViewById(R.id.coverimg)
        image1 = findViewById(R.id.image1)
        image2 = findViewById(R.id.image2)
        image3 = findViewById(R.id.image3)
        titleInput = findViewById(R.id.title)
        ratingInput = findViewById(R.id.rating)
        descriptionInput = findViewById(R.id.description)
        priceInput = findViewById(R.id.price)
        offerInput = findViewById(R.id.offer)
        spinner = findViewById(R.id.spinner)
        uploadDataButton = findViewById(R.id.uploadDataButton)
        imagePickerButton = findViewById(R.id.imagePickerButton)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initially hide image views
        image1.visibility = View.GONE
        image2.visibility = View.GONE
        image3.visibility = View.GONE

        // Handle image picker
        imagePickerButton.setOnClickListener {
            if (selectedImages.size < 4) {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = "image/*"
                }
                startActivityForResult(intent, galleryRequestCode)
            } else {
                Toast.makeText(this, "You can only select up to 4 images.", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle setting cover image
        image1.setOnClickListener { updateCoverImage(0) }
        image2.setOnClickListener { updateCoverImage(1) }
        image3.setOnClickListener { updateCoverImage(2) }

        // Handle upload data
        uploadDataButton.setOnClickListener {
            val spinners = spinner.selectedItem.toString()
            uploadDataToFirestore(spinners)
        }

        loadCategoriesIntoSpinner()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == galleryRequestCode && resultCode == RESULT_OK) {
            val uri = data?.data
            uri?.let {
                // Start UCrop for cropping the selected image
                startCrop(it)
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val croppedUri = UCrop.getOutput(data!!)
            croppedUri?.let { handleCroppedImage(it) }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Toast.makeText(this, "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))

        // Configure UCrop with rectangle cropping
        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(4f, 3f) // Rectangle aspect ratio (e.g., 4:3)
            .withMaxResultSize(1080, 720) // Max size for cropped image
            .withOptions(getCropOptions()) // Custom options
            .start(this)
    }

    // Custom UCrop options for rectangle cropping
    private fun getCropOptions(): UCrop.Options {
        val options = UCrop.Options()
        options.setToolbarColor(ContextCompat.getColor(this, R.color.themecolor))
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.white))
//        options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.colorAccent))
        options.setCompressionQuality(80) // Adjust image quality if needed
        options.setFreeStyleCropEnabled(true) // Allow freeform cropping if needed
        return options
    }

    // Handle the cropped image URI
    private fun handleCroppedImage(croppedUri: Uri) {
        selectedImages.add(croppedUri)

        when (selectedImages.size) {
            1 -> {
                coverImageUri = croppedUri
                coverImageView.setImageURI(croppedUri) // Set first image as cover
            }
            2 -> {
                image1.setImageURI(croppedUri)
                image1.visibility = View.VISIBLE
            }
            3 -> {
                image2.setImageURI(croppedUri)
                image2.visibility = View.VISIBLE
            }
            4 -> {
                image3.setImageURI(croppedUri)
                image3.visibility = View.VISIBLE
            }
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == galleryRequestCode && resultCode == RESULT_OK) {
//            val uri = data?.data
//            uri?.let {
//                selectedImages.add(it)
//
//                when (selectedImages.size) {
//                    1 -> {
//                        coverImageUri = it
//                        coverImageView.setImageURI(it) // Set first image as cover
//                    }
//                    2 -> {
//                        image1.setImageURI(it)
//                        image1.visibility = View.VISIBLE
//                    }
//                    3 -> {
//                        image2.setImageURI(it)
//                        image2.visibility = View.VISIBLE
//                    }
//                    4 -> {
//                        image3.setImageURI(it)
//                        image3.visibility = View.VISIBLE
//                    }
//                }
//            }
//        }
//    }

    private fun updateCoverImage(index: Int) {
        if (index < selectedImages.size) {
            coverImageUri = selectedImages[index]
            coverImageView.setImageURI(selectedImages[index])
            Toast.makeText(this, "Cover image updated.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadDataToFirestore(category: String) {
        val title = titleInput.text.toString().trim()
        val rating = ratingInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val price = priceInput.text.toString().trim()
        val offer = offerInput.text.toString().trim()
        val spinnerval=spinner.selectedItem.toString()

        if (title.isEmpty() || rating.isEmpty() || description.isEmpty() || price.isEmpty() || offer.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val data = hashMapOf(
            "title" to title,
            "rating" to rating,
            "description" to description,
            "price" to price,
            "offer" to offer,
            "category" to spinnerval
        )

        // Upload cover image
        coverImageUri?.let { uri ->
            val coverFileName = "cover_${System.currentTimeMillis()}.jpg"
            val coverStorageRef = storage.reference.child("images/$coverFileName")
            coverStorageRef.putFile(uri).addOnSuccessListener {
                coverStorageRef.downloadUrl.addOnSuccessListener { coverUrl ->
                    data["coverImage"] = coverUrl.toString()

                    // Upload additional images
                    uploadAdditionalImages(data,category)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to upload cover image: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "Please select a cover image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadAdditionalImages(data: HashMap<String, String>, category: String) {
        val additionalImages = selectedImages.filter { it != coverImageUri }
        val uuid = UUID.randomUUID().toString() // Generate a random UUID
        data["id"] = uuid // Add the UUID to the data map

        if (additionalImages.isEmpty()) {
            saveToFirestore(data, uuid,category) // No additional images to upload
            return
        }

        for ((index, uri) in additionalImages.withIndex()) {
            val fileName = "image_${System.currentTimeMillis()}_$index.jpg"
            val storageRef = storage.reference.child("images/$fileName")

            storageRef.putFile(uri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { url ->
                    // Add the URL to the data map with a unique key (e.g., image1, image2, image3)
                    data["image${index + 1}"] = url.toString()

                    // If all images are uploaded, save the complete data to Firestore
                    if (data.keys.filter { it.startsWith("image") }.size == additionalImages.size) {
                        saveToFirestore(data, uuid,category)
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to upload image: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveToFirestore(data: HashMap<String, String>, uid: String, category:String) {
        firestore.collection(category)
            .document(uid) // Use the generated UUID as the document ID
            .set(data)
            .addOnSuccessListener {
                saveToFirestoreadmin(data,uid)
//                Toast.makeText(this, "Data uploaded successfully.", Toast.LENGTH_SHORT).show()
//                resetForm()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToFirestoreadmin(data: HashMap<String, String>, uid: String) {
        firestore.collection("services")
            .document(uid) // Use the generated UUID as the document ID
            .set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Data uploaded successfully 2.", Toast.LENGTH_SHORT).show()
                resetForm()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun resetForm() {
        titleInput.text.clear()
        ratingInput.text.clear()
        descriptionInput.text.clear()
        priceInput.text.clear()
        offerInput.text.clear()
        selectedImages.clear()

        coverImageUri = null
        coverImageView.setImageResource(R.drawable.splashservice)
        image1.visibility = View.GONE
        image2.visibility = View.GONE
        image3.visibility = View.GONE
    }


    private fun loadCategoriesIntoSpinner() {
        val database = FirebaseDatabase.getInstance()
        val categoriesRef = database.getReference("categories")

        val categoriesList = mutableListOf<String>()
        val categoriesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriesList)
        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = categoriesAdapter

        categoriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoriesList.clear()
                for (categorySnapshot in snapshot.children) {
                    val categoryName = categorySnapshot.child("name").getValue(String::class.java)
                    categoryName?.let { categoriesList.add(it) }
                }
                categoriesAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddService, "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
package com.app.serviceprovideradmin.sliderimage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.serviceprovideradmin.R
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID

class SliderImage : AppCompatActivity() {
    private lateinit var coverImageView: ImageView
    private lateinit var image1: ImageView
    private lateinit var image2: ImageView
    private lateinit var image3: ImageView
    private lateinit var uploadDataButton: Button
    private lateinit var imagePickerButton: Button

    private val selectedImages = mutableListOf<Uri>()
    private val galleryRequestCode = 101
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var coverImageUri: Uri? = null // Cover image URI

    val db= Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slider_image)

        coverImageView = findViewById(R.id.coverimg)
        image1 = findViewById(R.id.image1)
        image2 = findViewById(R.id.image2)
        image3 = findViewById(R.id.image3)
        uploadDataButton = findViewById(R.id.uploadDataButton)
        imagePickerButton = findViewById(R.id.imagePickerButton)

        //        imageslider
        val imageSlider = findViewById<ImageSlider>(R.id.image_slider)

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

        uploadDataButton.setOnClickListener {
            uploadDataToFirestore("check")
        }


        db.collection("imageslider").document("slider").get()
            .addOnSuccessListener {
                val cover=it.data?.get("coverImage").toString()
                val img1=it.data?.get("image1").toString()
                val img2=it.data?.get("image2").toString()
                val img3=it.data?.get("image3").toString()

                val imageList = ArrayList<SlideModel>() // Create image list

                imageList.add(SlideModel(img1, ScaleTypes.FIT))
                imageList.add(SlideModel(img2, ScaleTypes.FIT))
                imageList.add(SlideModel(img3, ScaleTypes.FIT))
                imageSlider.setImageList(imageList)

            }
            .addOnFailureListener {
                Toast.makeText(this,"Error", Toast.LENGTH_LONG).show()
            }

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

    private fun updateCoverImage(index: Int) {
        if (index < selectedImages.size) {
            coverImageUri = selectedImages[index]
            coverImageView.setImageURI(selectedImages[index])
            Toast.makeText(this, "Cover image updated.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun uploadDataToFirestore(category: String) {

        val uid= UUID.randomUUID().toString()

        val data = hashMapOf(
            "id" to uid
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
        firestore.collection("imageslider")
            .document("slider") // Use the generated UUID as the document ID
            .set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Data uploaded successfully.", Toast.LENGTH_SHORT).show()
                resetfrom()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun resetfrom(){
        coverImageUri = null
        coverImageView.setImageResource(R.drawable.splashservice)
        image1.visibility = View.GONE
        image2.visibility = View.GONE
        image3.visibility = View.GONE
    }

}
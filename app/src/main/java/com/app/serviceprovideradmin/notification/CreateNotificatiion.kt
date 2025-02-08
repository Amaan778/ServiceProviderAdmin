package com.app.serviceprovideradmin.notification

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.serviceprovideradmin.R
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID

class CreateNotificatiion : AppCompatActivity() {
    private lateinit var image: ImageView
    private lateinit var title: EditText
    private lateinit var description: EditText
    private val galleryRequestCode = 101
    private val cropRequestCode = UCrop.REQUEST_CROP
    private var coverImageUri: Uri? = null
    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var btn: Button
    private lateinit var recycler: RecyclerView
    private var mlist=ArrayList<NotificationDataClass>()
    private lateinit var adapter:NotificationAdapter
    private lateinit var empty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_notificatiion)

        image=findViewById(R.id.image)
        title=findViewById(R.id.title)
        description=findViewById(R.id.description)
        btn=findViewById(R.id.btn)
        recycler=findViewById(R.id.recyler)
        empty=findViewById(R.id.empty)

        recycler.layoutManager= LinearLayoutManager(this)
        adapter= NotificationAdapter(this,mlist)
        recycler.adapter=adapter

        mlist.clear()

        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()

        firestore.collection("Notifications").addSnapshotListener(object :
            EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {

                if (error!=null){
                    Log.e("firestore", error.message.toString())
                    Toast.makeText(this@CreateNotificatiion,"Error"+error.message.toString(), Toast.LENGTH_LONG).show()
                }

                for (dc: DocumentChange in value?.documentChanges!!){
                    if (dc.type == DocumentChange.Type.ADDED) {
                        mlist.add(dc.document.toObject(NotificationDataClass::class.java))
                    }
                }

                if (mlist.isEmpty()){
                    empty.visibility= View.VISIBLE
                    recycler.visibility= View.GONE
                }else{
                    empty.visibility= View.GONE
                    recycler.visibility= View.VISIBLE
                }

                adapter?.notifyDataSetChanged()
            }
        })

        image.setOnClickListener {
            openGallery()
        }

        btn.setOnClickListener {
            uploadDataToFirestore()
        }

    }
    // Open Gallery to Select an Image
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*" // Filter to show only images
        startActivityForResult(intent, galleryRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == galleryRequestCode && resultCode == RESULT_OK) {
            val sourceUri = data?.data // URI of the selected image
            sourceUri?.let {
                startCrop(it) // Pass the selected image to UCrop
            }
        } else if (requestCode == cropRequestCode && resultCode == RESULT_OK) {
            val croppedUri = UCrop.getOutput(data!!) // URI of the cropped image
            croppedUri?.let {
                coverImageUri = it
                image.setImageURI(it) // Display the cropped image
                Toast.makeText(this, "Image Cropped Successfully", Toast.LENGTH_SHORT).show()
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            cropError?.let {
                Toast.makeText(this, "Crop Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Start UCrop for Cropping
    private fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))

        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f) // Square crop (1:1 aspect ratio)
            .withMaxResultSize(1080, 1080) // Maximum cropped image resolution
            .withOptions(getCropOptions())
            .start(this)
    }

    // Customize UCrop Options
    private fun getCropOptions(): UCrop.Options {
        val options = UCrop.Options()
        options.setToolbarColor(ContextCompat.getColor(this, R.color.themecolor))
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.themecolor))
        options.setCompressionQuality(90) // Adjust image quality
        options.setFreeStyleCropEnabled(true) // Allow freeform cropping
        return options
    }

    private fun uploadDataToFirestore() {
        val titl = title.text.toString().trim()
        val desc= description.text.toString().trim()

        if (titl.isEmpty() || coverImageUri == null) {
            Toast.makeText(this, "Please enter a title and select an image.", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate a unique file name for the image
        val fileName = "image_${System.currentTimeMillis()}.jpg"
        val storageRef = storage.reference.child("images/$fileName")

        val uid= UUID.randomUUID().toString()
        // Upload the image to Firebase Storage
        coverImageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                        // Save the image URL and title to Firestore
                        saveToFirestore(titl, imageUrl.toString(),desc,uid)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveToFirestore(title: String, imageUrl: String, description:String, uid:String) {
        // Create a data map
        val data = hashMapOf(
            "title" to title,
            "description" to description,
            "imageUrl" to imageUrl,
            "id" to uid
        )

        // Save the data to Firestore
        firestore.collection("Notifications")
            .document(uid)
            .set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Data uploaded successfully.", Toast.LENGTH_SHORT).show()
                resetForm()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Reset form after successful upload
    private fun resetForm() {
        title.text.clear()
        description.text.clear()
        coverImageUri = null
        image.setImageResource(android.R.color.transparent)
    }


}
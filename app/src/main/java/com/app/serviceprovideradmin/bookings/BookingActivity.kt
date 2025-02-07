package com.app.serviceprovideradmin.bookings

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.serviceprovideradmin.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore

class BookingActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private var mList = ArrayList<Bookmodelclass>()
    private lateinit var adapter: BookingRecycler
    var db= Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var text: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        recycler=findViewById(R.id.recyler)
        text=findViewById(R.id.texting)
        auth = Firebase.auth

        recycler.layoutManager= LinearLayoutManager(this)
        adapter=BookingRecycler(this,mList)
        recycler.adapter=adapter

        mList.clear()

        val uid = auth.currentUser?.uid
        db = FirebaseFirestore.getInstance()
        db.collection("Orders").addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("firestore", error.message.toString())
                }

                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        mList.add(dc.document.toObject(Bookmodelclass::class.java))
                    }
                }

                adapter?.notifyDataSetChanged()

                // Show or hide the empty message based on list size
                if (mList.isEmpty()) {
                    text.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                } else {
                    text.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                }
            }
        })

    }
}
package com.app.serviceprovideradmin.manageservice

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.serviceprovideradmin.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ManageService : AppCompatActivity() {
    private lateinit var add: FloatingActionButton
    private lateinit var recycler: RecyclerView
    private var mList = ArrayList<ManageserviceDataClass>()
    private lateinit var adapter: RecyclerAdapter
    var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_service)

        add=findViewById(R.id.add)
        recycler=findViewById(R.id.recyler)

        add.setOnClickListener {
            startActivity(Intent(this,AddService::class.java))
        }

        recycler.layoutManager= LinearLayoutManager(this)
        adapter=RecyclerAdapter(this,mList)
        recycler.adapter=adapter

        mList.clear()

        db = FirebaseFirestore.getInstance()
        db.collection("services").addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("firestore", error.message.toString())
                }

                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        mList.add(dc.document.toObject(ManageserviceDataClass::class.java))
                    }
                }

                adapter?.notifyDataSetChanged()
            }
        })

    }
}
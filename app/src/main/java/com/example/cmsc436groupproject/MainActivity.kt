package com.example.cmsc436groupproject

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private var level: Int = 5
    private lateinit var spinner: Spinner
    private lateinit var firebase: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        spinner = findViewById(R.id.spinner)
        firebase = FirebaseDatabase.getInstance()
        val defaultText = "Select Level"
        val defaultColor = Color.WHITE
        val defaultList = mutableListOf(defaultText)
        val defaultAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defaultList)
        spinner.adapter = defaultAdapter

        defaultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val userRef = firebase.reference.child("users").child("email")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val highestLevel = dataSnapshot.child("level").getValue(Int::class.java) ?: 0
                populateSpinner(highestLevel)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("ERROR", "there's an error")
            }
        })
    }
    private fun populateSpinner(highestLevel: Int) {
        val levels = mutableListOf<String>()
        for (level in highestLevel downTo 1) {
            levels.add("Level $level")
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, levels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

}


//        gameView = GameView(this, level)
//        setContentView(gameView)
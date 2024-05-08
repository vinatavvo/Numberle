package com.example.cmsc436groupproject

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var emailText: EditText
    private lateinit var playButton: Button
    private lateinit var loginButton: Button
    private lateinit var spinner: Spinner
    private lateinit var firebase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var email: String
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        spinner = findViewById(R.id.spinner)
        emailText = findViewById(R.id.editTextName)
        playButton = findViewById(R.id.play)
        loginButton = findViewById(R.id.login)
        firebase = FirebaseDatabase.getInstance()
        reference = firebase.getReference("emails")

        val defaultText = "Select Level"
        val defaultList = mutableListOf(defaultText)
        val defaultAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defaultList)
        defaultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = defaultAdapter

        loginButton.setOnClickListener {
            login(it)
        }

        playButton.setOnClickListener {
            play(it)
        }
    }

    fun login(v: View) {
        email = emailText.text.toString()
        reference.child(email).setValue(5)

        reference.child(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    reference.child(email).setValue(0)
                }
                populateSpinner(email)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("ERROR", "There's an error")
            }
        })
    }

    fun play(v: View) {
        val selectedLevelPosition = spinner.selectedItemPosition
        val selectedLevelText = spinner.selectedItem.toString()
        var levelNumber = 1
        if (selectedLevelText == "Select Level"){
            // implement push notification to tell them to choose a level
            Log.w("MainActivity", "PUSH NOTIFICATION")
        } else {
            levelNumber = selectedLevelText.substringAfter("Level ").toInt()
        }

        Log.d("MainActivity", "Selected Level Position: $selectedLevelPosition")
        Log.d("MainActivity", "Selected Level Number: $levelNumber")
        Log.d("MainActivity", "Spinner Count: ${spinner.count}")

        gameView = GameView(this, levelNumber + 2)
        setContentView(gameView)
    }

    private fun populateSpinner(email: String) {
        reference.child(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.w("MainActivity", dataSnapshot.getValue(Int::class.java).toString())
                val highestLevel = dataSnapshot.getValue(Int::class.java) ?: 1
                Log.d("MainActivity", "Highest Level: $highestLevel")
                val levels = mutableListOf<String>()
                for (level in highestLevel downTo 1) {
                    levels.add("Level $level")
                }
                val adapter =
                    ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, levels)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("ERROR", "There's an error")
            }
        })
    }
}
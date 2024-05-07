package com.example.cmsc436groupproject

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ValueEventListener
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private var level: Int = 5
    lateinit var firebase:FirebaseDatabase
    lateinit var reference: DatabaseReference
    lateinit var email:String
    var score = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebase = FirebaseDatabase.getInstance()
        reference = firebase.getReference("emails")

    }

    fun play(v: View){
        var email = findViewById<EditText>(R.id.editTextUser).text.toString()

        reference.child(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    reference.child(email).setValue(0)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w( "MainActivity", "error: " + error.message )
            }
        })

        gameView = GameView(this, level)
        setContentView(gameView)
    }
}
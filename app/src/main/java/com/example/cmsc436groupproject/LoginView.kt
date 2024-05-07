package com.example.cmsc436groupproject

import android.os.Bundle
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference

class LoginView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var firebase: FirebaseDatabase = FirebaseDatabase.getInstance()
        var reference: DatabaseReference = firebase.getReference("email")

        reference.setValue("test")


    }



}
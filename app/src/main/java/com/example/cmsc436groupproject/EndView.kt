package com.example.cmsc436groupproject

import android.Manifest
import android.R.attr.name
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class EndView: AppCompatActivity(){
    private lateinit var firebase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end)

        firebase = FirebaseDatabase.getInstance()
        reference = firebase.getReference("usernames")
    }

    fun checkHighScore(score : Int, userName : String) {
        reference.orderByValue().limitToLast(1).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val highestScore = snapshot.children.first().getValue(Int::class.java) ?: 0
                    if (score > highestScore) {
                        sendPushNotification(userName, score)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read highest score", error.toException())
            }
        })
    }

    fun sendPushNotification(userName: String, score: Int) {
        val intent = Intent(this, AlertDetails::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val message = "$userName set a new high score: $score"
        val builder = NotificationCompat.Builder(this, "12345")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("NEW HIGH SCORE!!")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(this)) {
            if (ContextCompat.checkSelfPermission(this@EndView, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return@with
            }
            notify(1, builder.build())
        }
    }



}
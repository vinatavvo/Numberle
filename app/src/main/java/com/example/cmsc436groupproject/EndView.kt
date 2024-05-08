package com.example.cmsc436groupproject



import android.Manifest
import android.R.attr.name
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.*
import android.widget.ListView



class EndView: AppCompatActivity(){
    private lateinit var firebase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var leaderboardAdapter: ArrayAdapter<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end)

        listView = findViewById(R.id.leaderboardListView)
        leaderboardAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        listView.adapter = leaderboardAdapter

        databaseReference = FirebaseDatabase.getInstance().getReference("usernames")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val leaderboardEntries = mutableListOf<Pair<String, Int>>()
                for (entry in dataSnapshot.children) {
                    val username = entry.key ?: ""
                    val highScore = entry.getValue(Int::class.java) ?: 0
                    leaderboardEntries.add(Pair(username, highScore))
                }

                leaderboardEntries.sortByDescending { it.second }
                leaderboardAdapter.clear()
                val numEntriesToShow = minOf(leaderboardEntries.size, 5)
                for (i in 0 until numEntriesToShow) {
                    val entry = leaderboardEntries[i]
                    val username = entry.first
                    val highScore = entry.second
                    val leaderboardItem = "$username: $highScore"
                    leaderboardAdapter.add(leaderboardItem)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("ERROR", "There's an error")
            }
        })
    }

    private fun goLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goContinue() {
        val intent = Intent(this, GameView::class.java)
        startActivity(intent)
        finish()
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
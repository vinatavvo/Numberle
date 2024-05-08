package com.example.cmsc436groupproject



import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class EndView: AppCompatActivity(){
    private lateinit var firebase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var leaderboardAdapter: ArrayAdapter<String>
    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end)

        listView = findViewById(R.id.leaderboardListView)
        leaderboardAdapter = object : ArrayAdapter<String>(this, R.layout.leaderboard, R.id.leaderboardText) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                return view
            }
        }
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
                var currentRank = 1
                var currentScore = Int.MAX_VALUE
                for ((index, entry) in leaderboardEntries.withIndex()) {
                    val username = entry.first
                    val highScore = entry.second
                    if (index > 0 && highScore != currentScore) {
                        currentRank++
                        if (currentRank > 5) break
                    }
                    val leaderboardItem = "#$currentRank $username: $highScore"
                    leaderboardAdapter.add(leaderboardItem)
                    currentScore = highScore
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("ERROR", "There's an error")
            }
        })
        sendPushNotification("test", 3)
    }

    private fun goLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goContinue() {
        // have the level passed in from local storage
        gameView = GameView(this, 1 + 2)
        setContentView(gameView)
    }


    private fun sendPushNotification(userName: String, score: Int) {
        val notificationId = 1
        val channelId = "12345"
        val message = "$userName set a new high score: $score"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.appicon)
            .setContentTitle("New High Score!")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)

        NotificationManagerCompat.from(this).apply {
            if (ActivityCompat.checkSelfPermission(
                    this@EndView,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            Log.w("EndView", "Push here")
            notify(notificationId, notificationBuilder.build())
        }
    }





}
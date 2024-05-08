package com.example.cmsc436groupproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
class EndView : AppCompatActivity(){
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
}
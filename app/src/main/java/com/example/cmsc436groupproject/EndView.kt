package com.example.cmsc436groupproject

import android.app.Activity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.database.*
class EndView(private val activity: Activity, private val database: DatabaseReference) {
    fun displayTopScores() {
        // get the top 5 scores
        database.orderByValue().limitToLast(5).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val topScores = mutableMapOf<String, Int>()

                for (snapshot in dataSnapshot.children) {
                    val key = snapshot.key ?: ""
                    val score = snapshot.value as? Long ?: 0
                    topScores[key] = score.toInt()
                }

                // display the top 5 scores
                val leaderboardContainer = activity.findViewById<LinearLayout>(R.id.leaderboardContainer)
                leaderboardContainer.removeAllViews()

//                for ((index, (key, score)) in topScores.toList().reversed().withIndex()) {
//                    val entryView = LayoutInflater.from(activity).inflate(R.layout.leaderboard_entry, null)
//                    val rankTextView = entryView.findViewById<TextView>(R.id.rankTextView)
//                    val keyTextView = entryView.findViewById<TextView>(R.id.keyTextView)
//                    val valueTextView = entryView.findViewById<TextView>(R.id.valueTextView)
//
//                    rankTextView.text = "${index + 1}"
//                    keyTextView.text = key
//                    valueTextView.text = "$score"
//
//                    leaderboardContainer.addView(entryView)
//                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error: ${databaseError.message}")
            }
        })
    }
}
package com.example.cmsc436groupproject

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var usernameInput: EditText
    private lateinit var playButton: Button
    private lateinit var loginButton: Button
    private lateinit var mode: Button
    private lateinit var spinner: Spinner
    private lateinit var firebase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    lateinit var username: String
    private var score = 0
    private var permission : String = Manifest.permission.POST_NOTIFICATIONS
    private lateinit var launcher : ActivityResultLauncher<String>
    private lateinit var layout: RelativeLayout
    private lateinit var titleTextView: TextView
    private lateinit var nameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        spinner = findViewById(R.id.spinner)
        usernameInput = findViewById(R.id.usernameInput)
        playButton = findViewById(R.id.play)
        loginButton = findViewById(R.id.login)
        mode = findViewById(R.id.mode)
        firebase = FirebaseDatabase.getInstance()
        reference = firebase.getReference("usernames")
        layout = findViewById(R.id.startScreen)
        titleTextView = findViewById(R.id.title)
        nameTextView = findViewById(R.id.name)

        setupModeUI()

        var notificationPermissionStatus : Int
                = ContextCompat.checkSelfPermission( this, permission )
        if( notificationPermissionStatus == PackageManager.PERMISSION_GRANTED ) {
            Log.w( "MainActivity", "Push Notification given" )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Notifications"
                val descriptionText = "Give Notifications"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val mChannel = NotificationChannel("12345", name, importance)
                mChannel.description = descriptionText

                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(mChannel)
                Log.w("MainActivity", "Notification Channel Created")
            }

        } else {
            Log.w( "MainActivity", "Asking user for push permission" )
            var contract : ActivityResultContracts.RequestPermission
                    = ActivityResultContracts.RequestPermission( )
            var results : Results = Results( )
            launcher = registerForActivityResult( contract, results )
            launcher.launch( permission )

        }

        val defaultText = "Select Level"
        val defaultList = mutableListOf(defaultText)
        val defaultAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defaultList)
        defaultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = defaultAdapter
        spinner.setBackgroundResource(R.drawable.spinner_background)

        loginButton.setOnClickListener {
            login(it)
        }

        playButton.setOnClickListener {
            play(it)
        }
    }
    inner class Results : ActivityResultCallback<Boolean> {
        override fun onActivityResult(result: Boolean) {
            if(result) {
                Log.w( "MainActivity", "User just granted permission" )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val name = "Notifications"
                    val descriptionText = "Give Notifications"
                    val importance = NotificationManager.IMPORTANCE_HIGH
                    val mChannel = NotificationChannel("12345", name, importance)
                    mChannel.description = descriptionText

                    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(mChannel)
                    Log.w("MainActivity", "Notification Channel Created")
                }


            } else {
                Log.w( "MainActivity", "Permission denied by user" )
            }
        }
    }
    private fun setupModeUI() {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        var isDarkMode = sharedPreferences.getBoolean("mode", false)
        Log.w("MainActivity", isDarkMode.toString())
        val backgroundColor: Int
        val textColor: Int
        val buttonBackgroundColor: Int
        val buttonTextColor: Int

        if (isDarkMode) {
            mode.text = "Dark Mode"
            backgroundColor = Color.BLACK
            textColor = Color.WHITE
            buttonBackgroundColor = Color.WHITE
            buttonTextColor = Color.BLACK
        } else {
            mode.text = "Light Mode"
            backgroundColor = Color.WHITE
            textColor = Color.BLACK
            buttonBackgroundColor = Color.BLACK
            buttonTextColor = Color.WHITE
        }

        layout.setBackgroundColor(backgroundColor)
        usernameInput.setTextColor(textColor)
        mode.setBackgroundColor(buttonBackgroundColor)
        mode.setTextColor(buttonTextColor)
        titleTextView.setTextColor(textColor)
        nameTextView.setTextColor(textColor)

        mode.setOnClickListener {
            isDarkMode = !isDarkMode
            val modeText = if (isDarkMode) "Dark Mode" else "Light Mode"
            mode.text = modeText
            val newBackgroundColor = if (isDarkMode) Color.BLACK else Color.WHITE
            val newTextColor = if (isDarkMode) Color.WHITE else Color.BLACK
            val newButtonBackgroundColor = if (isDarkMode) Color.WHITE else Color.BLACK
            val newButtonTextColor = if (isDarkMode) Color.BLACK else Color.WHITE

            layout.setBackgroundColor(newBackgroundColor)
            usernameInput.setTextColor(newTextColor)
            mode.setBackgroundColor(newButtonBackgroundColor)
            mode.setTextColor(newButtonTextColor)
            titleTextView.setTextColor(newTextColor)
            nameTextView.setTextColor(newTextColor)

            with(sharedPreferences.edit()) {
                putBoolean("mode", isDarkMode)
                apply()
            }
        }
    }
    fun login(v: View) {
        username = usernameInput.text.toString()

        reference.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(username.isEmpty()){
                    Toast.makeText(v.context, "Input A Username", Toast.LENGTH_LONG).show()
                    return
                }
                if (!dataSnapshot.exists()) {
                    reference.child(username).setValue(1)
                    Toast.makeText(v.context, "Account Created", Toast.LENGTH_LONG).show()
                }
                Toast.makeText(v.context, "Successful Log In", Toast.LENGTH_LONG).show()
                populateSpinner(username)
                val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                with(sharedPrefs.edit()) {
                    putString("username", username)
                    apply()
                }
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
            Toast.makeText(this, "Need to Log In", Toast.LENGTH_LONG).show()
            return
        } else {
            levelNumber = selectedLevelText.substringAfter("Level ").toInt()
        }

        Log.d("MainActivity", "Selected Level Position: $selectedLevelPosition")
        Log.d("MainActivity", "Selected Level Number: $levelNumber")
        Log.d("MainActivity", "Spinner Count: ${spinner.count}")

        gameView = GameView(this, levelNumber + 2)
        setContentView(gameView)
    }

    private fun populateSpinner(username: String) {
        reference.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
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
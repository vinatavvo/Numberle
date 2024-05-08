package com.example.cmsc436groupproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ProgressBar
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.abs
import kotlin.math.pow

class GameView(context: Context, private var level: Int) : LinearLayout(context) {

    private val inputButtonLabels = listOf(
        "⏎", "0", "1", "2", "3", "4",
        "5", "6", "7", "8", "9", "⌫",
    )

    private lateinit var levelTextView: TextView
    private lateinit var guessGrid: GridLayout
    private lateinit var inputButtonsLayout: LinearLayout
    private var game: Game = Game(level)
    private var currentGuessRow = 0
    private var currentGuessColumn = 0
    private lateinit var progressBar: ProgressBar
    private var ad : InterstitialAd? = null
    private lateinit var endView: EndView

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        setBackgroundColor(Color.BLACK)

        val gameNameTextView = TextView(context)
        gameNameTextView.text = "NUMBERLE"
        gameNameTextView.textSize = 36f
        gameNameTextView.setTextColor(Color.WHITE)
        gameNameTextView.gravity = Gravity.CENTER
        addView(gameNameTextView)

        levelTextView = TextView(context)
        val displayLevel = level - 2
        levelTextView.text = "Level: $displayLevel"
        levelTextView.textSize = 18f
        levelTextView.setTextColor(Color.WHITE)
        levelTextView.gravity = Gravity.CENTER
        val levelParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        levelTextView.layoutParams = levelParams
        addView(levelTextView)
        setupProgressBar()
        setupGuessGrid(level)
        setupInputButtons()
    }

    private fun setupGuessGrid(level: Int) {
        guessGrid = GridLayout(context)
        val guessLayoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        guessLayoutParams.gravity = Gravity.CENTER_HORIZONTAL
        guessGrid.layoutParams = guessLayoutParams
        guessGrid.columnCount = level

        val screenWidth = resources.displayMetrics.widthPixels
        val borderSize = 6
        val availableWidth = screenWidth - (level + 1) * 4 - 2
        val tileWidth = (availableWidth - borderSize * level) / level

        for (i in 0 until (level + 1)) {
            for (j in 0 until level) {
                val tile = GuessTile(context, "", level)
                val tileParams = GridLayout.LayoutParams()
                tileParams.width = tileWidth
                tileParams.height = tileWidth
                tileParams.setMargins(6, 6, 6, 6)
                tile.layoutParams = tileParams
                guessGrid.addView(tile)
            }
        }

        if (indexOfChild(guessGrid) == -1) {
            addView(guessGrid)
        }
    }

    private fun setRoundedBackground(view: View, cornerRadius: Float, color: Int) {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = cornerRadius
        drawable.setColor(color)
        view.background = drawable
    }

    private fun setupInputButtons() {
        inputButtonsLayout = LinearLayout(context)
        inputButtonsLayout.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        inputButtonsLayout.orientation = VERTICAL

        val numCols = 6
        val screenWidth = resources.displayMetrics.widthPixels
        val buttonMargin = 4
        val totalHorizontalMargin = (numCols + 1) * buttonMargin
        val buttonWidth = (screenWidth - totalHorizontalMargin) / numCols - buttonMargin * 2

        val buttonHeight = 150

        val numRows = 2
        val buttonsPerRow = inputButtonLabels.size / numRows

        for (rowIndex in 0 until numRows) {
            val rowLayout = LinearLayout(context)
            rowLayout.layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            rowLayout.orientation = HORIZONTAL

            for (colIndex in 0 until buttonsPerRow) {
                val labelIndex = rowIndex * buttonsPerRow + colIndex
                if (labelIndex < inputButtonLabels.size) {
                    val label = inputButtonLabels[labelIndex]
                    val button = Button(context)
                    button.text = label
                    button.textSize = 28f
                    button.setTextColor(Color.WHITE)
                    button.setBackgroundColor(context.getColor(R.color.input))
                    setRoundedBackground(button, 20f, context.getColor(R.color.input))
                    button.setOnClickListener {
                        handleInputButtonClick(label)
                    }
                    button.setTypeface(null, Typeface.BOLD)

                    val params = LayoutParams(
                        buttonWidth,
                        buttonHeight
                    )
                    params.setMargins(buttonMargin, buttonMargin, buttonMargin, buttonMargin)
                    button.layoutParams = params

                    rowLayout.addView(button)
                }
            }

            inputButtonsLayout.addView(rowLayout)
        }

        addView(inputButtonsLayout)
    }

    private fun handleInputButtonClick(buttonText: String) {
        when (buttonText) {
            "⌫" -> {
                // Handle backspace button click
                // Remove the last entered digit from the current guess
                if (currentGuessColumn > 0 || currentGuessRow > 0) {
                    val currentGuessIndex = currentGuessRow * guessGrid.columnCount + currentGuessColumn
                    if (currentGuessColumn > 0) {
                        val guessTile = guessGrid.getChildAt(currentGuessIndex - 1) as GuessTile
                        guessTile.text = ""
                        currentGuessColumn--
                    }
                }
            }
            "⏎" -> {
                // Handle enter button click
                // Submit the current guess
                val currentGuess = getCurrentGuess()
                if (currentGuess.size < guessGrid.columnCount) {
                    Toast.makeText(context, "Not enough digits", Toast.LENGTH_SHORT).show()
                } else {
                    val statusList = game.checkCorrectDigits(currentGuess)
                    highlightTiles(statusList, currentGuessRow)
                    disableButton(game.getWrongAnswers())
                    updateProgressBar(currentGuess)
                    currentGuessRow++
                    currentGuessColumn = 0
                    if (statusList.all { it == "o" }) {
                        Toast.makeText(context, "Great!", Toast.LENGTH_SHORT).show()
                        val handler = Handler(Looper.getMainLooper())
                        handler.postDelayed({
                            nextLevel()
                        }, 1000)
                    }
                    if (game.isGameOver()) {
                        gameOver()
                    }
                }
            }
            else -> {
                // Handle digit button click
                // Add the clicked digit to the current guess
                if (currentGuessRow < guessGrid.rowCount && currentGuessColumn < guessGrid.columnCount) {
                    val guessTile =
                        guessGrid.getChildAt(currentGuessRow * guessGrid.columnCount + currentGuessColumn) as GuessTile
                    guessTile.text = buttonText
                    currentGuessColumn++
                }
            }
        }
    }
    private fun getCurrentGuess(): List<Int> {
        val currentGuess = mutableListOf<Int>()
        for (i in 0 until guessGrid.columnCount) {
            val guessTile = guessGrid.getChildAt(currentGuessRow * guessGrid.columnCount + i) as GuessTile
            if (!guessTile.text.isNullOrEmpty()) {
                currentGuess.add(guessTile.text.toString().toInt())
            }
        }
        return currentGuess
    }

    private fun highlightTiles(statusList: List<String>, rowIndex: Int) {
        val startIndex = rowIndex * guessGrid.columnCount
        val endIndex = minOf(startIndex + guessGrid.columnCount, guessGrid.childCount)
        for (index in startIndex until endIndex) {
            val guessTile = guessGrid.getChildAt(index) as GuessTile
            val statusIndex = index - startIndex
            val status = statusList.getOrNull(statusIndex)
            when (status) {
                "o" -> guessTile.setBackgroundColor(context.getColor(R.color.correctBoth))
                "-" -> guessTile.setBackgroundColor(context.getColor(R.color.correctDigit))
                else -> guessTile.setBackgroundColor(context.getColor(R.color.wrong))
            }
        }
    }

    private fun disableButton(wrongAnswers: MutableSet<Int>) {
        for (rowIndex in 0 until inputButtonsLayout.childCount) {
            val rowLayout = inputButtonsLayout.getChildAt(rowIndex) as LinearLayout
            for (colIndex in 0 until rowLayout.childCount) {
                val button = rowLayout.getChildAt(colIndex) as Button
                val buttonLabel = button.text.toString()

                val buttonNumber = buttonLabel.toIntOrNull()
                if (buttonNumber != null && buttonNumber in wrongAnswers) {
                    button.isEnabled = false
                    setRoundedBackground(button, 20f, context.getColor(R.color.wrong))
                }
            }
        }
    }


    private fun gameOver() {
        val generatedAnswer = game.getAnswer().toString()
        val toastMessage = "Game Over. The answer was: $generatedAnswer"
        Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()

        var name = MainActivity.getName(context)
        var firebase = FirebaseDatabase.getInstance()
        var reference = firebase.getReference("usernames")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (entry in dataSnapshot.children) {
                    val username = entry.key ?: ""
                    if (username == name){
                        var curr = level - 2
                        val highScore = entry.getValue(Int::class.java) ?: 0
                        if(curr > highScore) {
                            reference.child(name).setValue(curr)
                        }
                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("ERROR", "There's an error")
            }
        })

        val intent = Intent(context, EndView::class.java)
        context.startActivity(intent)
    }

    private fun nextLevel() {
        var adUnitId : String = "ca-app-pub-3940256099942544/1033173712"
        var adRequest : AdRequest = AdRequest.Builder( ).build()
        var adLoad : AdLoad = AdLoad()
        InterstitialAd.load( context, adUnitId, adRequest, adLoad )
        level++
        levelTextView.text = "Level: ${level - 2}"
        guessGrid.removeAllViews()
        inputButtonsLayout.removeAllViews()
        setupGuessGrid(level)
        setupInputButtons()
        currentGuessRow = 0
        currentGuessColumn = 0
        progressBar.progress = 0
        game.newStage()
    }

    private fun setupProgressBar() {
        progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        val progressBarParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        progressBar.layoutParams = progressBarParams
        progressBar.max = 100
        progressBar.progress = 0

        addView(progressBar)
    }

    private fun updateProgressBar(currentGuess: List<Int>) {
        var guess: Long = 0
        for (i in currentGuess.indices) {
            guess += (currentGuess[i] * (10.0.pow(currentGuess.size - 1 - i).toInt()))
        }

        val answer = game.getAnswer()

        val difference = abs(guess - answer)

        val maxDifference = answer/100
        val differencePercentage = (difference.toFloat() / maxDifference)
        Log.d("Main", differencePercentage.toString())
        val progress = 100 - differencePercentage.toInt()
        progressBar.progress = progress
    }

    inner class AdLoad : InterstitialAdLoadCallback( ) {
        override fun onAdLoaded(p0: InterstitialAd) {
            super.onAdLoaded(p0)
            // assign p0 to ad
            ad = p0
            // show the ad
            if( ad != null ) {
                ad!!.show(context as Activity)
                // manage the user interaction with the ad
                var manageAd : ManageAdShowing = ManageAdShowing()
                ad!!.fullScreenContentCallback = manageAd
            }

        }

        override fun onAdFailedToLoad(p0: LoadAdError) {
            super.onAdFailedToLoad(p0)
            Log.w( "MainActivity", "error loading the ad: " + p0.message )
        }
    }

    inner class ManageAdShowing : FullScreenContentCallback( ) {
        override fun onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent()
            Log.w( "MainActivity", "Ad dismissed" )
        }

        override fun onAdClicked() {
            super.onAdClicked()
            Log.w( "MainActivity", "ad clicked"  )
        }

        override fun onAdImpression() {
            super.onAdImpression()
            Log.w( "MainActivity", "ad impressed" )
        }

        override fun onAdShowedFullScreenContent() {
            super.onAdShowedFullScreenContent()
            Log.w( "MainActivity", " ad shown" )
        }

        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
            super.onAdFailedToShowFullScreenContent(p0)
            Log.w( "MainActivity", " ad failed to show" )
        }
    }

}



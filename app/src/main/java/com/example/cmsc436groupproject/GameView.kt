package com.example.cmsc436groupproject

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView

class GameView(context: Context, level: Int) : LinearLayout(context) {

    private val inputButtonLabels = listOf(
        "ENTER", "0", "1", "2", "3", "4",
        "5", "6", "7", "8", "9", "⌫",
    )

    private lateinit var guessGrid: GridLayout
    private var game: Game = Game(level)
    private var currentGuessRow = 0
    private var currentGuessColumn = 0
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

        val levelTextView = TextView(context)
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

        setupGuessGrid(level)
        setupInputButtons(level)
    }

    private fun setupGuessGrid(level: Int) {
        guessGrid = GridLayout(context)
        guessGrid.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT,
            2f
        )

        guessGrid.rowCount = level + 1
        guessGrid.columnCount = level

        val screenWidth = resources.displayMetrics.widthPixels
        val borderSize = 3
        val availableWidth = screenWidth - (level + 1) * 4 - 2
        val tileWidth = (availableWidth - borderSize * level) / level

        for (i in 0 until (level + 1)) {
            for (j in 0 until level) {
                val tile = GuessTile(context, "", level)
                val tileParams = GridLayout.LayoutParams()
                tileParams.width = tileWidth
                tileParams.height = tileWidth
                tileParams.setMargins(4, 4, 4, 4)
                tile.layoutParams = tileParams
                guessGrid.addView(tile)
            }
        }

        addView(guessGrid)
    }

    private fun setupInputButtons(level: Int) {
        val numRows = 2
        val numCols = 6
        val buttonWidth = resources.displayMetrics.widthPixels / numCols

        val gameGridLayout = GridLayout(context)
        gameGridLayout.rowCount = numRows
        gameGridLayout.columnCount = numCols

        for (label in inputButtonLabels) {
            val button = Button(context)
            button.text = label
            button.textSize = 14f
            button.setTextColor(Color.WHITE)
            button.setBackgroundResource(android.R.drawable.btn_default)
            button.setOnClickListener {
                handleInputButtonClick(label)
            }

            val params = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f)
            )
            params.width = buttonWidth
            button.layoutParams = params

            gameGridLayout.addView(button)
        }

        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            0,
            15f / level
        )

        addView(gameGridLayout, layoutParams)
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
                    } else {
                        currentGuessRow--
                        currentGuessColumn = guessGrid.columnCount - 1
                        val guessTile = guessGrid.getChildAt(currentGuessIndex - 1) as GuessTile
                        guessTile.text = ""
                    }
                }
            }
            "ENTER" -> {
                // Handle enter button click
                // Submit the current guess
                val currentGuess = getCurrentGuess()
                Log.w("GameView", "$currentGuess")
                val statusList = game.checkCorrectDigits(currentGuess)
                highlightTiles(statusList, currentGuessRow) // Pass current guess row

                currentGuessRow++
                currentGuessColumn = 0
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
                "o" -> guessTile.setBackgroundColor(Color.parseColor("#6ca965"))
                "-" -> guessTile.setBackgroundColor(Color.parseColor("#c8b653"))
                else -> guessTile.setBackgroundColor(Color.parseColor("#787c7f"))
            }
        }
    }
}

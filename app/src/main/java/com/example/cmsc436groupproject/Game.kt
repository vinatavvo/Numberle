package com.example.cmsc436groupproject

import android.util.Log
import kotlin.random.Random

class Game(private val level: Int) {
    private var currScore: Float = 0.0F
    private var answer: Long = 0

    init {
        genAnswer()
    }

    fun setAnswer(answer: Long) {
        this.answer = answer
    }

    fun checkCorrectDigits(guess: List<Int>): List<Int> {
        val answerDigits = answer.toString().toCharArray().map { it.toString().toInt() }
        val correctDigits = mutableListOf<Int>()
        for (digit in guess) {
            if (digit in answerDigits && digit != answerDigits[guess.indexOf(digit)]) {
                correctDigits.add(digit)
            }
        }
        return correctDigits.distinct()
    }

    fun checkInPlaceDigits(guess: List<Int>): List<Int> {
        val answerDigits = answer.toString().toCharArray().map { it.toString().toInt() }
        val inPlaceDigits = mutableListOf<Int>()
        for (i in guess.indices) {
            if (guess[i] == answerDigits[i]) {
                inPlaceDigits.add(guess[i])
            }
        }
        return inPlaceDigits.distinct()
    }

    fun newStage() {
        genAnswer()
    }

    private fun genAnswer() {
        val firstDigit = Random.nextInt(1, 10)
        val remainingDigits = (1 until level).map { Random.nextInt(0, 10) }.joinToString("")
        val randomNumberAsString = "$firstDigit$remainingDigits"
        answer = randomNumberAsString.toLong()
        Log.w("Game", "Generated answer: $answer")
    }

    fun getScore(): Float {
        return currScore
    }

    fun getAnswer(): Long {
        return answer
    }

}
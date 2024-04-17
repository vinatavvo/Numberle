package com.example.cmsc436groupproject

import kotlin.math.abs
import kotlin.random.Random

class Game() {
    private var currScore : Float = 0.0F
    private var num : Long = 0
    private var numArr : ArrayList<Int> = ArrayList<Int>()
    private var stage : Int = 1
    private var answer : Long = 0


    init {
        genAnswer()
    }
    fun setNum(arr : ArrayList<Int>) {
        numArr = arr
        val numberAsString = numArr.joinToString("")
        num =  numberAsString.toLong()
    }

    //Return true if the number is correct
    fun checkNum() : Boolean {
        return answer == num
    }

    fun newStage() {
        stage++
        genAnswer()
    }

    private fun genAnswer() {
        val firstDigit = Random.nextInt(1, 10)
        val remainingDigits = (1 until stage+2).map { Random.nextInt(0, 10) }.joinToString("")
        val randomNumberAsString = "$firstDigit$remainingDigits"
        answer = randomNumberAsString.toLong()
    }

    //Returns arraylist of numbers that are in the answer but not in right place
    fun notInPLace() : ArrayList<Int> {
        var number = abs(answer)
        val digits = ArrayList<Int>()
        do {
            val digit = (number % 10).toInt()
            digits.add(digit)
            number /= 10
        } while (number > 0)

        val temp = ArrayList<Int>()
        digits.reverse()
        for(i in numArr) {
            if (i in digits && digits.indexOf(i) != numArr.indexOf(i)) {
                temp.add(i)
            }
        }
        return temp
    }

    //Returns arraylist of numbers that are in the answer but in right place
    fun inPlace() : ArrayList<Int>{
        var number = abs(answer)
        val digits = ArrayList<Int>()
        do {
            val digit = (number % 10).toInt()
            digits.add(digit)
            number /= 10
        } while (number > 0)

        val temp = ArrayList<Int>()
        digits.reverse()
        for(i in numArr) {
            if (i in digits && digits.indexOf(i) == numArr.indexOf(i)) {
                temp.add(i)
            }
        }
        return temp
    }

    fun getStage() : Int {
        return stage
    }

    fun getScore() : Float {
        return currScore
    }

    fun getAnswer() : Long {
        return answer
    }



}
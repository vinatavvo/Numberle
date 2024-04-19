package com.example.cmsc436groupproject

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView

class GuessTile(context: Context, text: String, level: Int) : AppCompatTextView(context) {

    init {
        setText(text)
        textSize = 150f / level
        setTextColor(Color.WHITE)
        setTypeface(null, Typeface.BOLD)
        setBackgroundWithBorder()
        gravity = Gravity.CENTER
    }

    private fun setBackgroundWithBorder() {
        val borderSize = 8
        val borderColor = context.getColor(R.color.wrong)

        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.setColor(Color.TRANSPARENT)
        drawable.setStroke(borderSize, borderColor)
        background = drawable
    }
}
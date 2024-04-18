package com.example.cmsc436groupproject

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView

class GuessTile(context: Context, private var text: String, level: Int) : AppCompatTextView(context) {

    init {
        setText(text)
        setTextSize(150f / level)
        setTextColor(Color.WHITE)
        setBackgroundWithBorder()
        gravity = Gravity.CENTER
    }

    private fun setBackgroundWithBorder() {
        val borderSize = 3
        val borderColor = Color.GRAY

        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.setColor(Color.TRANSPARENT)
        drawable.setStroke(borderSize, borderColor)
        background = drawable
    }
}
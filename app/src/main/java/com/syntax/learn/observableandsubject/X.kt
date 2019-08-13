package com.syntax.learn.observableandsubject

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import kotlin.math.ceil
import kotlin.math.sqrt

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
  return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun combineImages(bitmaps: List<Bitmap>): Bitmap? {
  val cs: Bitmap?

  val count = bitmaps.size
  val gridSize = ceil(sqrt(count.toFloat()))
  var numRows = gridSize.toInt()
  val numCols = gridSize.toInt()

  if ((gridSize * gridSize - count) >= gridSize) {
    numRows -= 1
  }

  val bitmap0 = bitmaps[0]
  val width = numCols * bitmap0.width
  val height = numRows * bitmap0.height

  cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

  val comboImage = Canvas(cs)

  for (row in 0 until numRows) {
    for (col in 0 until numCols) {
      val index = row * numCols + col
      if (index < count) {
        val bitmap = bitmaps[row * numCols + col]
        val left = col * bitmap0.width
        val top = row * bitmap0.height
        comboImage.drawBitmap(bitmap, left.toFloat(), top.toFloat(), null)
      }
    }
  }

  return cs
}
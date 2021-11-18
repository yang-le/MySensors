/*
 * Copyright (C) 2016 - Niklas Baudy, Ruben Gees, Mario Đanić and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package me.yangle.myphone.ui

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.text.Spanned
import android.text.TextPaint
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.EmojiSpan
import kotlin.math.roundToInt


internal class EmojiDrawable(unicode: String) : Drawable() {
    private var emojiSpan: EmojiSpan? = null
    private var processed = false
    private var emojiCharSequence: CharSequence?
    private val textPaint = TextPaint()
    private fun process() {
        emojiCharSequence = EmojiCompat.get().process(emojiCharSequence)
        if (emojiCharSequence is Spanned) {
            val spans = (emojiCharSequence as Spanned).getSpans(
                0, (emojiCharSequence as Spanned).length,
                EmojiSpan::class.java
            )
            if (spans.isNotEmpty()) {
                emojiSpan = spans[0] as EmojiSpan
            }
        }
    }

    override fun draw(canvas: Canvas) {
        textPaint.textSize = bounds.height() * TEXT_SIZE_FACTOR
        val y = (bounds.bottom - bounds.height() * BASELINE_OFFSET_FACTOR).roundToInt()
        if (!processed && EmojiCompat.get().loadState != EmojiCompat.LOAD_STATE_LOADING) {
            processed = true
            if (EmojiCompat.get().loadState != EmojiCompat.LOAD_STATE_FAILED) {
                process()
            }
        }
        if (emojiSpan == null) {
            emojiCharSequence?.let {
                canvas.drawText(
                    it,
                    0,
                    emojiCharSequence!!.length,
                    bounds.left.toFloat(),
                    y.toFloat(),
                    textPaint
                )
            }
        } else {
            emojiSpan!!.draw(
                canvas,
                emojiCharSequence,
                0,
                emojiCharSequence!!.length,
                bounds.left.toFloat(),
                bounds.top,
                y,
                bounds.bottom,
                textPaint
            )
        }
    }

    override fun setAlpha(alpha: Int) {
        textPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        textPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }

    companion object {
        private const val TEXT_SIZE_FACTOR = 0.8f
        private const val BASELINE_OFFSET_FACTOR = 0.225f
    }

    init {
        emojiCharSequence = unicode
        textPaint.style = Paint.Style.FILL
        textPaint.color = -0x1
        textPaint.isAntiAlias = true
    }
}

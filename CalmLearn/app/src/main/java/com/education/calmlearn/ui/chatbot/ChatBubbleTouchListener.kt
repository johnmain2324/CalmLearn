package com.education.calmlearn.ui.chatbot

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.hypot

class ChatBubbleTouchListener(
    private val edgeMarginPx: Int,
    private val onPositionSettled: () -> Unit
) : View.OnTouchListener {

    private var downRawX = 0f
    private var downRawY = 0f
    private var startX = 0f
    private var startY = 0f
    private var dragging = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val parent = view.parent as? View ?: return false
        val touchSlop = ViewConfiguration.get(view.context).scaledTouchSlop

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = event.rawX
                downRawY = event.rawY
                startX = view.x
                startY = view.y
                dragging = false
                view.parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - downRawX
                val dy = event.rawY - downRawY
                if (!dragging && hypot(dx, dy) > touchSlop) dragging = true
                if (dragging) {
                    view.x = (startX + dx).coerceIn(0f, (parent.width - view.width).toFloat())
                    view.y = (startY + dy).coerceIn(0f, (parent.height - view.height).toFloat())
                }
            }
            MotionEvent.ACTION_UP -> {
                if (dragging) snapToEdge(view, parent) else view.performClick()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (dragging) snapToEdge(view, parent)
            }
        }
        return true
    }

    private fun snapToEdge(view: View, parent: View) {
        val centerX = view.x + view.width / 2f
        val targetX = if (centerX < parent.width / 2f) {
            edgeMarginPx.toFloat()
        } else {
            (parent.width - view.width - edgeMarginPx).toFloat()
        }
        view.animate().x(targetX).setDuration(SNAP_DURATION_MS).withEndAction(onPositionSettled).start()
    }

    private companion object {
        const val SNAP_DURATION_MS = 150L
    }
}

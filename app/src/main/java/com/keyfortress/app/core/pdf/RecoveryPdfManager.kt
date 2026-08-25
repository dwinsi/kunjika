package com.keyfortress.app.core.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RecoveryPdfManager {

    fun generateRecoveryKit(
        context: Context,
        pinHint: String,
        recoveryKey: String,
        onComplete: (File?) -> Unit
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Background
        canvas.drawColor(Color.WHITE)

        // Header
        paint.color = Color.BLACK
        paint.textSize = 28f
        paint.isFakeBoldText = true
        canvas.drawText("KeyFortress: Emergency Recovery Kit", 50f, 80f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("Generated on: ${SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())}", 50f, 105f, paint)

        // Divider
        paint.strokeWidth = 2f
        canvas.drawLine(50f, 120f, 545f, 120f, paint)

        // Content
        paint.color = Color.BLACK
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("IMPORTANT: KEEP THIS PHYSICAL DOCUMENT SAFE", 50f, 160f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        val instructions = """
            This document contains essential information to recover your vault in case 
            you lose your phone or forget your Master PIN. Do not share this kit 
            with anyone you do not trust. It is recommended to print this 
            and store it in a secure physical location.
        """.trimIndent().split("\n")

        var currentY = 190f
        for (line in instructions) {
            canvas.drawText(line.trim(), 50f, currentY, paint)
            currentY += 18f
        }

        // Recovery Information
        currentY += 40f
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("Your PIN Hint:", 50f, currentY, paint)
        
        currentY += 25f
        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.color = Color.BLUE
        canvas.drawText(if (pinHint.isEmpty()) "[No hint set]" else pinHint, 70f, currentY, paint)

        currentY += 50f
        paint.color = Color.BLACK
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("Vault Recovery Key:", 50f, currentY, paint)

        currentY += 30f
        paint.textSize = 20f
        paint.color = Color.RED
        paint.typeface = android.graphics.Typeface.MONOSPACE
        canvas.drawText(recoveryKey, 70f, currentY, paint)

        // Bottom Warning
        paint.color = Color.GRAY
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("KeyFortress is a 100% localized application. Only you possess these keys.", 50f, 800f, paint)

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "KeyFortress_Recovery_Kit.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            onComplete(file)
        } catch (e: Exception) {
            onComplete(null)
        } finally {
            pdfDocument.close()
        }
    }
}

package com.kunjika.app.core.pdf

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
        canvas.drawText("Kunjika: Local Database Key Copy", 50f, 80f, paint)

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
            This document contains the local SQLCipher database passphrase. Keep it private.
            It does not restore entries on a new device; field keys stay in Android Keystore.
            Use an encrypted vault export to migrate entries to another device.
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
        canvas.drawText("Local Database Passphrase:", 50f, currentY, paint)

        currentY += 30f
        paint.textSize = 14f
        paint.color = Color.RED
        paint.typeface = android.graphics.Typeface.MONOSPACE
        recoveryKey.chunked(32).forEachIndexed { index, part ->
            canvas.drawText(part, 70f, currentY + index * 20f, paint)
        }

        // Bottom Warning
        paint.color = Color.GRAY
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("This PDF is not encrypted. Protect it and delete exported copies securely.", 50f, 800f, paint)

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "Kunjika_Recovery_Kit.pdf")
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

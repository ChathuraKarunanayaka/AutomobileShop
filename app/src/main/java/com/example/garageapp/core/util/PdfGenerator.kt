package com.example.garageapp.core.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.garageapp.domain.model.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points

    fun generateInvoicePdf(
        context: Context,
        invoice: Invoice,
        items: List<JobCardItem>,
        workshop: WorkshopDetails
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 18f
        }
        val subTitlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 14f
        }
        val normalPaint = Paint().apply {
            typeface = Typeface.DEFAULT
            textSize = 12f
        }
        val boldPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
        }

        var yPos = 50f

        // Workshop Header
        canvas.drawText(workshop.name.ifEmpty { "Workshop Name" }, 40f, yPos, titlePaint)
        yPos += 20f
        canvas.drawText(workshop.address, 40f, yPos, normalPaint)
        yPos += 15f
        canvas.drawText("Tel: ${workshop.phoneNumber}", 40f, yPos, normalPaint)
        yPos += 30f

        // Invoice Title
        canvas.drawText("INVOICE", PAGE_WIDTH / 2f - 30f, yPos, subTitlePaint)
        yPos += 30f

        // Customer & Invoice Details
        canvas.drawText("Invoice No: ${invoice.invoiceNumber}", 40f, yPos, boldPaint)
        canvas.drawText("Date: ${formatDate(invoice.createdAt)}", PAGE_WIDTH - 200f, yPos, normalPaint)
        yPos += 20f
        canvas.drawText("Customer: ${invoice.customerName}", 40f, yPos, normalPaint)
        canvas.drawText("Vehicle: ${invoice.vehicleNumber}", PAGE_WIDTH - 200f, yPos, normalPaint)
        yPos += 40f

        // Table Header
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawLine(40f, yPos - 15f, PAGE_WIDTH - 40f, yPos - 15f, paint)
        canvas.drawText("Description", 45f, yPos, boldPaint)
        canvas.drawText("Qty", 350f, yPos, boldPaint)
        canvas.drawText("Price", 420f, yPos, boldPaint)
        canvas.drawText("Total", 500f, yPos, boldPaint)
        yPos += 10f
        canvas.drawLine(40f, yPos, PAGE_WIDTH - 40f, yPos, paint)
        yPos += 20f

        // Items
        items.forEach { item ->
            canvas.drawText(item.description, 45f, yPos, normalPaint)
            canvas.drawText(item.quantity.toString(), 350f, yPos, normalPaint)
            canvas.drawText(item.sellingPrice.toInt().toString(), 420f, yPos, normalPaint)
            canvas.drawText(item.totalSellingPrice.toInt().toString(), 500f, yPos, normalPaint)
            yPos += 20f
            
            if (yPos > PAGE_HEIGHT - 150f) {
                // Should handle pagination here for a production app
            }
        }

        yPos += 20f
        canvas.drawLine(40f, yPos, PAGE_WIDTH - 40f, yPos, paint)
        yPos += 30f

        // Totals
        val rightAlignX = 400f
        canvas.drawText("Subtotal:", rightAlignX, yPos, normalPaint)
        canvas.drawText("Rs. ${invoice.subtotal.toInt()}", 500f, yPos, normalPaint)
        yPos += 20f
        canvas.drawText("Discount:", rightAlignX, yPos, normalPaint)
        canvas.drawText("Rs. ${invoice.discount.toInt()}", 500f, yPos, normalPaint)
        yPos += 20f
        canvas.drawText("Total Amount:", rightAlignX, yPos, boldPaint)
        canvas.drawText("Rs. ${invoice.totalAmount.toInt()}", 500f, yPos, boldPaint)
        yPos += 20f
        canvas.drawText("Paid Amount:", rightAlignX, yPos, normalPaint)
        canvas.drawText("Rs. ${invoice.paidAmount.toInt()}", 500f, yPos, normalPaint)
        yPos += 20f
        canvas.drawText("Balance:", rightAlignX, yPos, boldPaint)
        canvas.drawText("Rs. ${invoice.balanceAmount.toInt()}", 500f, yPos, boldPaint)

        yPos += 50f
        canvas.drawText(workshop.footerNote, 40f, yPos, normalPaint)

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "${invoice.invoiceNumber}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            pdfDocument.close()
        }
        return file
    }

    fun generateJobCardPdf(
        context: Context,
        jobCard: JobCard,
        workshop: WorkshopDetails
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 18f
        }
        val normalPaint = Paint().apply {
            typeface = Typeface.DEFAULT
            textSize = 12f
        }
        val boldPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
        }

        var yPos = 50f

        // Workshop Header
        canvas.drawText(workshop.name, 40f, yPos, titlePaint)
        yPos += 20f
        canvas.drawText(workshop.address, 40f, yPos, normalPaint)
        yPos += 15f
        canvas.drawText("Tel: ${workshop.phoneNumber}", 40f, yPos, normalPaint)
        yPos += 40f

        canvas.drawText("JOB CARD", PAGE_WIDTH / 2f - 30f, yPos, titlePaint)
        yPos += 30f

        canvas.drawText("Job Card No: ${jobCard.jobCardNumber}", 40f, yPos, boldPaint)
        canvas.drawText("Date: ${formatDate(jobCard.createdAt)}", PAGE_WIDTH - 200f, yPos, normalPaint)
        yPos += 25f

        canvas.drawText("Customer Name: ${jobCard.customerName}", 40f, yPos, normalPaint)
        yPos += 20f
        canvas.drawText("Phone Number: ${jobCard.customerPhone}", 40f, yPos, normalPaint)
        yPos += 20f
        canvas.drawText("Vehicle Number: ${jobCard.vehicleNumber}", 40f, yPos, boldPaint)
        yPos += 40f

        canvas.drawText("Complaint Description:", 40f, yPos, boldPaint)
        yPos += 20f
        // Simple multiline handling
        val lines = jobCard.complaintDescription.split("\n")
        lines.forEach { line ->
            canvas.drawText(line, 50f, yPos, normalPaint)
            yPos += 18f
        }

        yPos += 20f
        canvas.drawText("Inspection Notes:", 40f, yPos, boldPaint)
        yPos += 20f
        jobCard.inspectionNotes.split("\n").forEach { line ->
            canvas.drawText(line, 50f, yPos, normalPaint)
            yPos += 18f
        }

        yPos += 40f
        canvas.drawText("Status: ${jobCard.status.name}", 40f, yPos, boldPaint)

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "${jobCard.jobCardNumber}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            pdfDocument.close()
        }
        return file
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

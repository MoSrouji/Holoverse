package com.example.holoverse.ui.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream

class PdfManager(private val context: Context) {
    private var pdfRenderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null

    var currentPageIndex by mutableIntStateOf(0)
    var pageCount by mutableIntStateOf(0)

    private val _currentBitmap = MutableStateFlow<Bitmap?>(null)
    val currentBitmap: StateFlow<Bitmap?> = _currentBitmap

    fun loadPdf(uri: Uri) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_pdf.pdf")
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor!!)
            pageCount = pdfRenderer?.pageCount ?: 0
            renderPage(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun renderPage(index: Int) {
        if (index < 0 || index >= pageCount) return

        pdfRenderer?.let { renderer ->
            val page = renderer.openPage(index)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            _currentBitmap.value = bitmap
            currentPageIndex = index
            page.close()
        }
    }

    fun nextPage() {
        if (currentPageIndex < pageCount - 1) {
            renderPage(currentPageIndex + 1)
        }
    }

    fun previousPage() {
        if (currentPageIndex > 0) {
            renderPage(currentPageIndex - 1)
        }
    }

    fun close() {
        pdfRenderer?.close()
        fileDescriptor?.close()
        _currentBitmap.value = null
        currentPageIndex = 0
        pageCount = 0
    }
}

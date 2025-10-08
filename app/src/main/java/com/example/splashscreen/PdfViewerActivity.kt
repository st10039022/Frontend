package com.example.splashscreen

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView

class PdfViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        val pdfView = findViewById<PDFView>(R.id.pdfView)
        // default to nlbh.pdf
        val assetName = intent.getStringExtra("pdfFileName") ?: "nlbh.pdf"

        try {
            // Loads directly from /assets
            pdfView.fromAsset(assetName)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .spacing(8)
                .load()
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open $assetName", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}

package com.example.upiscanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var currentCameraId: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001
    private var cameraList: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            startCameraSetup()
        }
    }

    private fun startCameraSetup() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            cameraList = cameraProvider!!.availableCameraInfos.mapNotNull {
                CameraIdHelper.tryGetCameraId(it)
            }
            currentCameraId = cameraList.firstOrNull()
            invalidateOptionsMenu()
            startCamera()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startCamera() {
        val cameraProvider = cameraProvider ?: return

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, BarcodeAnalyzer())
            }

        val cameraSelector = CameraSelector.Builder()
            .addCameraFilter { cameras ->
                cameras.filter {
                    CameraIdHelper.tryGetCameraId(it) == currentCameraId
                }
            }.build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )
        } catch (exc: Exception) {
            Log.e("CameraX", "Use case binding failed", exc)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        cameraList.forEach { id ->
            menu?.add(Menu.NONE, Menu.NONE, Menu.NONE, id)
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        cameraList.forEach { id ->
            menu?.add("Camera ID $id")  // Label with visible text
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        currentCameraId = item.title.toString().removePrefix("Camera ID ")
        Toast.makeText(this, "Switched to camera ID: $currentCameraId", Toast.LENGTH_SHORT).show()
        startCamera()
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCameraSetup()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private inner class BarcodeAnalyzer : ImageAnalysis.Analyzer {
        private val scanner = BarcodeScanning.getClient()

        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            barcode.rawValue?.let { value ->
                                showResultDialog(value)
                                imageProxy.close()
                                return@addOnSuccessListener
                            }
                        }
                        imageProxy.close()
                    }
                    .addOnFailureListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }

    private fun showResultDialog(content: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("QR Code Detected")
                .setMessage(content)
                .setPositiveButton("OK", null)
                .show()
        }
    }
}

object CameraIdHelper {
    fun tryGetCameraId(cameraInfo: CameraInfo): String? {
        return try {
            val method = cameraInfo.javaClass.getMethod("getCameraId")
            method.invoke(cameraInfo) as? String
        } catch (e: Exception) {
            null
        }
    }
}
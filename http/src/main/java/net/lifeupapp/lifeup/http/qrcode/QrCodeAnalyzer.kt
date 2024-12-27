package net.lifeupapp.lifeup.http.qrcode

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class QRCodeAnalyser(private val listener: (Barcode, Int, Int) -> Unit) : ImageAnalysis.Analyzer {

    companion object {
        const val TAG = "BarcodeScanningActivity"
    }

    // config the current scan code format
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC
        )
        .build()
    private val detector = BarcodeScanning.getClient(options)

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: kotlin.run {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        detector.process(image)
            .addOnSuccessListener { barCodes ->
                if (barCodes.size > 0) {
                    listener.invoke(barCodes[0], imageProxy.width, imageProxy.height)
                    // 接收到结果后，就关闭解析
                    detector.close()
                }
            }
            .addOnFailureListener { Log.d(TAG, "Error: ${it.message}") }
            .addOnCompleteListener { imageProxy.close() }
    }
}

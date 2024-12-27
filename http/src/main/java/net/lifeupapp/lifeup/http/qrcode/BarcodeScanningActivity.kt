package net.lifeupapp.lifeup.http.qrcode

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Size
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import net.lifeupapp.lifeup.http.databinding.ActivityBarcodeScanningBinding
import java.util.concurrent.Executors

class BarcodeScanningActivity : AppCompatActivity() {

    private val TAG = "BarcodeScanningActivity"

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private var listener: OverlayListener? = null

    private var camera: Camera? = null

    private var scaleX = 0f

    private var scaleY = 0f

    private lateinit var binding: ActivityBarcodeScanningBinding

    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Handle the returned Uri
        // scan from gallery
        uri?.let {
            Log.i(TAG, "scan from gallery: $it")
            // analyze the image
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_AZTEC
                )
                .build()
            val detector = BarcodeScanning.getClient(options)
            val image = InputImage.fromFilePath(this, it)
            detector.process(image)
                .addOnSuccessListener { barCodes ->
                    if (barCodes.size > 0) {
                        val result = barCodes[0]
                        detector.close()

                        Handler(mainLooper).postDelayed({
                            val intent = Intent()
                            intent.putExtra(SCAN_RESULT, result.rawValue)
                            setResult(RESULT_OK, intent)
                            finish()
                        }, 150)
                    }
                }
                .addOnFailureListener { Log.d(TAG, "Error: ${it.message}") }
        }
    }

    companion object {
        const val SCAN_RESULT = "BarcodeScanningActivity.scan_result"
        const val REQUEST_PERMISSION = 12345
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // init view binding
        binding = ActivityBarcodeScanningBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA),
            REQUEST_PERMISSION
        )
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        listener = OverlayListener()
        binding.overlay.viewTreeObserver.addOnGlobalLayoutListener(listener)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    inner class OverlayListener : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                bindScan(cameraProvider, binding.overlay.width, binding.overlay.height)
            }, ContextCompat.getMainExecutor(this@BarcodeScanningActivity))
            binding.overlay.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun bindScan(cameraProvider: ProcessCameraProvider, width: Int, height: Int) {

        Log.i(TAG, "bindScan: width:$width height:$height")

        val preview: Preview = Preview.Builder()
            .build()

        // bind preview
        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        // use the camera that is facing back
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        // config image analysis
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(width, height))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        // scan from gallery
        binding.ivGallery.setOnClickListener {
            getContent.launch("image/*")
        }

        // bind image analysis
        imageAnalysis.setAnalyzer(
            Executors.newSingleThreadExecutor(),
            QRCodeAnalyser { barcode, imageWidth, imageHeight ->
                cameraProvider.unbindAll()
                initScale(imageWidth, imageHeight)
                barcode.boundingBox?.let {
                    // qr code bounding box
                    binding.overlay.addRect(translateRect(it))
                    Log.i(
                        TAG,
                        "bindScan: left:${it.left} right:${it.right} top:${it.top} bottom:${it.bottom}"
                    )
                }
                Handler(mainLooper).postDelayed({
                    val intent = Intent()
                    intent.putExtra(SCAN_RESULT, barcode.rawValue)
                    setResult(RESULT_OK, intent)
                    finish()
                }, 150)
            }
        )
        // bind camera to lifecycle
        camera = cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            imageAnalysis,
            preview
        )
    }

    private fun translateX(x: Float): Float = x * scaleX
    private fun translateY(y: Float): Float = y * scaleY

    // translate rect to current screen size
    private fun translateRect(rect: Rect) = RectF(
        translateX(rect.left.toFloat()),
        translateY(rect.top.toFloat()),
        translateX(rect.right.toFloat()),
        translateY(rect.bottom.toFloat())
    )

    private fun initScale(imageWidth: Int, imageHeight: Int) {
        val overlay = binding.overlay
        if (isPortraitMode(this)) {
            scaleY = overlay.height.toFloat() / imageWidth.toFloat()
            scaleX = overlay.width.toFloat() / imageHeight.toFloat()
        } else {
            scaleY = overlay.height.toFloat() / imageHeight.toFloat()
            scaleX = overlay.width.toFloat() / imageWidth.toFloat()
        }
    }
}

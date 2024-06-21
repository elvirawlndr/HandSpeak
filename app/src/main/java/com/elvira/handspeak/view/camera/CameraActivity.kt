package com.elvira.handspeak.view

import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.elvira.handspeak.R
import com.elvira.handspeak.data.response.PredictSibiResponse
import com.elvira.handspeak.data.retrofit.ApiConfig
import com.elvira.handspeak.databinding.ActivityCameraBinding
import com.elvira.handspeak.helper.ImageClassifierHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.tensorflow.lite.task.vision.classifier.Classifications
import retrofit2.Call
import retrofit2.Callback
import toBitmap
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val modelType = intent.getStringExtra(MenuActivity.EXTRA_MODEL_TYPE)
        if (modelType != null) {
            initializeImageClassifier(modelType)
        }

        val uploadBtn: View = findViewById(R.id.floatingGalleryButton1)
        uploadBtn.setOnClickListener {
            Log.d("CameraActivity", "Upload button clicked")
            val intent = Intent(this@CameraActivity, UploadActivity::class.java)
            startActivity(intent)
            Log.d("CameraActivity", "Intent to UploadActivity started")
        }
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUI()
        startCamera()
    }

    private fun initializeImageClassifier(modelType: String) {
        val modelName = when (modelType) {
            "SIBI" -> "ml_model_SIBI_rgb.tflite"
            "BISINDO" -> "ml_model_BISINDO_tuned.tflite"
            else -> {"ml_model_SIBI_rgb.tflite"}
        }

        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    runOnUiThread {
                        Toast.makeText(this@CameraActivity, error, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                    runOnUiThread {
                        results?.let {
                            if (it.isNotEmpty() && it[0].categories.isNotEmpty()) {
                                val sortedCategories =
                                    it[0].categories.sortedByDescending { it.score }
                                val displayResult =
                                    sortedCategories.joinToString("\n") {
                                        "${it.label} "
                                            //.format(it.score).trim()
                                    }

                                binding.tvResultAnalyze1.text = displayResult
                                binding.tvInferenceTime.text = "$inferenceTime ms"
                            } else {
                                binding.tvResultAnalyze1.text = ""
                                binding.tvInferenceTime.text = ""
                            }
                        }
                    }
                }
            }
        )
        imageClassifierHelper.updateModelName(modelName)
        startCamera()

    }

    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val resolutionSelector = ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                .build()
            val imageAnalyzer = ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setTargetRotation(binding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
            imageAnalyzer.setAnalyzer(Executors.newSingleThreadExecutor()) { image ->
                imageClassifierHelper.classifyImage(image)
            }

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            /*val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()*/

            /*imageAnalyzer.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                processImage(imageProxy)
            }*/

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (exc: Exception) {
                Toast.makeText(
                    this@CameraActivity,
                    "Failed to bind camera lifecycle.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "startCamera: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(image: ImageProxy) {
        val mediaImage: Image? = image.image
        if (mediaImage != null) {
            val bitmap = mediaImage.toBitmap()
            image.close()

            // Convert bitmap to JPEG and send it to the backend
            lifecycleScope.launch(Dispatchers.IO) {
                val response = sendImageToBackend(bitmap)
                response?.let {
                    runOnUiThread {
                        binding.tvResultAnalyze1.text = it
                        binding.tvInferenceTime.text = it
                    }
                }
            }
        }
    }

    private fun sendImageToBackend(bitmap: Bitmap): String? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()

        val requestBody: RequestBody = byteArray.toRequestBody("image/jpeg".toMediaType())
        val multipartBody: MultipartBody.Part =
            MultipartBody.Part.createFormData("photo", "image.jpg", requestBody)
        var result: String? = null

        lifecycleScope.launch {
            try {
                val apiService = ApiConfig.getHandspeakApiService()
                val response = apiService.uploadSibiImage(multipartBody)

                response.enqueue(object : Callback<PredictSibiResponse> {
                    override fun onResponse(
                        call: Call<PredictSibiResponse>,
                        response: retrofit2.Response<PredictSibiResponse>
                    ) {
                        result = if (response.isSuccessful) {
                            val successResponse = response.body()
                            successResponse?.data?.result ?: "No result"
                        } else {
                            "Failed to upload image: ${response.code()} ${response.message()}"
                        }
                    }

                    override fun onFailure(call: Call<PredictSibiResponse>, t: Throwable) {
                        result = "Failed to upload image: ${t.message.toString()}"
                    }

                })
            } catch (e: Exception) {
                result = "Error uploading image: ${e.message}"
            }
        }
        return result
    }

    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private val orientationEventListener by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return
                }

                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageCapture?.targetRotation = rotation
            }
        }
    }

    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationEventListener.disable()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "CameraActivity"
        const val EXTRA_CAMERAX_IMAGE = "CameraX Image"
        const val CAMERAX_RESULT = 200
    }
}

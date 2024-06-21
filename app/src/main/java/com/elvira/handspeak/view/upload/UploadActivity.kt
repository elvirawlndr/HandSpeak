package com.elvira.handspeak.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.elvira.handspeak.R
import com.elvira.handspeak.data.response.PredictBisindoResponse
import com.elvira.handspeak.data.response.PredictSibiResponse
import com.elvira.handspeak.data.retrofit.ApiConfig
import com.elvira.handspeak.databinding.ActivityUploadBinding
import com.elvira.handspeak.uriToFile
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UploadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadBinding
    private var currentImageUri: Uri? = null
    private var modelType: String? = null

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showToast("Permission request granted")
            } else {
                showToast("Permission request denied")
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        modelType = intent.getStringExtra(EXTRA_MODEL_TYPE)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        binding.floatingGalleryButton.setOnClickListener {
            if (allPermissionsGranted()) {
                startGallery()
            } else {
                requestPermissionLauncher.launch(REQUIRED_PERMISSION)
            }
        }

        binding.buttonAnalyze.setOnClickListener {
            currentImageUri?.let {
                showLoading(true)
                if (modelType == "SIBI") {
                    uploadSibiImage()
                } else if (modelType == "BISINDO"){
                    uploadBisindoImage()
                }
            } ?: run {
                showToast("Please insert image first")
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) {
            showToast("No image selected")
        } else {
            UCrop.of(uri, Uri.fromFile(File(cacheDir, "cropped_image")))
                .withOptions(UCrop.Options().apply {
                    setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL)
                    setToolbarColor(ContextCompat.getColor(this@UploadActivity, R.color.purple))
                    setStatusBarColor(ContextCompat.getColor(this@UploadActivity, R.color.black))
                })
                .start(this@UploadActivity)
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            val contentResolver = applicationContext.contentResolver
            val inputStream = contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            binding.imgUpload.setImageBitmap(bitmap)
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let {
                currentImageUri = it
                showImage()
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            cropError?.let {
                showToast("Failed cropping image")
            }
        }
    }

    private fun uploadSibiImage() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this)
            Log.d("Image Classification File", "showImage: ${imageFile.path}")
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )
            lifecycleScope.launch {
                try {
                    val apiService = ApiConfig.getHandspeakApiService()
                    val response = apiService.uploadSibiImage(multipartBody)

                    response.enqueue(object : Callback<PredictSibiResponse> {
                        override fun onResponse(
                            call: Call<PredictSibiResponse>,
                            response: Response<PredictSibiResponse>
                        ) {
                            showLoading(false)
                            if (response.isSuccessful) {
                                val successResponse = response.body()
                                successResponse?.let {
                                    binding.tvResultAnalyze.text = it.data?.result
                                    showToast(getString(R.string.sibi_success))
                                }
                            } else {
                                showToast("Failed to upload image: ${response.code()} ${response.message()}")
                            }
                        }

                        override fun onFailure(call: Call<PredictSibiResponse>, t: Throwable) {
                            showLoading(false)
                            showToast("Failed to upload image: ${t.message.toString()}")
                        }

                    })
                } catch (e: Exception) {
                    showLoading(false)
                    showToast("Error uploading image: ${e.message}")
                }
            }
        }

    }

    private fun uploadBisindoImage() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this)
            Log.d("Image Classification File", "showImage: ${imageFile.path}")
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )
            lifecycleScope.launch {
                try {
                    val apiService = ApiConfig.getHandspeakApiService()
                    val response = apiService.uploadBisindoImage(multipartBody)

                    response.enqueue(object : Callback<PredictBisindoResponse> {
                        override fun onResponse(
                            call: Call<PredictBisindoResponse>,
                            response: Response<PredictBisindoResponse>
                        ) {
                            showLoading(false)
                            if (response.isSuccessful) {
                                val successResponse = response.body()
                                successResponse?.let {
                                    binding.tvResultAnalyze.text = it.data?.result
                                    showToast(getString(R.string.bisindo_success))
                                }
                            } else {
                                showToast("Failed to upload image: ${response.code()} ${response.message()}")
                            }
                        }

                        override fun onFailure(call: Call<PredictBisindoResponse>, t: Throwable) {
                            showLoading(false)
                            showToast("Failed to upload image: ${t.message.toString()}")
                        }

                    })
                } catch (e: Exception) {
                    showLoading(false)
                    showToast("Error uploading image: ${e.message}")
                }
            }
        }
    }

    // for detection using .tflite model
    /*private fun analyzeImage(uri: Uri) {
        val imageClassifierHelper = ImageClassifierHelperGallery(
            context = this,
            classifierListener = object : ImageClassifierHelperGallery.ClassifierListener {
                override fun onError(error: String) {
                    runOnUiThread { showToast(error) }
                }

                override fun onResults(results: List<Classifications>?) {
                    runOnUiThread {
                        results?.let { it ->
                            if (it.isNotEmpty() && it[0].categories.isNotEmpty()) {
                                println()
                                val sortedCategories = it[0].categories.sortedByDescending { it?.score }
                                val result = sortedCategories.first()
                                val displayResult = "${result.label} "

                                lifecycleScope.launch(Dispatchers.Main) {
                                    updateUI(displayResult)
                                    //uploadImage()
                                }
                            }
                        }
                    }
                }
            }
        )
        //imageClassifierHelper.classifyStaticImage(uri)
    }

    private fun updateUI(result: String) {
        val resultTextView: TextView = findViewById(R.id.tvResultAnalyze)
        resultTextView.text = "$result"
    }*/

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.READ_MEDIA_IMAGES
        const val EXTRA_MODEL_TYPE = "EXTRA_MODEL_TYPE"
    }
}
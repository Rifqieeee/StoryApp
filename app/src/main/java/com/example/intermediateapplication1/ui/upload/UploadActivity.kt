package com.example.intermediateapplication1.ui.upload
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.example.intermediateapplication1.data.UserPreference
import com.example.intermediateapplication1.databinding.ActivityUploadBinding
import com.example.intermediateapplication1.retrofit.ApiConfig
import com.example.intermediateapplication1.ui.createCustomTempFile
import com.example.intermediateapplication1.ui.getImageUri
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.quality

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

data class MultipartData(
    val filePart: MultipartBody.Part,
    val descriptionBody: RequestBody,
    val latBody: RequestBody?,
    val lonBody: RequestBody?
)

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private var currentImageUri: Uri? = null
    private val Context.dataStore by preferencesDataStore(name = "settings")
    private lateinit var userPreference: UserPreference
    private var currentLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            Toast.makeText(this, "Permissions Granted", Toast.LENGTH_LONG).show()
            getLocation()
        } else {
            Toast.makeText(this, "Permissions Denied", Toast.LENGTH_LONG).show()
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

        userPreference = UserPreference.getInstance(dataStore)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        binding.galleryButton.setOnClickListener {
            startGallery()
        }
        binding.cameraButton.setOnClickListener {
            startCamera()
        }
        binding.uploadButton.setOnClickListener {
            uploadStory()
        }
        binding.btnSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getLocation()
            } else {
                currentLocation = null
            }
        }
    }


    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun startGallery() {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("TAG", "No Media Selected")
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                    Log.d("PostActivity", "Location: ${location.latitude}, ${location.longitude}")
                }
            }
        }
    }

    private suspend fun createMultipart(
        fileUri: Uri,
        description: String,
        lat: Float?,
        lon: Float?,
        context: Context
    ): MultipartData = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver

        val file = createCustomTempFile(context)

        contentResolver.openInputStream(fileUri)?.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        // Compress the image file
        val compressedFile = Compressor.compress(context, file) {
            default()
            quality(75) // Set the desired quality level (0-100)
        }

        val reqFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), compressedFile)
        val body = MultipartBody.Part.createFormData("photo", compressedFile.name, reqFile)

        val descriptionBody = RequestBody.create("text/plain".toMediaTypeOrNull(), description)
        val latBody =
            lat?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it.toString()) }
        val lonBody =
            lon?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it.toString()) }
        MultipartData(body, descriptionBody, latBody, lonBody)
    }

    private fun uploadStory() {
        val description = binding.descriptionEditText.text.toString()
        if (currentImageUri != null && description.isNotEmpty()) {
            val lat = if (binding.btnSwitch.isChecked) currentLocation?.latitude?.toFloat() else null
            val lon = if (binding.btnSwitch.isChecked) currentLocation?.longitude?.toFloat() else null

            // Check file size before creating multipart
            val fileSize = getFileSize(currentImageUri!!)
            val MAX_FILE_SIZE = 100000000
            if (fileSize > MAX_FILE_SIZE) {
                Toast.makeText(
                    this,
                    "File size exceeds the maximum limit (${MAX_FILE_SIZE} MB)",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            // Call createMultipart inside lifecycleScope
            lifecycleScope.launch {
                try {
                    val (filePart, descriptionBody, latBody, lonBody) =
                        createMultipart(currentImageUri!!, description, lat, lon, this@UploadActivity)

                    Log.d("PostActivity", "Lat: $lat, Lon: $lon") // Debug log

                    binding.progressBar.visibility = View.VISIBLE

                    val token = userPreference.getUserToken().first()
                    if (token != null) {
                        val apiService = ApiConfig.getApiService(token)
                        val response = apiService.addStories(
                            "Bearer $token",
                            filePart,
                            descriptionBody,
                            latBody,
                            lonBody
                        )
                        binding.progressBar.visibility = View.GONE
                        if (response.error != true) {
                            Toast.makeText(
                                this@UploadActivity,
                                "Upload successful",
                                Toast.LENGTH_LONG
                            ).show()
                            setResult(Activity.RESULT_OK) // Set the result as OK
                            finish()
                        } else {
                            Toast.makeText(
                                this@UploadActivity,
                                "Upload failed: ${response.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@UploadActivity,
                            "Token not found",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@UploadActivity,
                        "Upload failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("TAG", "Upload error: ${e.message}", e)
                }
            }
        } else {
            Toast.makeText(
                this,
                "Please select an image and enter a description",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private fun getFileSize(uri: Uri): Long {
        return contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            descriptor.statSize
        } ?: 0
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}

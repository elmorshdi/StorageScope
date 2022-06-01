package com.example.storagescope

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {
    private val CAMERA_REQUEST_CODE = 100
    private val STORAGE_REQUEST_CODE = 101

    private lateinit var cameraButton: Button
    private lateinit var storageButton: Button
    private lateinit var cameraPhoto: ImageView

    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var GallaryResultLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //initialization views
        cameraButton = findViewById(R.id.button)
        storageButton = findViewById(R.id.button2)
        cameraPhoto = findViewById(R.id.imageView3)

        cameraButton.setOnClickListener {
            if (hasCameraPermission()){
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraResultLauncher.launch(cameraIntent)

            }else{ requestCameraPermission()
            }
        }
        storageButton.setOnClickListener {
            if (hasStoragePermission()) {
                val galleryIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
                GallaryResultLauncher.launch(galleryIntent)

            } else {
            requestStoragePermission()}
        }


        // this is new way to handle intent instead  of onActivityResult is deprecated now
        //result of open camera
        cameraResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    handleCameraImage(result.data)
                }
            }
        //result for  open gallery
        GallaryResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    handleGalleryImage(result.data)
                }
            }


    }
    //handle Gallery intent data
    private fun handleGalleryImage(data: Intent?) {
        data?.let {
            cameraPhoto.setImageURI(it.data)
        }
    }
    //handle Camera intent data
    private fun handleCameraImage(intent: Intent?) {
        val bitmap = intent?.extras?.get("data") as Bitmap
        cameraPhoto.setImageBitmap(bitmap)
    }
    //check app sdk version && request Storage Permission
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    STORAGE_REQUEST_CODE
                )
            }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        val uri: Uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)

                    } catch (e: Exception) {
                        val intent = Intent()
                        intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                        startActivityIfNeeded(intent, STORAGE_REQUEST_CODE)
                    }
                }
        }
    }
    //requestCameraPermission
    private fun requestCameraPermission() {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && hasCameraPermission()) {
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show()
                    // do your work here
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraResultLauncher.launch(cameraIntent)

                } else if (!shouldShowRequestPermissionRationale(permissions[0]!!)
                ) {
                    // User selected the Never Ask Again Option Change settings in app settings manually
                    dialog(
                        "Change Permissions in Settings",
                        "Click SETTINGS to Manually Set Permissions to use Camera",
                        "SETTINGS",
                        null,
                        ::intentToAppPermission
                    )
                } else {
                    // User selected Deny Dialog to EXIT App ==> OR <== RETRY to have a second chance to Allow Permissions
                    if (!hasCameraPermission()) {
                        dialog(
                            " Permissions is important",
                            "Click RETRY to Set Permissions to Allow\n" +
                                    "Click EXIT to the Close App",
                            "RETRY",
                            "Exit",
                            ::intentToMain
                        )

                    }
                }
            }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && hasStoragePermission()) {
                    Toast.makeText(this, "storage permission granted", Toast.LENGTH_LONG).show()
                    // do your work here
                    val galleryIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
                    GallaryResultLauncher.launch(galleryIntent)
                } else if (!shouldShowRequestPermissionRationale(permissions[0]!!)) {
                    // User selected the Never Ask Again Option Change settings in app settings manually
                    dialog(
                        "Change Permissions in Settings",
                        "Click SETTINGS to Manually Set Permissions to use Database Storage",
                        "SETTINGS",
                        null,
                        ::intentToAppPermission
                    )

                } else {
                    // User selected Deny Dialog to EXIT App ==> OR <== RETRY to have a second chance to Allow Permissions
                    if (!hasStoragePermission()) {
                        dialog(
                            " Permissions is important",
                            "Click RETRY to Set Permissions to Allow\n" +
                                    "Click EXIT to the Close App",
                            "RETRY",
                            "Exit",
                            ::intentToMain
                        )

                    }
                }
            }

        }
    }
    //intent to go app settings
    private fun intentToAppPermission() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts(
            "package",
            packageName, null
        )
        intent.data = uri
        startActivity(intent)
    }
    //intent to go back to main
    private fun intentToMain() {
        val i = Intent(this@MainActivity, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(i)
    }

    //Check if has Storage Permission
    private fun hasStoragePermission() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    //Check if has Camera Permission
    private fun hasCameraPermission() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    //fun to create custom dialog
    private fun dialog(
        title: String, message: String, positiveButton: String,
        negativeButton: String?, positiveFun: () -> Unit
    ) {
        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle(title)
            setMessage(message)
            setPositiveButton(positiveButton) { _, _ ->
                positiveFun()
            }
            setCancelable(false)
            if (!negativeButton.isNullOrBlank()) {
                setNegativeButton(negativeButton) { dialog, _ ->
                    finish()
                    dialog.cancel()
                }
            }
            show()
        }
    }
}
package com.example.storagescope

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    private val CAMERA_REQUEST_CODE = 100
    private val STORAGE_REQUEST_CODE = 101

    private lateinit var cameraButton: Button
    private lateinit var storageButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cameraButton = findViewById<Button>(R.id.button)
        cameraButton.setOnClickListener {
            requestCameraPermission()

        }
        storageButton = findViewById<Button>(R.id.button2)
        storageButton.setOnClickListener {

               requestStoragePermission()
        }
    }
    private fun requestStoragePermission()
    {
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.R){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_REQUEST_CODE)

            }
        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager())
            {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivityForResult(intent, STORAGE_REQUEST_CODE)

                }catch (e:Exception){
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    startActivityIfNeeded(intent, STORAGE_REQUEST_CODE)
                }
            }
        }
        }

    private fun requestCameraPermission()
    {
        if(hasCameraPermission()) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, 33)
        }else{
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE)
            }
        }





    private fun hasStoragePermission() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasCameraPermission() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {if (grantResults.isNotEmpty() && hasCameraPermission()) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show()
                // do your work here
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, 33)

            }
            else if (!shouldShowRequestPermissionRationale(permissions[0]!!)
            ) {
                // User selected the Never Ask Again Option Change settings in app settings manually
                val builder = AlertDialog.Builder(this)
                with(builder)
                {
                    setTitle("Change Permissions in Settings")
                    setMessage("Click SETTINGS to Manually Set Permissions to use Camera")
                    setPositiveButton("SETTINGS") { dialog, which ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts(
                            "package",
                            packageName, null
                        )
                        intent.data = uri
                        startActivityForResult(intent, CAMERA_REQUEST_CODE)
                    }
                    setCancelable(false)
                    show()
                }

            } else {
                // User selected Deny Dialog to EXIT App ==> OR <== RETRY to have a second chance to Allow Permissions
                if (!hasCameraPermission()) {
                    val builder = AlertDialog.Builder(this)
                    with(builder)
                    {
                        setTitle(" Permissions is important")
                        setMessage("Click RETRY to Set Permissions to Allow\n" +
                                "Click EXIT to the Close App")
                        setPositiveButton("RETRY") { dialog, which ->
                            val i = Intent(this@MainActivity, MainActivity::class.java)
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(i)
                        }
                        setCancelable(false)
                        setNegativeButton("Exit"){ dialog, which ->
                            finish()
                            dialog.cancel()
                        }
                        show()
                    }

                }
            }
        }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && hasStoragePermission()) {
                Toast.makeText(this, "storage permission granted", Toast.LENGTH_LONG).show()
                // do your work here

            }
                else if (!shouldShowRequestPermissionRationale(permissions[0]!!)) {
                // User selected the Never Ask Again Option Change settings in app settings manually
                val builder = AlertDialog.Builder(this)
                with(builder)
                {
                    setTitle("Change Permissions in Settings")
                    setMessage("Click SETTINGS to Manually Set Permissions to use Database Storage")
                    setPositiveButton("SETTINGS") { dialog, which ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts(
                            "package",
                            packageName, null
                        )
                        intent.data = uri
                        startActivityForResult(intent, STORAGE_REQUEST_CODE)
                    }
                    setCancelable(false)
                    show()
                }

            }
                else {
                // User selected Deny Dialog to EXIT App ==> OR <== RETRY to have a second chance to Allow Permissions
                if (!hasStoragePermission()) {
                    val builder = AlertDialog.Builder(this)
                    with(builder)
                    {
                        setTitle(" Permissions is important")
                        setMessage("Click RETRY to Set Permissions to Allow\n" +
                                "Click EXIT to the Close App")
                        setPositiveButton("RETRY") { dialog, which ->
                            val i = Intent(this@MainActivity, MainActivity::class.java)
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(i)
                        }
                        setCancelable(false)
                        setNegativeButton("Exit"){ dialog, which ->
                            finish()
                            dialog.cancel()
                        }
                        show()
                    }

                }
            }
            }
        }
    }
}
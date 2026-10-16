package com.rafario.lahrecetah.ui.recipe_form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.app.Activity
import android.content.ActivityNotFoundException
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.rafario.lahrecetah.R
import com.yalantis.ucrop.UCrop
import java.io.File
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeImagePicker(
    onImageSelected: (String) -> Unit,
    content: @Composable (onPickImage: () -> Unit) -> Unit
) {
    var showImagePickerSheet by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        val data = result.data

        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val resultUri = data?.let {
                    UCrop.getOutput(it)
                }

                if (resultUri != null) {
                    onImageSelected(resultUri.toString())
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.crop_result_missing),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            UCrop.RESULT_ERROR -> {
                val error = data?.let {
                    UCrop.getError(it)
                }

                Toast.makeText(
                    context,
                    error?.message ?: context.getString(R.string.crop_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(
            File(
                context.cacheDir,
                "cropped_${System.currentTimeMillis()}.jpg"
            )
        )

        val intent = UCrop.of(
            sourceUri,
            destinationUri
        )
            .withAspectRatio(16f, 9f)
            .getIntent(context)

        cropLauncher.launch(intent)
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            startCrop(uri)
        }
    }

    var cameraUri by rememberSaveable {
        mutableStateOf<Uri?>(null)
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let {
                startCrop(it)
            }
        }
    }

    fun launchCamera() {
        val file = File(
            context.cacheDir,
            "camera_${System.currentTimeMillis()}.jpg"
        )

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        cameraUri = uri

        try {
            takePictureLauncher.launch(uri)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(R.string.camera_unavailable),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.camera_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    content { showImagePickerSheet = true }

    if (showImagePickerSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showImagePickerSheet = false
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 24.dp,
                        horizontal = 16.dp
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        showImagePickerSheet = false

                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            launchCamera()
                        } else {
                            cameraPermissionLauncher.launch(
                                android.Manifest.permission.CAMERA
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = stringResource(R.string.camera),
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(stringResource(R.string.camera))
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        showImagePickerSheet = false
                        pickImageLauncher.launch("image/*")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = stringResource(R.string.gallery),
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(stringResource(R.string.gallery))
                }
            }
        }
    }
}
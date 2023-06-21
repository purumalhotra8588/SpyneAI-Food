import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface.ROTATION_90
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.aspectRatio
import com.spyneai.base.BaseFragment
import com.spyneai.camera2.ShootDimensions
import com.spyneai.databinding.FragmentCameraAppBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureFailureEvent
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.data.model.ShootData
import com.spyneai.shootapp.ui.ImproveShootFragment
import com.spyneai.shootapp.ui.dialogs.CreateProjectAndSkuDialog
import com.spyneai.shootapp.ui.dialogs.CreateSkuDialog
import com.spyneai.shootapp.ui.dialogs.ShootExitDialog
import com.spyneai.shootapp.ui.ecomwithgrid.dialogs.CreateSkuEcomDialog
import com.spyneai.shootapp.ui.ecomwithgrid.dialogs.ProjectTagDialog
import com.spyneai.shootapp.utils.log
import com.spyneai.shootapp.utils.shoot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.roundToInt


class CameraFragmentApp() : BaseFragment<ShootViewModelApp, FragmentCameraAppBinding>(),
    PickiTCallbacks,
    SensorEventListener {
    private var imageCapture: ImageCapture? = null

    private var imageAnalyzer: ImageAnalysis? = null

    private lateinit var cameraExecutor: ExecutorService
    var pickIt: PickiT? = null
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private var flashMode: Int = ImageCapture.FLASH_MODE_OFF

    // Selector showing which camera is selected (front or back)
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    lateinit var file: File
    var haveGyrometer = false
    var isSensorAvaliable = false
    var rotation = 0
    var end: Long = 0
    var begin: Long = 0
    var mid: Long = 0
    var angle = 0
    var upcomingAngle = 0

    var skuCount = 0


    private var pitch = 0.0
    var roll = 0.0
    var azimuth = 0.0

    private lateinit var mSensorManager: SensorManager
    private var mAccelerometer: Sensor? = null
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var cameraControl: CameraControl? = null
    private var cameraInfo: CameraInfo? = null
    private var handler: Handler? = null

    private var filename = ""

    var gravity = FloatArray(3)
    val TAG = "Camera Fragment"


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mSensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
//        binding.tvSkuN!!.text = viewModel.sku!!.skuName

        handler = Handler()

        val activity: Activity? = activity

        handler!!.postDelayed({
            if (activity != null)
                startCamera()
        }, 300)

        cameraExecutor = Executors.newSingleThreadExecutor()
        // Determine the output direcrotory
        pickIt = PickiT(requireContext(), this, requireActivity())


        binding.ivBack?.setOnClickListener {
            ShootExitDialog().show(requireActivity().supportFragmentManager, "CameraFragment")
        }

        if (Utilities.getPreference(
                requireContext(),
                AppConstants.SELECTED_CATEGORY_ID
            ) == AppConstants.ECOM_CATEGORY_ID
        ) {
            binding.llImproveShoot?.visibility = View.VISIBLE
        }



        binding.ivShowImproveShoot?.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .add(R.id.flCamerFragment, ImproveShootFragment())
                .addToBackStack(null)
                .commit()
        }




        viewModel.imageTypeInfo.observe(viewLifecycleOwner) {
            if (it) {
                startCamera()
            }
        }
        viewModel.isSkuNameAdded.observe(viewLifecycleOwner) {
            if (it) {
                binding.llSKuName.isClickable = true
//                viewModel.isSkuNameAdded.value=false
                binding.tvSkuN!!.text = viewModel.skuApp!!.skuName
            }
        }

        refreshText()

        if (viewModel.shootList.value == null) {
            binding.llSKuName!!.isClickable = true
            binding.ivEditSku!!.visibility = View.VISIBLE
        } else {
            binding.llSKuName!!.isClickable = false
            binding.ivEditSku!!.visibility = View.GONE
        }

        viewModel.isProjectNameEdited.observe(viewLifecycleOwner) {
            if (it) {
                binding.llSKuName!!.isClickable = false
                binding.ivEditSku!!.visibility = View.GONE
                binding.tvSkuN!!.text = viewModel.skuApp!!.skuName
            } else {
                binding.llSKuName!!.isClickable = true
                binding.ivEditSku!!.visibility = View.VISIBLE
            }
        }

        if (viewModel.isReshoot) {
            binding.llSKuName!!.isClickable = false
            binding.ivEditSku!!.visibility = View.GONE
            binding.tvSkuN!!.text = viewModel.skuApp!!.skuName
        }

        checkSensor()


        viewModel.showLeveler.observe(viewLifecycleOwner) {
            Log.d(TAG, "onViewCreated: gyro $it")
            if (it && isSensorAvaliable) {
                val cameraSettings =
                    if (viewModel.subcategoryV2?.cameraSettings == null)
                        viewModel.category?.cameraSettingsV2
                    else
                        viewModel.subcategoryV2?.cameraSettings

                binding.flLevelIndicator.visibility = View.VISIBLE
//                Log.d(TAG, "orientation: "+viewModel.category?.orientation!!)
                val s = ""
                viewModel.category?.orientation?.let { orientation ->
                    cameraSettings?.let { a ->
                        binding.flLevelIndicator.start(orientation, a)
                    }
                }
            } else {
                binding.flLevelIndicator.visibility = View.GONE
            }
        }

        viewModel.showGrid.observe(viewLifecycleOwner) {
            if (it) {
                binding.groupGridLines?.visibility = View.VISIBLE
            } else binding.groupGridLines?.visibility = View.INVISIBLE
        }




        binding.llSKuName.setOnClickListener {
            binding.llSKuName.isClickable = false
            if (viewModel.category?.orientation == "portrait") {
                Log.d(TAG, "sku count camera: " + viewModel.skuCount)

                if (viewModel.skuCount > 1) {
                    CreateSkuEcomDialog().show(
                        requireActivity().supportFragmentManager,
                        "Camera Fragment"
                    )
                } else {
                    ProjectTagDialog().show(
                        requireActivity().supportFragmentManager,
                        "Camera Fragment"
                    )
                }

            } else {
                if (viewModel.skuCount > 1) {
                    CreateSkuDialog().show(
                        requireActivity().supportFragmentManager,
                        "Camera Fragment"
                    )
                } else {
                    CreateProjectAndSkuDialog().show(
                        requireActivity().supportFragmentManager,
                        "Camera Fragment"
                    )
                }
            }
        }

        binding.cameraCaptureButton.setOnClickListener {

            onCaptureClick()
            viewModel.pointAngle.value = true
        }

        binding.tvSkipShoot?.setOnClickListener {
            viewModel.skipImage(getString(R.string.app_name))
        }

        viewModel.onVolumeKeyPressed.observe(viewLifecycleOwner) {
            viewModel.skuApp?.let {
                if (it.isSelectAble)
                    onCaptureClick()
            }
        }

        //camera setting
        if (viewModel.category?.orientation == "landscape" || viewModel.category?.orientation == "portrait") {
            viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
            viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
            viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive

            binding.llSetting.setOnClickListener {
                if (viewModel.categoryDetails.value?.imageType == "Info" ||
                    viewModel.categoryDetails.value?.imageType == "Misc" ||
                    viewModel.categoryDetails.value?.imageType == "Interior" ||
                    viewModel.categoryDetails.value?.imageType == "Focus Shoot"
                ) {
                    binding.switchShowOverlay?.isChecked = false
                    binding.switchShowGyro?.isChecked = false
                    binding.switchShowOverlay?.isClickable = false
                    binding.switchShowGyro?.isClickable = false
                } else {
                    binding.switchShowOverlay?.isChecked = viewModel.getCameraSetting().isOverlayActive
                    binding.switchShowGyro?.isChecked = viewModel.getCameraSetting().isGryroActive
                    binding.switchShowGrid?.isChecked = viewModel.getCameraSetting().isGridActive
                    binding.switchShowOverlay?.isClickable = true
                    binding.switchShowGyro.isClickable = true
                }

                if (viewModel.category?.orientation == "portrait" || getString(R.string.app_name) == AppConstants.SWIGGY
                ) {

                    if (viewModel.hasEcomOverlay) {
                        binding.switchShowOverlay?.isClickable = true
                    } else {
                        binding.switchShowOverlay?.isChecked = false
                        binding.switchShowOverlay?.isClickable = false
                    }
                }

                if (viewModel.categoryDetails.value?.imageType == "Info") {
                    binding.switchShowOverlay?.isChecked = false
                    binding.switchShowOverlay?.isClickable = false
                    binding.switchShowGyro?.isChecked = false
                    binding.switchShowGyro?.isClickable = false
                }
                if (binding.ivCross?.visibility == GONE) {
                    binding.ivCross?.visibility = VISIBLE
                    binding.llShowOverlay?.visibility = VISIBLE
                    binding.llShowGrid?.visibility = VISIBLE
                    binding.llShowGyro?.visibility = VISIBLE
                } else {
                    binding.ivCross?.visibility = GONE
                    binding.llShowOverlay?.visibility = GONE
                    binding.llShowGrid?.visibility = GONE
                    binding.llShowGyro?.visibility = GONE
                }
            }

            binding.switchShowGyro?.isChecked = viewModel.getCameraSetting().isGryroActive

            binding.switchShowGyro.setOnCheckedChangeListener { _, isChecked ->
                if (viewModel.startInteriorShots.value != true && viewModel.startMiscShots.value != true) {
                    Utilities.saveBool(
                        requireContext(),
                        viewModel.category?.categoryId + AppConstants.SETTING_STATUS_GYRO,
                        isChecked
                    )
                }
                if (isChecked)
                    viewModel.showLeveler.value = isChecked
                else
                    viewModel.showLeveler.value = false
            }

            binding.switchShowOverlay.isChecked = viewModel.getCameraSetting().isOverlayActive

            binding.switchShowOverlay.setOnCheckedChangeListener { _, isChecked ->
                if (viewModel.startInteriorShots.value != true && viewModel.startMiscShots.value != true) {
                    Utilities.saveBool(
                        requireContext(),
                        viewModel.category?.categoryId + AppConstants.SETTING_STATUS_OVERLAY,
                        isChecked
                    )
                }
                if (isChecked)
                    viewModel.showOverlay.value = isChecked
                else
                    viewModel.showOverlay.value = false

            }

            binding.switchShowGrid?.isChecked = viewModel.getCameraSetting().isGridActive

            binding.switchShowGrid.setOnCheckedChangeListener { _, isChecked ->
                Utilities.saveBool(
                    requireContext(),
                    viewModel.category?.categoryId + AppConstants.SETTING_STATUS_GRID,
                    isChecked
                )
                if (isChecked)
                    viewModel.showGrid.value = isChecked
                else
                    viewModel.showGrid.value = false
            }
        }


    }

    fun checkSensor() {
        val mAccelerometer =
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
                mSensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    SensorManager.SENSOR_DELAY_UI
                )
            }

        val magneticField =
            mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
                mSensorManager.registerListener(
                    this,
                    magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    SensorManager.SENSOR_DELAY_UI
                )
            }

        if (mAccelerometer != null && magneticField != null)
            isSensorAvaliable = true

    }

    override fun onResume() {
        super.onResume()
        shoot("onResume called(camera fragment)")

        checkSensor()
    }

    override fun onDestroy() {
        mSensorManager.unregisterListener(this)
        super.onDestroy()
    }

    private fun onCaptureClick() {
        if (binding.flLevelIndicator.visibility == View.VISIBLE) {
            if (binding.flLevelIndicator.isGyroOnCorrectAngle) {
                captureImage()
            } else {
                showGryroToast()
            }
        } else {
            captureImage()
        }
    }

    private fun showGryroToast() {
        val text = getString(R.string.level_gryometer)
        val centeredText: Spannable = SpannableString(text)
        centeredText.setSpan(
            AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
            0, text.length - 1,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )

        Toast.makeText(requireContext(), centeredText, Toast.LENGTH_LONG).show()
    }

    private fun captureImage() {
        //ThreeSixtyInteriorHintDialog().show(requireActivity().supportFragmentManager,"ThreeSixtyInteriorHintDialog")
        if (viewModel.isCameraButtonClickable) {
            takePhoto()
            log("shoot image button clicked")
            viewModel.isCameraButtonClickable = false
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            val a: Activity? = activity
            if (a != null) a.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }


    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {
        if (isAdded) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
            var cameraProvider: ProcessCameraProvider
            cameraProviderFuture.addListener({

                // Used to bind the lifecycle of cameras to the lifecycle owner
                try {
                    cameraProvider = cameraProviderFuture.get()
                } catch (e: InterruptedException) {
                    Log.d(TAG, "startCamera: " + e.message)
                    Toast.makeText(requireContext(), "Error starting camera", Toast.LENGTH_SHORT)
                        .show()
                    return@addListener
                } catch (e: ExecutionException) {
                    Log.d(TAG, "startCamera: " + e.message)
                    Toast.makeText(requireContext(), "Error starting camera", Toast.LENGTH_SHORT)
                        .show()
                    return@addListener
                }

                // The display information
                //val metrics = DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }
                // The ratio for the output image and preview
                var height = 0
                var width = 0
                val displayMetrics = DisplayMetrics()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (requireContext() != null) {
                        requireContext().display?.getRealMetrics(displayMetrics)
                        height = displayMetrics.heightPixels
                        width = displayMetrics.widthPixels
                    }

                } else {
                    if (requireActivity() != null) {
                        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
                        height = displayMetrics.heightPixels
                        width = displayMetrics.widthPixels
                    }
                }
                val aspectRatio = aspectRatio(width, height, getString(R.string.app_name))
                // The display rotation
                //val rotation = binding.viewFinder.display.rotation

                val localCameraProvider = cameraProvider
                    ?: throw IllegalStateException("Camera initialization failed.")
                var ecomSize = Size(2500, 2500)

                // Preview
                val preview = Preview.Builder()
                            .setTargetResolution(ecomSize)
                            .build()
                            .also {
                                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                            }



                imageCapture =  ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setFlashMode(flashMode)
                    .setTargetResolution(ecomSize)
                    .setTargetRotation(ROTATION_90)
                    .build()

                val useCaseGroup = UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(imageCapture!!)
                    .build()


                // Select back camera as a default
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                try {
                    // Bind use cases to camera
                    val camera = cameraProvider.bindToLifecycle(
                        viewLifecycleOwner,
                        cameraSelector,
                        useCaseGroup
                    )

                    cameraControl = camera.cameraControl

                    cameraInfo = camera.cameraInfo

                    if (viewModel.category?.orientation == "landscape") {
                        var currentZoomRatio = cameraInfo?.zoomState?.value?.zoomRatio ?: 0F
                        cameraControl?.setZoomRatio(currentZoomRatio * 1.3F)
                    }

                    if (viewModel.shootDimensions.value == null ||
                        viewModel.shootDimensions.value?.previewHeight == 0
                    ) {
                        getPreviewDimensions(binding.viewFinder!!, 0)
                    }
                } catch (exc: Exception) {
                    Log.e(TAG, "Use case binding failed", exc)
                    val properties = HashMap<String, Any?>()
                    properties["error"] = exc?.localizedMessage
                    properties["category"] = viewModel.categoryDetails.value?.categoryName

                    BaseApplication.getContext().captureEvent(
                        Events.OVERLAY_CAMERA_FIALED,
                        properties
                    )
                }

            }, ContextCompat.getMainExecutor(requireContext()))

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        shoot("onDestroyView called(overlay fragment)")

        // Shut down our background executor
        cameraExecutor.shutdown()

    }


    private fun takePhoto() {
        begin = System.currentTimeMillis()
        viewModel.begin.value = begin
        // Get a stable reference of the modifiable image capture use case
        val imageCapture1 = imageCapture ?: return


        // Setup image capture metadata
        val metadata = ImageCapture.Metadata().apply {
            // Mirror image when using the front camera
            isReversedHorizontal = lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA
        }

        // The Folder location where all the files will be stored
        val outputDirectory: String by lazy {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "${Environment.DIRECTORY_DCIM}/Spyne/${viewModel.projectApp?.projectName}-${viewModel.skuApp?.projectUuid}/${viewModel.skuApp?.skuName}-${viewModel.skuApp?.uuid}/"
            } else {
                "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/Spyne/${viewModel.projectApp?.projectName}-${viewModel.skuApp?.projectUuid}/${viewModel.skuApp?.skuName}-${viewModel.skuApp?.uuid}/"
            }
        }

        filename = viewModel.getFileName(
            requireActivity().intent.getIntExtra(AppConstants.INTERIOR_SIZE, 0),
            requireActivity().intent.getIntExtra(AppConstants.MISC_SIZE, 0)
        )

        // Options fot the output image file
        val outputOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, outputDirectory)
            }

            val contentResolver = requireContext().contentResolver

            // Create the output uri
            val contentUri =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            ImageCapture.OutputFileOptions.Builder(contentResolver, contentUri, contentValues)
        } else {
            File(outputDirectory).mkdirs()
            file = File(outputDirectory, "${filename}.jpg")

            ImageCapture.OutputFileOptions.Builder(file)
        }.setMetadata(metadata).build()


        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture1.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    viewModel.isCameraButtonClickable = true
                    log("Photo capture failed: " + exc.message)

                    try {
                        Toast.makeText(
                            requireContext(),
                            "Photo capture failed: " + exc.message,
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {

                    }

                    Utilities.hideProgressDialog()

                    BaseApplication.getContext().captureFailureEvent(
                        Events.IMAGE_CAPRURE_FAILED,
                        HashMap<String, Any?>(),
                        exc.localizedMessage
                    )
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    if (output.savedUri == null) {
                        if (file != null)
                            mid = System.currentTimeMillis()
                        val difference = (mid - begin) / 1000.toFloat()
                        log("onImageSaved- " + difference)
                        addShootItem(file.path)
                    } else {
                        try {
                            mid = System.currentTimeMillis()
                            val difference = (mid - begin) / 1000.toFloat()
                            log("onImageSaved2- " + difference)
                            var file = output.savedUri!!.toFile()
                            addShootItem(file.path)
                        } catch (ex: IllegalArgumentException) {
                            pickIt?.getPath(output.savedUri, Build.VERSION.SDK_INT)
                        }
                    }
                }
            })
    }

    override fun onSensorChanged(event: SensorEvent?) {
        //Get Rotation Vector Sensor Values

        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(
                event.values,
                0,
                accelerometerReading,
                0,
                accelerometerReading.size
            )
        } else if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        if (isSensorAvaliable && viewModel.showLeveler.value == true) {
            updateOrientationAngles()
        } else {
            binding.flLevelIndicator.visibility = View.GONE
        }
    }

    fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        // "rotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // "orientationAngles" now has up-to-date information.

        //binding.tvAzimuth.text = "Azimuth ${Math.toDegrees(orientationAngles[0].toDouble())}"

        val movearrow = abs(Math.toDegrees(orientationAngles[2].toDouble()).roundToInt()) - abs(
            roll.roundToInt()
        ) >= 1

        val rotatedarrow =
            abs(Math.toDegrees(orientationAngles[1].toDouble()).roundToInt()) - abs(
                pitch.roundToInt()
            ) >= 1


        pitch = Math.toDegrees(orientationAngles[1].toDouble())
        roll = Math.toDegrees(orientationAngles[2].toDouble())
        azimuth = (orientationAngles[0] * 180 / Math.PI.toFloat()).toDouble()

        binding.tvPitch?.text = "Pitch : " + pitch.roundToInt().toString()
        binding.tvRoll?.text = "Roll : " + roll.roundToInt().toString()

        binding.flLevelIndicator.updateGryoView(
            roll,
            pitch,
            movearrow,
            viewModel.desiredAngle,
            rotatedarrow,
            Utilities.getPreference(requireContext(), AppConstants.SELECTED_CATEGORY_ID)!!,
            viewModel.hasEcomOverlay
        )
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }


    private fun getPreviewDimensions(view: View, type: Int) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                when (type) {
                    0 -> {
                        val shootDimensions = ShootDimensions()
                        shootDimensions.previewWidth = view.width
                        shootDimensions.previewHeight = view.height

                        viewModel.shootDimensions.value = shootDimensions

                        binding.flTapToFocus.init(
                            binding.viewFinder,
                            cameraControl!!,
                            cameraInfo!!,
                            shootDimensions
                        )
                    }
                }

            }
        })
    }

    private fun refreshText() {
        binding.tvSkuN?.text = viewModel.skuApp?.skuName
    }


    private fun addShootItem(capturedImage: String) {
        viewModel.showConfirmReshootDialog.value = true

        if (viewModel.shootList.value == null) {
            Utilities.hideProgressDialog()
            Utilities.hideProgressDialog()
            viewModel.shootList.value = ArrayList()
        }

        var sequenceNumber = viewModel.getSequenceNumber(
            requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE, 0),
            requireActivity().intent.getIntExtra(AppConstants.INTERIOR_SIZE, 0),
            requireActivity().intent.getIntExtra(AppConstants.MISC_SIZE, 0)
        )

        val debugData = JSONObject()
        debugData.put("roll", roll.roundToInt().unaryPlus())
        debugData.put("pitch", pitch.roundToInt().unaryPlus())

        val shootData = ShootData(
            capturedImage,
            viewModel.skuApp?.projectUuid!!,
            viewModel.skuApp?.uuid!!,
            viewModel.categoryDetails.value?.imageType!!,
            Utilities.getPreference(BaseApplication.getContext(), AppConstants.AUTH_KEY).toString(),
            viewModel.overlayId,
            sequenceNumber,
            binding.flLevelIndicator.cameraAngle,
            filename,
            debugData.toString()
        )

        val item = viewModel.shootList.value!!.firstOrNull {
            it.overlayId == viewModel.overlayId
        }


        if (viewModel.isReclick) {
            item?.capturedImage = capturedImage
            item?.angle = binding.flLevelIndicator.cameraAngle
            item?.name = filename
        } else {
            viewModel.shootList.value!!.add(shootData)
        }

        Log.d(TAG, "addShootItem: " + shootData.toProcessAt)

        viewModel.shootList.value = viewModel.shootList.value

        val properties = HashMap<String, Any?>()
//        properties.apply {
//            this["project_id"] = viewModel.projectId.value!!
//            this["sku_id"] = viewModel.sku?.skuId!!
//            this["image_type"] = viewModel.categoryDetails.value?.imageType!!
//        }

        BaseApplication.getContext().captureEvent(Events.IMAGE_CAPTURED, properties)
    }

    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCameraAppBinding.inflate(inflater, container, false)

    override fun PickiTonUriReturned() {

    }


    override fun PickiTonStartListener() {

    }

    override fun PickiTonProgressUpdate(progress: Int) {

    }

    override fun PickiTonCompleteListener(
        path: String?,
        wasDriveFile: Boolean,
        wasUnknownProvider: Boolean,
        wasSuccessful: Boolean,
        Reason: String?
    ) {
        addShootItem(path!!)
    }
}
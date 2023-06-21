package com.spyneai.singleimageprocessing.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.provider.MediaStore
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.camera2.ShootDimensions
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureFailureEvent
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.databinding.FragmentSingleImageCameraBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shootapp.adapters.OverlaysAdapter
import com.spyneai.shootapp.adapters.SubcatAndAngleAdapter
import com.spyneai.shootapp.data.OnOverlaySelectionListener
import com.spyneai.shootapp.utils.log
import com.spyneai.shootapp.utils.shoot
import com.spyneai.singleimageprocessing.data.SingleImageViewModel
import java.io.File
import java.util.HashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.roundToInt

class SingleImageCamera : BaseFragment<SingleImageViewModel, FragmentSingleImageCameraBinding>(),
    OnItemClickListener, OnOverlaySelectionListener, SensorEventListener, PickiTCallbacks {

    val TAG = "SingleImageCamera"

    lateinit var file: File
    private var currentSelected: CatAgnosticResV2.CategoryAgnos.SubCategoryV2.OverlayApp? = null
    private var pitch = 0.0
    var roll = 0.0
    var azimuth = 0.0

    var subcatAndAngleAdapter: SubcatAndAngleAdapter? = null
    var overlaysAdapter: OverlaysAdapter? = null
    private var imageCapture: ImageCapture? = null

    private var imageAnalyzer: ImageAnalysis? = null

    private lateinit var cameraExecutor: ExecutorService
    var pickIt: PickiT? = null
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private var flashMode: Int = ImageCapture.FLASH_MODE_OFF

    // Selector showing which camera is selected (front or back)
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA

    private var cameraControl: CameraControl? = null
    private var cameraInfo: CameraInfo? = null
    private var handler: Handler? = null
    private lateinit var mSensorManager: SensorManager
    private var mAccelerometer: Sensor? = null
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    var isSensorAvaliable = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pickIt = PickiT(requireContext(),this,requireActivity())
        mSensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        handler = Handler(Looper.getMainLooper())

        val activity: Activity? = activity

        handler?.postDelayed({
            if (activity != null)
                startCamera()
        }, 300)

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.tvDescription.text = "Select your ${viewModel.category?.name}'s Sub-categories"

        //select subcategories
        viewModel.category?.let {
            if (it.shootExperience?.hasSubcategories == true)
                getSubcategories()
            else {
                hideViews()
                handler?.postDelayed({
                    viewModel.showLeveler.value = true
                }, 1500)
            }


        }

        viewModel.showLeveler.observe(viewLifecycleOwner) {
            Log.d(TAG, "onViewCreated: gyro $it")
            if (it && isSensorAvaliable) {
                val cameraSettings =
                    if (viewModel.subcategoryV2?.cameraSettings == null) viewModel.category?.cameraSettingsV2 else viewModel.subcategoryV2?.cameraSettings
                binding.flLevelIndicator?.visibility = View.VISIBLE
                binding.flLevelIndicator?.start(
                    viewModel.category?.orientation!!,
                    cameraSettings!!
                )
            } else {
                binding.flLevelIndicator?.visibility = View.GONE
            }
        }

        binding.cameraCaptureButton?.setOnClickListener {
            onCaptureClick()
        }
    }


    private fun onCaptureClick() {
        if (binding.flLevelIndicator?.visibility == View.VISIBLE) {
            if (binding.flLevelIndicator?.isGyroOnCorrectAngle == true) {
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

    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {
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

            val localCameraProvider = cameraProvider
                ?: throw IllegalStateException("Camera initialization failed.")
            var size = Size(1024, 768)
            var automobileResolution = Size(1920, 1080)

            // Preview
            val preview = when (viewModel.category?.orientation) {
                "landscape" -> {
                    if (getString(R.string.app_name) == AppConstants.KARVI) {
                        Preview.Builder()
                            .setTargetResolution(size)
                            .build()
                            .also {
                                it.setSurfaceProvider(binding.viewFinder?.surfaceProvider)
                            }
                    } else {
                        Preview.Builder()
                            .setTargetResolution(automobileResolution)
                            .build()
                            .also {
                                it.setSurfaceProvider(binding.viewFinder?.surfaceProvider)
                            }
                    }
                }
                else -> {
                    Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build()
                        .also {
                            it.setSurfaceProvider(binding.viewFinder?.surfaceProvider)
                        }
                }

            }

            imageCapture = when (viewModel.category?.orientation) {
                "landscape" -> {
                    if (getString(R.string.app_name) == AppConstants.KARVI) {
                        ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .setFlashMode(flashMode)
                            .setTargetResolution(size)
                            .build()
                    } else {
                        ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .setFlashMode(flashMode)
                            .setTargetResolution(automobileResolution)
                            .build()
                    }
                }
                else -> {
                    ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setFlashMode(flashMode)
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .setTargetRotation(Surface.ROTATION_90)
                        .build()
                }

            }

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

                BaseApplication.getContext().captureEvent(
                    Events.OVERLAY_CAMERA_FIALED,
                    properties
                )
            }

        }, ContextCompat.getMainExecutor(requireContext()))

    }

    private fun getSubcategories() {
        viewModel.getSubcategoriesV2(
            Utilities.getPreference(
                requireContext(),
                AppConstants.SELECTED_CATEGORY_ID
            ).toString()
        )?.observe(viewLifecycleOwner) {

            requireContext().captureEvent(
                Events.GET_SUBCATEGORIES,
                HashMap<String, Any?>()
            )

            subcatAndAngleAdapter = SubcatAndAngleAdapter(it, this)

            val lyManager =
                if (viewModel.category?.orientation == "landscape")
                    LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                else
                    LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )


            binding.rvData?.apply {
                layoutManager = lyManager
                adapter = subcatAndAngleAdapter
            }
        }
    }

    override fun getViewModel() = SingleImageViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSingleImageCameraBinding.inflate(inflater, container, false)

    override fun onItemClick(view: View, position: Int, data: Any?) {

        when (data) {
            is CatAgnosticResV2.CategoryAgnos.SubCategoryV2 -> {
                viewModel.subcategoryV2 = data

                //fetch overlays
                subcatAndAngleAdapter?.let {
                    it.listItems = ArrayList()
                    it.notifyDataSetChanged()
                }

                hideViews()

                viewModel.getOrientation()?.observe(viewLifecycleOwner) {
                    if (it == "landscape")
                        fetchOverlays(8)
                    else {
                        if (Utilities.getPreference(
                                requireContext(),
                                AppConstants.SELECTED_CATEGORY_ID
                            ) == AppConstants.ECOM_CATEGORY_ID
                        )
                            viewModel.showLeveler.value = true
                        else
                            fetchOverlays(0)
                    }

                }
            }

            is CatAgnosticResV2.CategoryAgnos.SubCategoryV2.OverlayApp -> {
                //select and show overlay
                currentSelected?.let {
                    it.isSelected = false
                }

                currentSelected = data
                data.isSelected = true

                overlaysAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun hideViews() {
        binding.clRoot?.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.transparent
            )
        )

        binding.apply {
            ivArrow?.visibility = View.GONE
            tvDescription?.visibility = View.INVISIBLE
        }
    }

    private fun fetchOverlays(angle: Int) {
        viewModel.subcategoryV2?.let {
            Utilities.hideProgressDialog()

            val overlaysList = ArrayList<CatAgnosticResV2.CategoryAgnos.SubCategoryV2.OverlayApp>()
            val thumbNailList = ArrayList<String>()

            it.overlayApps?.forEach {
                if (it.angles == angle) {
                    overlaysList.add(it)
                    thumbNailList.add(it.displayThumbnail)
                }
            }

            overlaysList[0].isSelected = true
            viewModel.displayName = it.overlayApps!![0].displayName
            viewModel.displayThumbanil = it.overlayApps!![0].displayThumbnail
            currentSelected = overlaysList[0]

            overlaysAdapter = OverlaysAdapter(
                overlaysList,
                this,
                this
            )
            val lyManager =
                if (viewModel.category?.orientation == "landscape")
                    LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                else
                    LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )

            binding.rvData?.apply {
                visibility = View.VISIBLE
                layoutManager = lyManager
                adapter = overlaysAdapter
            }

            viewModel.showLeveler.value = true
        }
    }

    override fun onOverlaySelected(view: View, position: Int, data: Any?) {
        when (data) {
            is CatAgnosticResV2.CategoryAgnos.SubCategoryV2.OverlayApp -> {
                if (data.frameAngle != "")
                    viewModel.desiredAngle = data.frameAngle.toInt()

                viewModel.displayName = data.displayName
                viewModel.displayThumbanil = data.displayThumbnail
//                viewModel.overlayId = data.id
                loadOverlay(data.displayThumbnail)
            }
        }
    }

    private fun loadOverlay(overlay: String) {

        Glide.with(requireContext())
            .load(overlay)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .skipMemoryCache(false)
            .into(binding.imgOverlay!!)
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

                        binding.flTapToFocus?.init(
                            binding.viewFinder!!,
                            cameraControl!!,
                            cameraInfo!!,
                            shootDimensions
                        )
                    }
                }

            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        shoot("onDestroyView called(overlay fragment)")

        // Shut down our background executor
        cameraExecutor.shutdown()

    }

    override fun onResume() {
        super.onResume()
        shoot("onResume called(camera fragment)")

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

    override fun onSensorChanged(event: SensorEvent?) {
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
            binding.flLevelIndicator?.visibility = View.GONE
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
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


        binding.flLevelIndicator?.updateGryoView(
            roll,
            pitch,
            movearrow,
            viewModel.desiredAngle,
            rotatedarrow,
            Utilities.getPreference(BaseApplication.getContext(),AppConstants.SELECTED_CATEGORY_ID)!!,
            false
        )
    }

    private fun takePhoto() {
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
                "${Environment.DIRECTORY_DCIM}/Spyne/${
                    Utilities.getPreference(
                        requireContext(),
                        AppConstants.DEVICE_ID
                    ).toString()
                }/"
            } else {
                "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/Spyne/${
                    Utilities.getPreference(
                        requireContext(),
                        AppConstants.DEVICE_ID
                    ).toString()
                }/"
            }
        }

        val filename = Utilities.getPreference(requireContext(), AppConstants.DEVICE_ID)
            .toString() + "_" + System.currentTimeMillis().toString()

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
                        addShootItem(file.path)
                    } else {
                        try {
                            var file = output.savedUri!!.toFile()
                            addShootItem(file.path)
                        } catch (ex: IllegalArgumentException) {
                            pickIt?.getPath(output.savedUri, Build.VERSION.SDK_INT)
                        }
                    }
                }
            })
    }

    private fun addShootItem(capturedImage: String) {
        //show confirmation dialog
        val bundle = Bundle().apply {
            putString("captured_image", capturedImage)
            putBoolean(AppConstants.PROCESS_IMAGE, requireActivity().intent.getBooleanExtra(AppConstants.PROCESS_IMAGE,false))
            putString(AppConstants.BACKGROUND_ID, requireActivity().intent.getStringExtra(AppConstants.BACKGROUND_ID).toString())
            putBoolean(AppConstants.FROM_SELECTION, false)
        }

        val fragment = SingleImageConfirmationDialog()
        fragment.arguments = bundle

        fragment.show(requireActivity().supportFragmentManager, "SingleImageConfirmationDialog")

        val properties = HashMap<String, Any?>()
        BaseApplication.getContext().captureEvent(Events.SINGLE_IMAGE_CAPTURED, properties)
    }

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
        path?.let {
            addShootItem(it)
        }
    }
}
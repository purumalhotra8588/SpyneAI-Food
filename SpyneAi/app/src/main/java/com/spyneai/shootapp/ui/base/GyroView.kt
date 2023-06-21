package com.spyneai.shootapp.ui.base

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.dashboard.response.CameraSettings
import com.spyneai.databinding.GyroViewAppBinding
import com.spyneai.needs.AppConstants
import kotlin.math.abs
import kotlin.math.roundToInt

class GyroView : FrameLayout {

    var binding: GyroViewAppBinding
    private var topConstraint = 0
    private var centerPosition = 0
    private var bottomConstraint = 0
    var isGyroOnCorrectAngle = false
    var cameraAngle = 45
    var angle = 0
    var upcomingAngle = 0
    var cameraSettings: CameraSettings? = null
    //var cateoryName: String? = null
    var cateoryOrientation: String? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

        val view = LayoutInflater.from(context).inflate(
            R.layout.gyro_view_app, null
        )

        this.addView(view)

        binding = GyroViewAppBinding.bind(this)
    }

    //Todo: handle start method in record fragment
    fun start(cateoryOrientation: String, cameraSettings: CameraSettings) {
        this.cameraSettings = cameraSettings
        this.cateoryOrientation = cateoryOrientation

//        this.cateoryName?.let {
//            if (cateoryName == "Footwear")
//                binding.tvLevelIndicator.visibility = View.GONE
//            else
//                binding.flLevelIndicator.visibility = View.VISIBLE
//        }

        getPreviewDimensions(binding.ivGryroRing!!, 1)
        getPreviewDimensions(binding.tvCenter!!, 2)
    }

    fun updateGryoView(
        roll: Double,
        pitch: Double,
        movearrow: Boolean,
        desiredAngle: Int = 0,
        rotatedarrow: Boolean,
        categoryId:String,
        hasEcomOverlay:Boolean
    ) {


        when(cateoryOrientation){
            "portrait" -> {
                if(categoryId==AppConstants.FOOD_AND_BEV_CATEGORY_ID
                    || hasEcomOverlay){

                    if (binding.llTiltDown?.visibility == View.VISIBLE)
                        binding.llTiltUp?.visibility = View.INVISIBLE
                    else if (binding.llTiltUp?.visibility == View.VISIBLE)
                        binding.llTiltDown?.visibility = View.INVISIBLE
                    // angle name
                    if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3)&& desiredAngle == 0) {
                        angle = 0
                        binding.tvAngleValue!!.visibility = View.VISIBLE
                        binding.tvAngleRed!!.visibility = View.INVISIBLE
                        binding.llTiltDown?.visibility = View.INVISIBLE
                        binding.llTiltUp?.visibility = View.INVISIBLE
                    }else
                        if (pitch.roundToInt() <= -82 && pitch.roundToInt() >= -88 && desiredAngle == 90) {
                            angle = 90
                            binding.tvAngleValue!!.visibility = View.VISIBLE
                            binding.tvAngleRed!!.visibility = View.INVISIBLE
                            binding.llTiltDown?.visibility = View.INVISIBLE
                            binding.llTiltUp?.visibility = View.INVISIBLE
                        }else
                            if ((pitch.roundToInt() <= -40 && pitch.roundToInt() >= -45) && abs(roll.roundToInt()) < 100 && desiredAngle == 45) {
                                angle = 45
                                binding.tvAngleValue!!.visibility = View.VISIBLE
                                binding.tvAngleRed!!.visibility = View.INVISIBLE
                                binding.llTiltDown?.visibility = View.INVISIBLE
                                binding.llTiltUp?.visibility = View.INVISIBLE
                            }else {
                                binding.tvAngleValue!!.visibility = View.INVISIBLE
                                binding.tvAngleRed!!.visibility = View.VISIBLE

                                if (desiredAngle == 90 && (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -80)){
                                    binding.llTiltUp?.visibility = View.VISIBLE
                                    binding.llTiltDown?.visibility = View.INVISIBLE
                                    binding.lottieDownArrow!!.visibility = View.INVISIBLE
                                    binding.lottieUpArrow!!.visibility = View.INVISIBLE
                                }
                                else if (desiredAngle == 45 && (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -39)){
                                    binding.llTiltUp?.visibility = View.VISIBLE
                                    binding.llTiltDown?.visibility = View.INVISIBLE
                                    binding.lottieDownArrow!!.visibility = View.INVISIBLE
                                    binding.lottieUpArrow!!.visibility = View.INVISIBLE
                                }
                                else if (desiredAngle == 45 && (pitch.roundToInt() <= -46 && pitch.roundToInt() >= -90)){
                                    binding.llTiltDown?.visibility = View.VISIBLE
                                    binding.llTiltUp?.visibility = View.INVISIBLE
                                    binding.lottieDownArrow!!.visibility = View.INVISIBLE
                                    binding.lottieUpArrow!!.visibility = View.INVISIBLE
                                }
                                else if (desiredAngle == 0 && (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -90)){
                                    binding.llTiltDown?.visibility = View.VISIBLE
                                    binding.llTiltUp?.visibility = View.INVISIBLE
                                    binding.lottieDownArrow!!.visibility = View.INVISIBLE
                                    binding.lottieUpArrow!!.visibility = View.INVISIBLE}
                                else if (desiredAngle == 0 && (pitch.roundToInt() >= 0 && pitch.roundToInt() <= 90)){
                                    binding.llTiltUp?.visibility = View.VISIBLE
                                    binding.llTiltDown?.visibility = View.INVISIBLE
                                    binding.lottieDownArrow!!.visibility = View.INVISIBLE
                                    binding.lottieUpArrow!!.visibility = View.INVISIBLE
                                }
                            }
                    if (binding.flLevelIndicator.visibility == View.VISIBLE) {
                        when (angle) {
                            0 -> {
                                binding.tvAngleValue!!.text = "0" + "\u00B0"
                                binding.tvAngleValue!!.visibility = View.VISIBLE
                                binding.groupOverlay!!.visibility = View.GONE
                                binding.ivAppBottomRight.visibility = View.VISIBLE
                                binding.ivAppBottomLeft.visibility = View.VISIBLE
                            }
                            45 -> {
                                binding.tvAngleValue!!.text = "45" + "\u00B0"
                                binding.tvAngleValue!!.visibility = View.VISIBLE
                                binding.groupOverlay!!.visibility = View.GONE
                                binding.ivAppBottomRight.visibility = View.VISIBLE
                                binding.ivAppBottomLeft.visibility = View.VISIBLE
                            }
                            90 -> {
                                binding.tvAngleValue!!.text = "90" + "\u00B0"
                                binding.tvAngleValue!!.visibility = View.VISIBLE


                                if(categoryId!=AppConstants.FOOD_AND_BEV_CATEGORY_ID){
                                    binding.groupOverlay!!.visibility = View.GONE
                                    binding.ivAppBottomRight.visibility = View.VISIBLE
                                    binding.ivAppBottomLeft.visibility = View.VISIBLE

                                }else {
                                    binding.groupOverlay!!.visibility = View.VISIBLE
                                    binding.ivAppBottomRight.visibility = View.GONE
                                    binding.ivAppBottomLeft.visibility = View.GONE
                                }
                            }
                            else -> {
                                binding.tvAngleValue!!.visibility = View.INVISIBLE
                                binding.ivAppBottomRight.visibility = View.VISIBLE
                                binding.ivAppBottomLeft.visibility = View.VISIBLE
                            }
                        }
                    }

                    //hide moving line
                    if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3))
                        binding.tvLevelIndicator.visibility = View.GONE
                    else
                        binding.tvLevelIndicator.visibility = View.VISIBLE


                    when {
                        (((pitch.roundToInt() == 0 || (pitch.roundToInt() <= 3 && pitch.roundToInt() >= -3))
                                && (abs(roll.roundToInt()) <= 3 && abs(roll.roundToInt()) >= -3))&& desiredAngle == 0) -> {
                            cameraSettings?.roll?.let {
                                if (it.contains(0)) {
                                    binding.lottieDownArrow!!.visibility = View.INVISIBLE
                                    binding.lottieUpArrow!!.visibility = View.INVISIBLE
                                    binding.tvUpcomingAngle1!!.visibility = View.INVISIBLE
                                    binding.tvUpcomingAngle2!!.visibility = View.INVISIBLE
                                    binding.tvAngleRed!!.visibility = View.INVISIBLE
                                    isGyroOnCorrectAngle = true

                                    if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3)) {
                                        cameraAngle = 0
                                        gyroMeterOnLevel(false)
                                    }
                                }
                            }
                        }

                        (pitch.roundToInt() <= -82 && pitch.roundToInt() >= -88 && desiredAngle == 90) -> {
                            cameraSettings?.roll?.let {
                                if (it.contains(90)) {
                                    binding.lottieDownArrow!!.visibility = View.INVISIBLE
                                    binding.lottieUpArrow!!.visibility = View.INVISIBLE
                                    binding.tvUpcomingAngle1!!.visibility = View.INVISIBLE
                                    binding.tvUpcomingAngle2!!.visibility = View.INVISIBLE
                                    binding.tvAngleRed!!.visibility = View.INVISIBLE
                                    isGyroOnCorrectAngle = true

                                    cameraAngle = 90
                                    gyroMeterOnLevel(true)
                                }
                            }
                        }

                        (pitch.roundToInt() <= -40 && pitch.roundToInt() >= -45 && desiredAngle == 45) -> {
                            cameraSettings?.roll?.let {
                                if (it.contains(45)) {
                                    binding.lottieDownArrow!!.visibility = View.INVISIBLE
                                    binding.lottieUpArrow!!.visibility = View.INVISIBLE
                                    binding.tvUpcomingAngle1!!.visibility = View.INVISIBLE
                                    binding.tvUpcomingAngle2!!.visibility = View.INVISIBLE
                                    binding.tvAngleRed!!.visibility = View.INVISIBLE
                                    isGyroOnCorrectAngle = true

                                    cameraAngle = 45
                                    gyroMeterOnLevel(false)
                                }
                            }
                        }

                        else -> {
                            if (binding.flLevelIndicator.visibility == View.VISIBLE) {
                                binding.lottieDownArrow!!.visibility = View.VISIBLE
                                binding.lottieUpArrow!!.visibility = View.VISIBLE
                                binding.tvAngleRed!!.visibility = View.VISIBLE
                            }

                            binding.tvAngleValue!!.visibility = View.INVISIBLE
                            binding.groupOverlay!!.visibility = View.GONE
                            binding.tvAngleValue!!.visibility = View.INVISIBLE
                            binding.groupOverlay!!.visibility = View.GONE
                            binding.ivAppBottomRight.visibility = View.VISIBLE
                            binding.ivAppBottomLeft.visibility = View.VISIBLE
                            isGyroOnCorrectAngle = false
                            val gyroAngle = (-pitch.roundToInt())

                            binding.tvAngleRed!!.text = gyroAngle.toString() + "\u00B0"
                            gyroMeterOffLevel()

                            if (movearrow) {
                                if (abs(roll.roundToInt()) < 100) {
                                    moveArrow((pitch + 85).unaryMinus())
                                } else {
                                    moveArrow(pitch + 85)
                                }
                            } else {
                            }

                            if (roll.roundToInt() == 1 || roll.roundToInt() == -1) {
                                if (roll.roundToInt() == 1) {
                                    rotateArrow((pitch + 85).unaryMinus().roundToInt())
                                } else {
                                    rotateArrow((pitch + 85).roundToInt())
                                }
                            }else{}
                        }
                    }

                }
                else{

                    if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3))
                        angle = 0

                    if (pitch.roundToInt() <= -82 && pitch.roundToInt() >= -88)
                        angle = 90

                    if ((pitch.roundToInt() <= -40 && pitch.roundToInt() >= -45) && abs(roll.roundToInt()) < 100)
                        angle = 45

                    if (binding.flLevelIndicator.visibility == View.VISIBLE) {
                        when (angle) {
                            0 -> {
                                binding.tvAngleValue?.visibility = View.VISIBLE
                                binding.tvAngleValue?.text = "0" + "\u00B0"
                                binding.groupOverlay?.visibility = View.GONE
                            }
                            45 -> {
                                binding.tvAngleValue?.visibility = View.VISIBLE
                                binding.tvAngleValue?.text = "45" + "\u00B0"
                                binding.groupOverlay?.visibility = View.GONE
                            }
                            90 -> {
                                binding.tvAngleValue?.visibility = View.VISIBLE
                                binding.tvAngleValue?.text = "90" + "\u00B0"
                                binding.groupOverlay?.visibility = View.GONE
                            }
                            else -> {
                                binding.tvAngleValue?.visibility = View.INVISIBLE
                                binding.groupOverlay?.visibility = View.GONE
                            }
                        }
                    }

                    //hide moving line
                    if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3))
                        binding.tvLevelIndicator.visibility = View.GONE
                    else
                        binding.tvLevelIndicator.visibility = View.VISIBLE


                    when {
                        ((pitch.roundToInt() == 0 || (pitch.roundToInt() <= 3 && pitch.roundToInt() >= -3))
                                && (abs(roll.roundToInt()) <= 3 && abs(roll.roundToInt()) >= -3)) -> {
                            cameraSettings?.roll?.let {
                                if (it.contains(0)) {
                                    binding.lottieDownArrow!!.visibility = View.INVISIBLE
                                    binding.lottieUpArrow!!.visibility = View.INVISIBLE
                                    binding.tvUpcomingAngle1!!.visibility = View.INVISIBLE
                                    binding.tvUpcomingAngle2!!.visibility = View.INVISIBLE
                                    binding.tvAngleRed!!.visibility = View.INVISIBLE
                                    isGyroOnCorrectAngle = true

                                    if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3)) {
                                        cameraAngle = 0
                                        gyroMeterOnLevel(false)
                                    }
                                }
                            }
                        }

                        (pitch.roundToInt() <= -82 && pitch.roundToInt() >= -88) -> {
                            cameraSettings?.roll?.let {
                                if (it.contains(90)) {
                                    binding.lottieDownArrow!!.visibility = View.INVISIBLE
                                    binding.lottieUpArrow!!.visibility = View.INVISIBLE
                                    binding.tvUpcomingAngle1!!.visibility = View.INVISIBLE
                                    binding.tvUpcomingAngle2!!.visibility = View.INVISIBLE
                                    binding.tvAngleRed!!.visibility = View.INVISIBLE
                                    isGyroOnCorrectAngle = true

                                    cameraAngle = 90
                                    gyroMeterOnLevel(true)
                                }
                            }
                        }

                        (pitch.roundToInt() <= -40 && pitch.roundToInt() >= -45) -> {
                            cameraSettings?.roll?.let {
                                if (it.contains(45)) {
                                    binding.lottieDownArrow!!.visibility = View.INVISIBLE
                                    binding.lottieUpArrow!!.visibility = View.INVISIBLE
                                    binding.tvUpcomingAngle1!!.visibility = View.INVISIBLE
                                    binding.tvUpcomingAngle2!!.visibility = View.INVISIBLE
                                    binding.tvAngleRed!!.visibility = View.INVISIBLE
                                    isGyroOnCorrectAngle = true

                                    cameraAngle = 45
                                    gyroMeterOnLevel(false)
                                }
                            }
                        }

                        else -> {
                            if (binding.flLevelIndicator.visibility == View.VISIBLE) {
                                binding.lottieDownArrow!!.visibility = View.VISIBLE
                                binding.lottieUpArrow!!.visibility = View.VISIBLE
                                binding.tvAngleRed!!.visibility = View.VISIBLE
                            }

                            binding.tvAngleValue!!.visibility = View.INVISIBLE
                            binding.groupOverlay!!.visibility = View.GONE
                            binding.tvAngleValue!!.visibility = View.INVISIBLE
                            isGyroOnCorrectAngle = false
                            val gyroAngle = (-pitch.roundToInt())

                            binding.tvAngleRed!!.text = gyroAngle.toString() + "\u00B0"
                            gyroMeterOffLevel()

                            if (movearrow) {
                                if (abs(roll.roundToInt()) < 100) {
                                    moveArrow((pitch + 85).unaryMinus())
                                } else {
                                    moveArrow(pitch + 85)
                                }
                            } else {
                            }

                            if (roll.roundToInt() == 1 || roll.roundToInt() == -1) {
                                if (roll.roundToInt() == 1) {
                                    rotateArrow((pitch + 85).unaryMinus().roundToInt())
                                } else {
                                    rotateArrow((pitch + 85).roundToInt())
                                }
                            }else{}
                        }
                    }
                }

            }
            else -> {
                if(cameraSettings == null){
                    return
                }
                when {
                    roll > cameraSettings?.rollVar?.plus(90)
                        ?.unaryMinus()!! && roll < (cameraSettings?.rollVar?.plus(70))?.unaryMinus()!! -> {
                        cameraSettings?.roll?.let {
                            if (it.contains(90)) {
                                val rollMax =
                                    cameraSettings?.rollVar?.plus(90)?.unaryMinus()
                                val rollMin =
                                    (cameraSettings?.rollVar?.plus(70))?.unaryMinus()

                                val pitchMax = cameraSettings?.pitchVar?.plus(0)
                                val pitchMin =
                                    cameraSettings?.pitchVar?.minus(0)?.unaryMinus()


                                if ((roll >= rollMax!! && roll <= rollMin!!) && (pitch >= pitchMin!! && pitch <= pitchMax!!)) {
                                    gyroMeterOnLevel(true)
                                } else {
                                    gyroMeterOffLevel()

                                    if (movearrow)
                                        moveArrow(roll + 90)

                                    if (rotatedarrow) {
                                        if (pitch > 0) {
                                            rotateArrow(pitch.minus(0).roundToInt())
                                        } else {
                                            rotateArrow(pitch.plus(0).roundToInt())
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        gyroMeterOffLevel()

                        if (movearrow)
                            moveArrow(roll + 90)

                        if (rotatedarrow) {
                            if (pitch > 0) {
                                rotateArrow(pitch.minus(0).roundToInt())
                            } else {
                                rotateArrow(pitch.plus(0).roundToInt())
                            }
                        } else {

                        }
                    }
                }
            }
        }

    }


    private fun gyroMeterOnLevel(removeAnimation: Boolean) {
        isGyroOnCorrectAngle = true
        if (removeAnimation) {
            binding
                .tvLevelIndicator
                ?.animate()
                ?.translationY(0f)
                ?.setInterpolator(AccelerateInterpolator())?.duration = 0
        }

        binding.tvLevelIndicator?.rotation = 0f

        binding.ivAppTopLeft?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_in_level
            )
        )
        binding.ivAppBottomLeft?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_in_level
            )
        )

        binding.ivGryroRing?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_in_level
            )
        )
        binding.tvLevelIndicator?.background = ContextCompat.getDrawable(
            BaseApplication.getContext(),
            R.drawable.bg_gyro_level
        )

        binding.ivAppTopRight?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_in_level
            )
        )
        binding.ivAppBottomRight?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_in_level
            )
        )
    }

    private fun rotateArrow(roundToInt: Int) {
        binding.tvLevelIndicator?.rotation = roundToInt.toFloat()
    }

    private fun moveArrow(newRoll: Double) {
        if (newRoll > 0 && (centerPosition + newRoll) < bottomConstraint) {
            binding
                .tvLevelIndicator
                ?.animate()
                ?.translationY(newRoll.toFloat())
                ?.setInterpolator(AccelerateInterpolator())?.duration = 0
        }

        if (newRoll < 0 && (centerPosition - newRoll) > topConstraint) {
            binding
                .tvLevelIndicator
                ?.animate()
                ?.translationY(newRoll.toFloat())
                ?.setInterpolator(AccelerateInterpolator())?.duration = 0
        }
    }

    private fun gyroMeterOffLevel() {
        isGyroOnCorrectAngle = false
        binding.ivAppTopLeft?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_error_level
            )
        )
        binding.ivAppBottomLeft?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_error_level
            )
        )

        binding.ivGryroRing?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_error_level
            )
        )
        binding.tvLevelIndicator?.background = ContextCompat.getDrawable(
            BaseApplication.getContext(),
            R.drawable.bg_gyro_error
        )

        binding.ivAppTopRight?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_error_level
            )
        )
        binding.ivAppBottomRight?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_error_level
            )
        )
    }

    private fun getPreviewDimensions(view: View, type: Int) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                when (type) {
                    1 -> {
                        topConstraint = view.top
                        bottomConstraint = topConstraint + view.height
                    }

                    2 -> {
                        centerPosition = view.top
                    }
                }
            }
        })
    }
}
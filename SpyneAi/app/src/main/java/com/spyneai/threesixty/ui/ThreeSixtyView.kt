package com.spyneai.threesixty.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.MotionEventCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.spyneai.R
import com.spyneai.databinding.ThreeSixtyViewBinding
import com.spyneai.needs.AppConstants
import com.spyneai.posthog.captureEvent
import com.spyneai.videorecording.model.TSVParams
import kotlin.math.roundToInt

class ThreeSixtyView : ConstraintLayout, View.OnTouchListener {

    private var binding: ThreeSixtyViewBinding? = null
    private val imageList = ArrayList<String>()
    private lateinit var tsvParamFront: TSVParams
    private val myHandler = Handler(Looper.getMainLooper())
    private var retryCount = 0
    private var changeFrameAfter = 8

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        binding = ThreeSixtyViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun init(imageList: ArrayList<String>) {
        if (imageList.isEmpty()) {
            binding?.progressbar?.isVisible = false
            binding?.tvProgress?.isVisible = false
            binding?.tvError?.isVisible = true
            binding?.tvError?.text = if(AppConstants.IS_NERF_AVAILABLE){
                context.resources.getString(R.string.nerf_data_not_available)
            }else{
                context.resources.getString(R.string.non_nerf_data_not_available)
            }
        } else {
            this.imageList.clear()
            this.imageList.addAll(imageList)
            changeFrameAfter(imageList)

            tsvParamFront = TSVParams()

            tsvParamFront.type = 0
            tsvParamFront.framesList = imageList
            tsvParamFront.mImageIndex = imageList.size / 2

            binding?.progressbar?.max = imageList.size
            preLoadFront(0, tsvParamFront)
        }
    }

    private fun changeFrameAfter(imageList: ArrayList<String>) {
        changeFrameAfter = when (imageList.size) {
            36 -> 2
            24 -> 4
            12 -> 8
            8 -> 12
            else -> 12
        }
    }

    private fun preLoadFront(loadCount: Int, tsvParams: TSVParams) {
        if (loadCount < tsvParams.framesList.size) {
            Glide.with(this)
                .load(tsvParams.framesList[loadCount])
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .skipMemoryCache(false)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        context.captureEvent(
                            "Preload Failed",
                            HashMap<String, Any?>().apply {
                                put("index", loadCount)
                                put("url", tsvParams.framesList[loadCount])
                                put("exception", e?.message)
                                put("message", e?.localizedMessage)
                            }
                        )
                        retryCount ++

                        if (retryCount >= 8){
                            retryCount = 0
                            preLoadFront(loadCount.plus(1), tsvParams)
                        }
                        else
                            preLoadFront(loadCount, tsvParams)

                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        var percentage = 0
                        retryCount = 0

                        try {
                            percentage = (loadCount.plus(1).toFloat()
                                .div(tsvParams.framesList.size.toFloat())).times(100)
                                .roundToInt()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            context.captureEvent(
                                "Percentage exception",
                                HashMap<String, Any?>().apply {
                                    put("exception", e.message)
                                }
                            )
                        }

                        binding?.tvProgress?.text = "${percentage}%"

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            binding?.progressbar?.setProgress(loadCount.plus(1), true)
                        else
                            binding?.progressbar?.progress = loadCount.plus(1)


                        if (loadCount < tsvParams.framesList.size.minus(1))
                            preLoadFront(loadCount.plus(1), tsvParams)
                        else {
                            //load front image
                            binding?.ivFront?.let { image ->
                                Glide.with(context)
                                    .load(imageList[tsvParamFront.mImageIndex])
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                                    .skipMemoryCache(false)
                                    .listener(object : RequestListener<Drawable> {
                                        override fun onLoadFailed(
                                            e: GlideException?,
                                            model: Any?,
                                            target: Target<Drawable>?,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            onFrontImageLoad()
                                            return false
                                        }

                                        override fun onResourceReady(
                                            resource: Drawable?,
                                            model: Any?,
                                            target: Target<Drawable>?,
                                            dataSource: DataSource?,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            onFrontImageLoad()
                                            return false
                                        }

                                    })
                                    .into(image)
                            }
                        }
                        return false
                    }
                })
                .dontAnimate()
                .preload()
        } else {
            onFrontImageLoad()
        }
    }

    private fun onFrontImageLoad() {
        binding?.ivFront?.visibility = View.VISIBLE
        binding?.tvProgress?.visibility = View.GONE
        binding?.progressbar?.isVisible = false
        binding?.ivFront?.setOnTouchListener(this@ThreeSixtyView)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val action = MotionEventCompat.getActionMasked(event)

        when (v?.id) {
            R.id.iv_front -> {
                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        event?.let {
                            tsvParamFront.mStartX = it.x.toInt()
                            tsvParamFront.mStartY = it.y.toInt()
                        }

                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        event?.let {
                            tsvParamFront.mEndX = it.x.toInt()
                            tsvParamFront.mEndY = it.y.toInt()
                        }


                        if (tsvParamFront.mEndX - tsvParamFront.mStartX >= changeFrameAfter) {
                            tsvParamFront.mImageIndex++
                            if (tsvParamFront.mImageIndex >= tsvParamFront.framesList.size) tsvParamFront.mImageIndex =
                                0

                            binding?.ivFront?.let { image ->
                                loadImage(tsvParamFront, image)
                            }

                            event?.let {
                                tsvParamFront.mEndX = it.x.toInt()
                                tsvParamFront.mEndY = it.y.toInt()
                            }
                        }
                        if (tsvParamFront.mEndX - tsvParamFront.mStartX <= changeFrameAfter.unaryMinus()) {
                            tsvParamFront.mImageIndex--
                            if (tsvParamFront.mImageIndex < 0) tsvParamFront.mImageIndex =
                                tsvParamFront.framesList.size - 1

                            binding?.ivFront?.let { image ->
                                loadImage(tsvParamFront, image)
                            }

                            event?.let {
                                tsvParamFront.mEndX = it.x.toInt()
                                tsvParamFront.mEndY = it.y.toInt()
                            }
                        }

                        event?.let {
                            tsvParamFront.mStartX = it.x.toInt()
                            tsvParamFront.mStartY = it.y.toInt()
                        }

                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        event?.let {
                            tsvParamFront.mEndX = it.x.toInt()
                            tsvParamFront.mEndY = it.y.toInt()
                        }

                        return true
                    }
                    MotionEvent.ACTION_CANCEL -> return true
                    MotionEvent.ACTION_OUTSIDE -> return true
                }
            }
        }

        return true
    }

    private fun loadImage(tsvParams: TSVParams, imageView: ImageView) {

        myHandler.removeCallbacksAndMessages(null)

        myHandler.postDelayed({

            try {
                val glide = Glide.with(this)
                    .load(tsvParams.framesList[tsvParams.mImageIndex])

                tsvParams.placeholder?.let {
                    glide.placeholder(it)
                }


                glide.listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource?.let {
                            tsvParams.placeholder = it
                        }

                        return false
                    }

                })
                    .dontAnimate()
                    .into(imageView)


                binding?.ivFront?.isVisible = true

            } catch (ex: UninitializedPropertyAccessException) {

            }
        }, 10)
    }
}
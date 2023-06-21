package com.spyneai.output.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.spyneai.app.BaseApplication
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.credits.model.ProjectStatusBody
import com.spyneai.databinding.FragmentOutputBinding
import com.spyneai.gotoHome
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.processedimages.data.ProcessedViewModelApp
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.ui.base.ShootPortraitActivity
import com.spyneai.shootapp.ui.dialogs.ShootExitDialog
import com.spyneai.threesixty.data.ThreeSixtyViewModel
import kotlinx.coroutines.*
import java.util.*

class OutputFragment : BaseFragment<ShootViewModelApp, FragmentOutputBinding>() {

    lateinit var processedViewModelApp: ProcessedViewModelApp
    lateinit var threeSixtyViewModel: ThreeSixtyViewModel
    var skuCount = 0

    private val timer = Timer()
    private var timerTask: TimerTask? = null
    private var secondsRemaining = 5 * 60
    private var showEndShoot = true

    override fun getViewModel() = ShootViewModelApp::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOutputBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startTimer()



        processedViewModelApp =
            ViewModelProvider(requireActivity(), ViewModelProvider.NewInstanceFactory()).get(
                ProcessedViewModelApp::class.java
            )
        threeSixtyViewModel =
            ViewModelProvider(requireActivity(), ViewModelProvider.NewInstanceFactory()).get(
                ThreeSixtyViewModel::class.java
            )

        binding.btNextShoot.isVisible = !threeSixtyViewModel.fromVideo && !requireActivity().intent.getBooleanExtra(AppConstants.FROM_SDK,false)

        if (threeSixtyViewModel.fromVideo) {
            startTimer()
            binding.clProgress.visibility = View.VISIBLE
            binding.btNextShoot.isVisible = false
            binding.btEndShoot.text = "Go to home" + "(10s)"
        } else {
            binding.clProgress.visibility = View.GONE
            binding.btNextShoot.isVisible = true
        }



        fetchSkus()


        lifecycleScope.launch(Dispatchers.IO) {
            //update sku
            viewModel.skuApp?.uuid?.let { skuUuid ->
                viewModel.projectApp?.uuid?.let { projectUuid ->
                    viewModel.setProjectAndSkuData(
                        projectUuid,
                        skuUuid
                    )
                }
            }

            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.totalSkuCaptured.value = viewModel.projectApp?.skuCount.toString()
                viewModel.totalImageCaptured.value = viewModel.projectApp?.imagesCount
                binding.tvTotalShoot.text = viewModel.projectApp?.skuCount.toString()
            }
        }

        listners()


        processedViewModelApp.isImageProcessed.observe(viewLifecycleOwner){
            if(it) {
                binding.clProgress.visibility = View.GONE
            }
        }

    }

    private fun listners() {
        binding.btEndShoot.setOnClickListener {
            val projectIdList: MutableList<String> = arrayListOf()
            viewModel.projectApp?.projectId?.let { projectIdList.add(it) }


            var body = ProjectStatusBody(
                Utilities.getPreference(BaseApplication.getContext(), AppConstants.AUTH_KEY).toString(),
                projectIdList
            )
            projectStatusUpdate(body)
            observeProjectStatusUpdate(projectIdList)
        }

        binding.btNextShoot.setOnClickListener {
            nextSku()
            processedViewModelApp.skuSucessResultCount.value = null
        }

        binding.ivBackGif.setOnClickListener {
            ShootExitDialog().show(requireFragmentManager(), "ShootExitDialog")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun nextSku() {

        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                viewModel.updateTotalFrames()
            }

            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.shootList.value?.clear()
                val intent =  Intent(activity, ShootPortraitActivity::class.java)

                intent.putExtra(AppConstants.PROJECT_UUIID, viewModel.projectApp?.uuid)
                intent.putExtra(
                    AppConstants.CATEGORY_NAME,
                    viewModel.category?.name
                )
                intent.putExtra(
                    AppConstants.CATEGORY_ID,
                    viewModel.category?.categoryId
                )
                startActivity(intent)
            }

        }
    }

    private fun fetchSkus() {

        val uuid = viewModel.skuApp?.uuid

        requireContext().captureEvent(Events.FETCH_OUTPUT_SKU, HashMap<String, Any?>().apply {
            this.put("sku_uuid", viewModel.skuApp?.uuid)
        })

        lifecycleScope.launch(Dispatchers.IO) {
            val list = viewModel.getSkuWithProjectUuid()

            lifecycleScope.launch(Dispatchers.Main) {
                list?.let {
                    skuCount = list.size
                    it.forEach { sku ->
                        if (sku.status == "draft" || sku.status == "Draft")
                            showEndShoot = false

                        val fragment = SkuBeforeAfterFragment()
                        var bundle = Bundle()
                        sku.credits?.total?.credit?.let { credits ->
                            bundle.putInt(
                                "credits",
                                credits
                            )
                        }
                        bundle.putString("sku_id", sku.skuId)
                        bundle.putString("project_uuid", sku.projectUuid)
                        bundle.putString("sku_uuid", sku.uuid)
                        bundle.putString("sku_name", sku.skuName)
                        fragment.arguments = bundle
                        requireActivity().supportFragmentManager.beginTransaction()
                            .add(binding.llContainer.id, fragment, "SkuBeforeAfter Fragment")
                            .commit()
                    }

                    binding.llButtonOutput.isVisible = showEndShoot
                }
            }
        }
    }

    fun projectStatusUpdate(body : ProjectStatusBody){
        Utilities.showProgressDialog(requireContext())
        processedViewModelApp.projectStatusUpdate(body)
    }

   fun observeProjectStatusUpdate(projectIdList:MutableList<String>){
       val properties = HashMap<String,Any?>()
        processedViewModelApp.projectStatusUpdate.observe(viewLifecycleOwner){
            when(it){
                is Resource.Success ->{
                    Utilities.hideProgressDialog()
                    //update project status to ongoing
                    viewModel.projectApp?.let {
                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.updateProjectStatus(it.uuid)
                        }
                    }


                    requireContext().gotoHome()

                    requireContext().captureEvent("PROJECT_STATUS_UPDATED",

                        properties.apply {
                            put("project_id",projectIdList)
                            put("sku_count",viewModel.projectApp?.skuCount)
                            put("response", it.value)
                        }
                    )



                }
                Resource.Loading ->{


                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()

                    requireContext().captureEvent("PROJECT_STATUS_UPDATED_FAIL",
                        properties.apply {
                            put("response",it)
                            put("project_id",viewModel.projectApp?.projectId)
                            put("sku_count",viewModel.projectApp?.skuCount)
                            put("throwable",it.throwable)
                        }
                    )

                }


            }
        }
    }


    //Timer
    private fun startTimer() {
        onStartTimer()

        timerTask = object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    if (secondsRemaining > 0) {
                        onTick(secondsRemaining)
                        secondsRemaining--
                    } else {
                        onComplete()
                        timerTask?.cancel()
                    }
                }
            }
        }

        timer.schedule(timerTask, 0, 1000)
    }

    private fun onStartTimer() {
        binding.tvTimer.text="05:00 mins"
        binding.progressBar.progress=0
        binding.tvPercentage.text="0 %"
    }

    private fun onTick(secondsRemaining: Int) {
        val minutes = secondsRemaining / 60
        val seconds = secondsRemaining % 60
        binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)


        val secondComplete = 300 - secondsRemaining
        val percentage = (secondComplete * 100)/300
        binding.tvPercentage.text="$percentage %"
        binding.progressBar.max=300
        binding.progressBar.progress=secondComplete

    }

    private fun onComplete() {
        binding.progressBar.visibility=View.GONE
        binding.tvPercentage.visibility=View.GONE
        binding.llTimer.visibility=View.GONE
//        binding.tvProcessing.text="This is taking too long, might be internet issue. Please check your internet connection."
    }

    override fun onDestroy() {
        super.onDestroy()
        timerTask?.cancel()
        timer.cancel()
    }
}

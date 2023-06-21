package com.spyneai.orders.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.paging.ExperimentalPagingApi
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentMyOrdersBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.FilterType
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.adapter.OrdersSlideAdapter
import com.spyneai.reshoot.ui.ReshootRequestsActivity
import com.spyneai.setLocale

@ExperimentalPagingApi
class MyOrdersFragment : BaseFragment<MyOrdersViewModel, FragmentMyOrdersBinding>() {

    private var TAG = "MyOrdersFragment"
    var tabId = 0

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    )  = FragmentMyOrdersBinding.inflate(inflater, container, false)


    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireContext().setLocale()


        checkFilterStatus()

        if(Utilities.getBool(requireContext(),AppConstants.NEW_ENTERPRISE_USER,false)){
            binding.llReshootRequest.visibility=View.GONE
        }else{
            binding.llReshootRequest.visibility=View.VISIBLE
        }

        viewModel.viewType.value=Utilities.getBool(BaseApplication.getContext(), "viewTypeGrid" , false)

        if(viewModel.viewType.value==true){
            binding.cvLinear.background.setTint(Color.parseColor("#EBEBEB"))
            binding.cvGrid.background.setTint(ContextCompat.getColor(requireActivity(),R.color.primary_light_dark))
        }else{
            binding.cvGrid.background.setTint(Color.parseColor("#EBEBEB"))
            binding.cvLinear.background.setTint(ContextCompat.getColor(requireActivity(),R.color.primary_light_dark))
        }

        binding.cvGrid.setOnClickListener {
            binding.cvLinear.background.setTint(Color.parseColor("#EBEBEB"))
            binding.cvGrid.background.setTint(ContextCompat.getColor(requireActivity(),R.color.primary_light_dark))
            Utilities.saveBool(requireContext(), "viewTypeGrid", true)
            viewModel.viewType.value=true
        }
        binding.cvLinear.setOnClickListener {
            binding.cvGrid.background.setTint(Color.parseColor("#EBEBEB"))
            binding.cvLinear.background.setTint(ContextCompat.getColor(requireActivity(),R.color.primary_light_dark))
            Utilities.saveBool(requireContext(), "viewTypeGrid", false)
            viewModel.viewType.value=false
        }


        binding.llFilter.setOnClickListener {
            if(binding.flSort.visibility!=View.VISIBLE)
                binding.flSort.visibility=View.VISIBLE
            else    binding.flSort.visibility=View.GONE
        }

        binding.llCategory.setOnClickListener {
            if(binding.cbAutomobile.visibility!=View.VISIBLE) {
                binding.ivCategory.rotation=90f
                binding.cbAutomobile.visibility = View.VISIBLE
                binding.cbFabric.visibility = View.VISIBLE
            }
            else{
                binding.ivCategory.rotation=0f
                binding.cbAutomobile.visibility = View.GONE
                binding.cbFabric.visibility = View.GONE
            }
        }


        binding.llImageRating.setOnClickListener {
            if(binding.ivImageRating.visibility!=View.VISIBLE){
                makeFilterCheck(binding.tvImageRating,binding.ivImageRating,FilterType.IMAGE_RATING)
            }else{
                makeFilterUncheck(binding.tvImageRating,binding.ivImageRating,FilterType.IMAGE_RATING)
            }
        }

        binding.llAiScore.setOnClickListener {
            if(binding.ivAiScore.visibility!=View.VISIBLE){
                makeFilterCheck(binding.tvAiScore,binding.ivAiScore, FilterType.AI_SCORE)
            }else{
                makeFilterUncheck(binding.tvAiScore,binding.ivAiScore,FilterType.AI_SCORE)
            }
        }

        binding.llDate.setOnClickListener {
            if(binding.ivDate.visibility!=View.VISIBLE){
                makeFilterCheck(binding.tvDate,binding.ivDate,FilterType.DATE)
            }else{
                makeFilterUncheck(binding.tvDate,binding.ivDate,FilterType.DATE)
            }
        }

        binding.llAlphabet.setOnClickListener {
            if(binding.ivAlphabet.visibility!=View.VISIBLE){
                makeFilterCheck(binding.tvAlphabet,binding.ivAlphabet,FilterType.APLHABETICAL)
            }else{
                makeFilterUncheck(binding.tvAlphabet,binding.ivAlphabet,FilterType.APLHABETICAL)
            }
        }


        binding.cbAutomobile.isChecked = viewModel.getFilterCheck().isAutomobileSelected

        binding.cbAutomobile.setOnCheckedChangeListener { _, isChecked ->
            Utilities.saveBool(
                requireContext(), "isAutomobileSelected" + AppConstants.FILTER_AUTOMOBILE_SELECTED, isChecked)
            if (isChecked)
                viewModel.categoryOrder.value= listOf("Automobile")
            else
                viewModel.categoryOrder.value = listOf("")
        }

        arguments?.getInt("tab")?.let {
            tabId = it
        }

        binding.backButton.setOnClickListener {
            if(Utilities.getBool(requireContext(),AppConstants.NEW_ENTERPRISE_USER,false))
                requireActivity().onBackPressed()
            else
            viewModel.goToHomeFragment.value=true
        }

        binding.llReshootRequest.setOnClickListener {
            val intent = Intent(requireContext(), ReshootRequestsActivity::class.java)
            this.startActivity(intent)
        }

        setupTabLayout()
    }


    private fun checkFilterStatus(){
        if(viewModel.getFilterCheck().isAiScoreSelected)
            makeFilterCheck(binding.tvAiScore,binding.ivAiScore,FilterType.AI_SCORE)

        if(viewModel.getFilterCheck().isImageRatingSelected)
            makeFilterCheck(binding.tvImageRating,binding.ivImageRating,FilterType.IMAGE_RATING)

        if(viewModel.getFilterCheck().isDateSelected)
            makeFilterCheck(binding.tvDate,binding.ivDate,FilterType.DATE)


        if(viewModel.getFilterCheck().isAlphabeticalSelected)
            makeFilterCheck(binding.tvAlphabet,binding.ivAlphabet,FilterType.APLHABETICAL)


    }


    private fun makeFilterCheck(
        textView: TextView,
        imageView: ImageView,
        type: FilterType
    ) {
        textView.setTypeface(textView.typeface, Typeface.BOLD)
        imageView.visibility=View.VISIBLE

        when(type){
            FilterType.AI_SCORE->{
                Utilities.saveBool(requireContext(), "isAiScoreSelected" + AppConstants.FILTER_AISCORE_SELECTED, true)
                viewModel.aiScoreOrder.value="DESC"
            }
            FilterType.IMAGE_RATING->{
                Utilities.saveBool(requireContext(), "isImageRatingSelected" + AppConstants.FILTER_IMAGE_RATING_SELECTED, true)
                viewModel.imageRatingOrder.value="DESC"
            }
            FilterType.DATE->{
                Utilities.saveBool(requireContext(), "isDateSelected" + AppConstants.FILTER_DATE_SELECTED, true)
                viewModel.dateOrder.value="DESC"
            }
            FilterType.APLHABETICAL->{
                Utilities.saveBool(requireContext(), "isAlphabeticalSelected" + AppConstants.FILTER_ALPHABETICAL_SELECTED, true)
                viewModel.alphabeticOrder.value="DESC"
            }
        }
    }

    private fun makeFilterUncheck(
        textView: TextView,
        imageView: ImageView,
        type: FilterType
    ) {
        textView.setTypeface(null, Typeface.NORMAL)
        imageView.visibility=View.GONE
        when(type){
            FilterType.AI_SCORE->{
                Utilities.saveBool(requireContext(), "isAiScoreSelected" + AppConstants.FILTER_AISCORE_SELECTED, false)
                viewModel.aiScoreOrder.value=""
            }
            FilterType.IMAGE_RATING->{
                Utilities.saveBool(requireContext(), "isImageRatingSelected" + AppConstants.FILTER_IMAGE_RATING_SELECTED, false)
                viewModel.imageRatingOrder.value=""
            }
            FilterType.DATE->{
                Utilities.saveBool(requireContext(), "isDateSelected" + AppConstants.FILTER_DATE_SELECTED, false)
                viewModel.dateOrder.value=""
            }
            FilterType.APLHABETICAL->{
                Utilities.saveBool(requireContext(), "isAlphabeticalSelected" + AppConstants.FILTER_ALPHABETICAL_SELECTED, false)
                viewModel.alphabeticOrder.value=""
            }
        }
    }

    private fun setupTabLayout() {

        binding.viewPager.apply {
            adapter = OrdersSlideAdapter(requireActivity())
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.drafts)
                1 -> tab.text = getString(R.string.ongoing)
                else -> tab.text = getString(R.string.completed)
            }
        }.attach()

        binding.tabLayout.getTabAt(tabId)?.select()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 2){
                    viewModel.moveToZero = true
                    viewModel.updateCompletedProjects.value = true
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }

    override fun getViewModel() = MyOrdersViewModel::class.java


}
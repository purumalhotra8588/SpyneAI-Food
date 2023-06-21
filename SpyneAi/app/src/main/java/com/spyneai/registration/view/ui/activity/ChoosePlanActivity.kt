package com.spyneai.registration.view.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.spyneai.R
import com.spyneai.base.BaseActivity
import com.spyneai.databinding.ActivityChoosePlanBinding
import com.spyneai.needs.Utilities

class ChoosePlanActivity : BaseActivity() {
    lateinit var binding: ActivityChoosePlanBinding
    private var isEnterPrise = false
    private var isStandard = false
    private var selectedType = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChoosePlanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.clEnterprice.setOnClickListener {
            isEnterPrise = true
            isStandard = false
            if (isEnterPrise) {
                binding.btnSubmit.isClickable = true
                binding.btnSubmit.backgroundTintList =
                    resources.getColorStateList(R.color.primary_light_dark)
                binding.btnSubmit.setTextColor(resources.getColor(R.color.white))
                makeSelectedEnterprisePlan()
                isEnterPrise = false
                Utilities.savePrefrence(this, "SELECTED_PLAN", "enterprise")
            }
        }
        binding.clStandard.setOnClickListener {
            isEnterPrise = false
            isStandard = true
            binding.btnSubmit.isClickable = true
            if (isStandard) {
                binding.btnSubmit.backgroundTintList =
                    resources.getColorStateList(R.color.primary_light_dark)
                binding.btnSubmit.setTextColor(resources.getColor(R.color.white))
                makeSelectedStandardPlan()
                isStandard = false
                Utilities.savePrefrence(this, "SELECTED_PLAN", "individual")
            }
        }
        binding.btnSubmit.setOnClickListener {
            startActivity(Intent(this, RegistrationBaseActivity::class.java)
                .putExtra("selectedType", selectedType))
            finishAffinity()
        }
    }

    private fun makeSelectedEnterprisePlan() {
        binding.apply {
            // BLACK FOR ENTERPRISE
            clEnterprice.setBackgroundColor(
                ContextCompat.getColor(
                    this@ChoosePlanActivity,
                    R.color.black
                )
            )
            tvEntOne.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.white))
            tvEntTwo.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.white))
            tvEntThree.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.white))
            tvEntFour.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.white))
            tvEntFive.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.white))
            ivCheck.setBackgroundResource(R.drawable.ic_check_icon_17_16)
            ivCheck2.setBackgroundResource(R.drawable.ic_check_icon_17_16)
            ivCheck3.setBackgroundResource(R.drawable.ic_check_icon_17_16)
            ivCheck4.setBackgroundResource(R.drawable.ic_check_icon_17_16)
            ivCheck5.setBackgroundResource(R.drawable.ic_check_icon_17_16)

            // WHITE FOR STANDARD
            clStandard.setBackgroundColor(
                ContextCompat.getColor(
                    this@ChoosePlanActivity,
                    R.color.white
                )
            )
            tvStdOne.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.black))
            tvStdTwo.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.black))
            tvStdThree.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.black))
            tvStdFour.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.black))
            tvStdFive.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.black))
            ivStandardCheck.setBackgroundResource(R.drawable.ic_check_icon)
            ivStandardCheck2.setBackgroundResource(R.drawable.ic_check_icon)
            ivSTandardCheck3.setBackgroundResource(R.drawable.ic_check_icon)
            ivStandardCheck4.setBackgroundResource(R.drawable.ic_check_icon)
            ivStandardCheck5.setBackgroundResource(R.drawable.ic_check_icon)
        }

    }

    private fun makeSelectedStandardPlan() {
        binding.apply {
            // BLACK FOR Standard
            clStandard.setBackgroundColor(
                ContextCompat.getColor(
                    this@ChoosePlanActivity,
                    R.color.black
                )
            )
            tvStdOne.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.white))
            tvStdTwo.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.white))
            tvStdThree.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.white))
            tvStdFour.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.white))
            tvStdFive.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.white))
            ivStandardCheck.setBackgroundResource(R.drawable.ic_check_icon_17_16)
            ivStandardCheck2.setBackgroundResource(R.drawable.ic_check_icon_17_16)
            ivSTandardCheck3.setBackgroundResource(R.drawable.ic_check_icon_17_16)
            ivStandardCheck4.setBackgroundResource(R.drawable.ic_check_icon_17_16)
            ivStandardCheck5.setBackgroundResource(R.drawable.ic_check_icon_17_16)

            // WHITE FOR STANDARD
            clEnterprice.setBackgroundColor(
                ContextCompat.getColor(
                    this@ChoosePlanActivity,
                    R.color.white
                )
            )
            tvEntOne.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.black))
            tvEntTwo.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.black))
            tvEntThree.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.black))
            tvEntFour.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.black))
            tvEntFive.setTextColor(ContextCompat.getColor(this@ChoosePlanActivity, R.color.black))
            ivStandardCheck.setBackgroundResource(R.drawable.ic_check_icon)
            ivStandardCheck2.setBackgroundResource(R.drawable.ic_check_icon)
            ivSTandardCheck3.setBackgroundResource(R.drawable.ic_check_icon)
            ivStandardCheck4.setBackgroundResource(R.drawable.ic_check_icon)
            ivStandardCheck5.setBackgroundResource(R.drawable.ic_check_icon)
        }

    }

    override fun onConnectionChange(isConnected: Boolean) {

    }
}
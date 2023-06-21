package com.spyneai.credits.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.credits.adapter.TransactionHistoryAdapter
import com.spyneai.credits.adapter.TransactionTypeAdapter
import com.spyneai.credits.model.TransactionHistory
import com.spyneai.credits.model.TransactionType
import com.spyneai.credits.model.TransactionTypeResponse
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentTransactionWalletBinding
import com.spyneai.handleFirstPageError
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.paging.LoaderStateAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


@ExperimentalPagingApi
class TransactionWalletFragment :
    BaseFragment<DashboardViewModel, FragmentTransactionWalletBinding>(),
    OnItemClickListener {

    private val calendarCurrent: Calendar = Calendar.getInstance()
    private val calendarPreviousMonth: Calendar = Calendar.getInstance()
    private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private val outputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var startCalendar: Calendar? = Calendar.getInstance()
    private var endCalendar: Calendar? = Calendar.getInstance()
    private var endTimeMilliSec: Long? = null
    private var startTimeMilliSec: Long? = null
    private var contact_email: String? = ""
    lateinit var transactionHistoryAdapter: TransactionHistoryAdapter
    lateinit var transactionTypeAdapter: TransactionTypeAdapter
    private var transactionTypeList: List<TransactionType>? = null

    private var actionTypeArrayList: ArrayList<String> = arrayListOf("")
    private var actionTypeList: List<String>? = listOf()
    private var emptyList: List<String>? = listOf()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val locale = Locale("en", "IN")
        Locale.setDefault(locale)

        transactionHistoryAdapter = TransactionHistoryAdapter(this@TransactionWalletFragment)

        binding.ivBackGif.setOnClickListener {
            activity?.onBackPressed()
        }

        Utilities.getPreference(requireContext(), AppConstants.USER_NAME)?.let {
            if (it.isNotEmpty())
                binding.tvPlan.text = it
        }

        // transaction load shimmer
        transactionHistoryAdapter.addLoadStateListener { loadState ->
            when {
                transactionHistoryAdapter.itemCount == 0 -> {
                    val error =
                        handleFirstPageError(loadState) { transactionHistoryAdapter.retry() }
                    if (error || loadState.append.endOfPaginationReached) {
                        binding.tvTopDate.visibility = View.GONE
                        binding.llNoTransactionData.visibility = View.VISIBLE
                        stopLoader()
                    }
                }
                transactionHistoryAdapter.itemCount > 0 -> {
                    val date: Date =
                        inputFormat.parse(transactionHistoryAdapter.snapshot().items[0].date) as Date
                    val trimedDate = outputDateFormat.format(date)
                    binding.tvTopDate.visibility = View.VISIBLE
                    binding.llNoTransactionData.visibility = View.GONE
                    binding.tvTopDate.text = trimedDate
                    stopLoader()

                }

                loadState.append.endOfPaginationReached -> stopLoader()

                else -> stopLoader()
            }
        }

        binding.rvTransaction.layoutManager =
            LinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL,
                false
            )

        binding.rvTransaction.adapter = transactionHistoryAdapter

        val loaderStateAdapter = LoaderStateAdapter { transactionHistoryAdapter.retry() }
        binding.rvTransaction.adapter =
            transactionHistoryAdapter.withLoadStateFooter(loaderStateAdapter)

        getSubscriptionPlan()
        observeSubscriptionPlan()

        //Set by-default current Date
        val currentDate = outputDateFormat.format(calendarCurrent.time)
        calendarPreviousMonth.add(Calendar.DATE, -15)
        val lastMonthDate = outputDateFormat.format(calendarPreviousMonth.time)
        binding.tvStartDate.text = lastMonthDate
        binding.tvEndDate.text = currentDate


        //Transaction TYpe Filter List
        val transactionType = resources.openRawResource(R.raw.transaction_type)
            .bufferedReader().use { it.readText() }
        var response = Gson().fromJson(transactionType, TransactionTypeResponse::class.java)
        transactionTypeList = response.data.sortedBy {
            it.priority
        }
        transactionTypeList?.let {
            setTransactionTypeList(it)
        }

        getTransactionHistory()

        // DatePicker
        binding.llStartDate.setOnClickListener {
            showDatePickerDialog()
        }
        binding.llEndDate.setOnClickListener {
            showEndDatePickerDialog()
        }


        // Recy
        binding.rvTransaction.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // Get the LinearLayoutManager from the RecyclerView
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                // Get the position of the topmost visible item in the RecyclerView
                val position = layoutManager!!.findFirstVisibleItemPosition()

                // Check if the position is within the bounds of the PagingData
                if (position >= 0 && position < transactionHistoryAdapter.snapshot().items.size) {
                    val date: Date =
                        inputFormat.parse(transactionHistoryAdapter.snapshot().items[position].date) as Date
                    val trimedDate = outputDateFormat.format(date)
                    binding.tvTopDate.text = trimedDate
                }
            }
        })

        binding.llTransactionTypeFilter.setOnClickListener {
            binding.llTransactionTypeList.isVisible = !binding.llTransactionTypeList.isVisible
        }
    }


    private fun getTransactionHistory() {
//        actionTypeArrayList.clear()
//        actionTypeList=emptyList
//        (transactionTypeAdapter.listItems as List<TransactionType>).forEach {
//            if (it.isSelected) {
//                actionTypeArrayList.add(it.transactionId)
//                actionTypeList=actionTypeArrayList
//            }
//        }
        val selectedTypes = (transactionTypeAdapter.listItems as List<TransactionType>).filter { it.isSelected }
        val selectedTransactionIds = selectedTypes.map { it.transactionId }
        Log.d("TransactionWallet", "actionTypeList: ${selectedTransactionIds.toString()}")

        lifecycleScope.launch {
            viewModel.getTransactionHistory(
                binding.tvStartDate.text.toString(),
                binding.tvEndDate.text.toString(),
                selectedTransactionIds.toString()
            )
                .distinctUntilChanged().collectLatest { pagingData ->
                    transactionHistoryAdapter.submitData(pagingData)
                }
        }
    }


    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val startYear = calendar.get(Calendar.YEAR)
        val startMonth = calendar.get(Calendar.MONTH)
        val startDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            android.R.style.Theme_DeviceDefault_Light_Dialog_Alert,
            { view, year, month, dayOfMonth ->
                startCalendar?.set(year, month, dayOfMonth)
                startTimeMilliSec = startCalendar?.timeInMillis

                // Check whether selected end date must after start date
                if (startTimeMilliSec!! > (endTimeMilliSec ?: 0)) {
                    startCalendar = endCalendar
                    endCalendar?.set(year, month, dayOfMonth)
                    val date =
                        String.format(Locale.getDefault(), "%d-%d-%d", year, month + 1, dayOfMonth)
                    binding.tvEndDate.text = date
                }

                val date =
                    String.format(Locale.getDefault(), "%d-%d-%d", year, month + 1, dayOfMonth)
                binding.tvStartDate.text = date
            },
            startYear,
            startMonth,
            startDay
        )
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }


    private fun showEndDatePickerDialog() {
        val calendar = Calendar.getInstance()
        calendar.time = startCalendar?.time!!
        val endYear = calendar.get(Calendar.YEAR)
        val endMonth = calendar.get(Calendar.MONTH)
        val endDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            android.R.style.Theme_DeviceDefault_Light_Dialog_Alert,
            { view, year, month, dayOfMonth ->
                endCalendar?.set(year, month, dayOfMonth)
                if (endCalendar?.before(startCalendar) == true) {
                    // show an error message
                    Toast.makeText(
                        requireContext(),
                        "End date must be after start date",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // do something with the start and end calendars
                }
                endTimeMilliSec = endCalendar?.timeInMillis
                val date =
                    String.format(Locale.getDefault(), "%d-%d-%d", year, month + 1, dayOfMonth)
                binding.tvEndDate.text = date
                getTransactionHistory()
            },
            endYear,
            endMonth,
            endDay
        )
        datePickerDialog.datePicker.minDate = startCalendar?.timeInMillis!!
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }


    private fun getSubscriptionPlan() {
        Utilities.showProgressDialog(requireContext())
        viewModel.getSubscriptionPlan()
    }


    private fun observeSubscriptionPlan() {
        viewModel.subscriptionPlanRes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    contact_email = it.value.contactDetails.emailId
                    if (it.value.status != 404) {
                        binding.clSubscriptionPlan.visibility = View.VISIBLE

                        val paid_amount = it.value.invoices[0].paid_amount
                        val n: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
                        binding.tvPlanCredit.text = n.format(paid_amount / 100.0)

                        try {
                            val start_date =
                                outputDateFormat.format(Date(it.value.next_payment_details.data.start_at * 1000L)) // this never ends while debugging
                            val end_date =
                                outputDateFormat.format(Date(it.value.next_payment_details.data.end_at * 1000L)) // this never ends while debugging
                            binding.tvPlanRange.text = " : billed on $start_date - till $end_date"
                            binding.tvPlanState.text = it.value.next_payment_details.data.status
                        } catch (e: Exception) {
                        }

                        if (it.value.next_payment_details.data.status == "active") {
                            binding.tvPlanState.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.wappcolor
                                )
                            )
                            binding.btContactUs.visibility = View.GONE
                        } else {
                            binding.tvPlanState.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red
                                )
                            )
                            binding.btContactUs.visibility = View.VISIBLE
                        }
                    }else{
                        binding.tvPlanCredit.text= "Available Credits\n"+Utilities.getPreference(requireContext(),AppConstants.CREDITS_USER)
                    }
                }
                Resource.Loading -> {}
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { getSubscriptionPlan() }
                }
            }
        }
    }


    private fun setTransactionTypeList(transactionTypeList: List<TransactionType>) {
        transactionTypeAdapter =
            TransactionTypeAdapter(transactionTypeList, listener = this@TransactionWalletFragment)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvTransactionType.layoutManager = layoutManager
        binding.rvTransactionType.adapter = transactionTypeAdapter

    }


    private fun stopLoader() {
        binding.shimmerTransaction.stopShimmer()
        binding.shimmerTransaction.visibility = View.GONE
        binding.rvTransaction.visibility = View.VISIBLE
    }


    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTransactionWalletBinding.inflate(inflater, container, false)

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when (data) {
            is TransactionType -> {
                data.isSelected = !data.isSelected
                getTransactionHistory()
                transactionTypeAdapter.notifyItemChanged(position)
                binding.llTransactionTypeList.visibility = View.GONE
            }
            is TransactionHistory -> {

            }
        }
    }

}
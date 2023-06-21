package com.spyneai.dashboard.ui


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.spyneai.*
import com.spyneai.activity.SearchCategoryActivity
import com.spyneai.app.BaseApplication
import com.spyneai.base.BaseActivity
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.response.CatAgnosticResV2
import com.spyneai.databinding.ActivityDashboardMainBinding
import com.spyneai.draft.data.DraftViewModel
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.MyOrdersActivity
import com.spyneai.orders.ui.fragment.MyOrdersFragment
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.registration.view.ui.activity.InvitationAddFragment
import com.spyneai.registration.view.ui.activity.InvitationFragment
import com.spyneai.registration.view.ui.activity.PreferenceFragmentV2
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.ui.base.ShootPortraitActivity
import com.spyneai.shootapp.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalPagingApi::class)
class MainDashboardActivity : BaseActivity() {
    override var TAG = MainDashboardActivity::class.java.simpleName
    private lateinit var binding: ActivityDashboardMainBinding
    private var viewModel: DashboardViewModel? = null
    private var shootViewModelApp: ShootViewModelApp? = null
    private lateinit var myOrderViewModel: MyOrdersViewModel
    private var draftViewModel: DraftViewModel? = null
    lateinit var appUpdateManager: AppUpdateManager
    private val MY_REQUEST_CODE: Int = 1
    private var tab_id = ""
    var filePath = ""
    var firstFragment: HomeV2Fragment? = HomeV2Fragment()
    var myOrdersFragment: MyOrdersFragment? = MyOrdersFragment()

    @OptIn(ExperimentalPagingApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        viewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        shootViewModelApp = ViewModelProvider(this).get(ShootViewModelApp::class.java)
        myOrderViewModel = ViewModelProvider(this).get(MyOrdersViewModel::class.java)
        draftViewModel = ViewModelProvider(this).get(DraftViewModel::class.java)
        appUpdateManager = AppUpdateManagerFactory.create(this)

       // firstFragment = HomeV2Fragment()
       // myOrdersFragment = MyOrdersFragment()

        viewModel?.refreshProjectData?.observe(this) {
            it?.let { myOrderViewModel.refreshProjectData.value = it }
        }
        viewModel?.fabClickable?.value = false

        tab_id = intent.getStringExtra("SOURCE").toString()

        setLocale()
        listeners()

        if (Utilities.getBool(this, AppConstants.COACH_MARKS, true))

        //send data to server
            if (!Utilities.getBool(this, AppConstants.IS_SKU_DATA_SENT, false)) {
                //send files data
                BaseApplication.getContext().captureEvent(
                    Events.SKU_DATA_NOT_SENT,
                    HashMap<String, Any?>()
                )
            }

        if (intent.getBooleanExtra("show_ongoing", false)) {
            val intent = Intent(this, MyOrdersActivity::class.java)
            intent.putExtra("TAB_ID", 1)
            startActivity(intent)
        }

        binding.bottomNavigation.background = null
        

        viewModel!!.startHereVisible.observe(this) {
            if (it) {
                binding.cvStartHere.visibility = View.VISIBLE
            } else {
                binding.cvStartHere.visibility = View.GONE
            }
        }


        viewModel!!.showNavigation.observe(
            this
        ) {
            if (it) {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                    )
                    .addToBackStack("pref fragment")
                    .add(R.id.flContainer, PreferenceFragmentV2())
                    .commit()
            }

        }

        viewModel!!.prefrencefrag.observe(this) {
            if (it) {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                    )
                    .addToBackStack("pref fragment")
                    .add(R.id.flContainer, PreferenceFragment())
                    .commit()
            }

        }

        viewModel!!.gotoMyOrderFragmentNew.observe(this) {
            if (it) {
                showProjectFragment()
            }
        }

        viewModel?.gotoSearchFragment?.observe(this) {
            if (it) {
                startActivity(Intent(this, SearchCategoryActivity::class.java))
            }
        }

        myOrderViewModel.goToHomeFragment.observe(this) {
            if (it) {
                binding.bottomNavigation.selectedItemId = R.id.homeDashboardFragment
                showHomeFragment()
            }
        }

        viewModel?.replacemanage?.observe(this) {
            if (it) {

                val fragment: Fragment = HomeV2Fragment()

                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                    )
                    .replace(binding.flContainer.id, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }


        viewModel?.replacemanagepref?.observe(this) {
            if (it) {

                val fragment: Fragment = InvitationFragment()

                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                    )
                    .replace(binding.flContainer.id, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        viewModel?.replacemanageAdd?.observe(this) {
            if (it) {

                val fragment: Fragment = InvitationAddFragment()

                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                    )
                    .replace(binding.flContainer.id, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }


        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homeDashboardFragment -> {
                    showHomeFragment()
                }

                R.id.completedOrdersFragment -> {
                    showProjectFragment()
                }
            }
            true
        }


        Log.d(TAG, "onCreate: $tab_id")
        
        if (tab_id == "1") {
            binding.bottomNavigation.selectedItemId = R.id.completedOrdersFragment
            binding.fab.isClickable = true
            binding.fab.enable(true)
        } else {
            binding.bottomNavigation.selectedItemId = R.id.homeDashboardFragment
        }

        if (intent.getBooleanExtra(AppConstants.IS_NEW_USER, false)) {
            viewModel!!.isNewUser.value = intent.getBooleanExtra(AppConstants.IS_NEW_USER, false)
            viewModel!!.creditsMessage.value = intent.getStringExtra(AppConstants.CREDITS_MESSAGE)
        }

        checkAutoUpdate()

        viewModel?.continueAnyway?.observe(this) {
            if (it) {
                continueShoot()
            }
        }

        viewModel?.fabClickable?.observe(this) {
            if (it) {
                binding.fab.isClickable = true
                binding.fab.enable(true)
            } else {
                if (tab_id != "1") {
                    binding.fab.isClickable = false
                    binding.fab.enable(false)
                }
            }
        }

        viewModel?.gotoMyOrderFragment?.observe(this) {
            if (it) {
                showProjectFragment()
            }
        }

    }

    private fun showHomeFragment() {
        Log.d(TAG, "showHomeFragment: ")
        firstFragment?.let { home ->
            myOrdersFragment?.let { projects ->
                Log.d(TAG, "showHomeFragment: ${home.isAdded}")
                if (home.isAdded){
                    supportFragmentManager.beginTransaction()
                        .show(home)
                        .hide(projects)
                        .commit()
                }else{
                    supportFragmentManager.beginTransaction().apply {
                        add(binding.flContainer.id, home)
                            .addToBackStack("Home")
                        commit()
                    }
                }
            }
        }
    }

    private fun showProjectFragment() {
        firstFragment?.let { home ->
            myOrdersFragment?.let { projects ->
                val bundle = Bundle()
                if (tab_id == "1")
                    bundle.putInt("tab", 2)
                else
                    bundle.putInt("tab", 0)

                projects.arguments = bundle

                if (projects.isAdded){
                    supportFragmentManager.beginTransaction()
                        .hide(home)
                        .show(projects)
                        .commit()
                }else {
                    supportFragmentManager.beginTransaction().apply {
                        add(binding.flContainer.id, projects)
                            .hide(home)
                            .addToBackStack("Project")
                        commit()
                    }
                }
            }
        }
    }

    private fun listeners() {
        var item: CatAgnosticResV2.CategoryAgnos? = null
        binding.fab.setOnClickListener {
            viewModel?.CategoryAgnosData?.value.let {
                it
                item = it
                if (viewModel?.startHereVisible?.value != true) {
                    viewModel?.startHereVisible?.value = true
                    binding.fab.rotation = 45f
                } else {
                    viewModel?.startHereVisible?.value = false
                    binding.fab.rotation = 0f
                }
            }

            val properties = HashMap<String, Any?>().apply {
                put(
                    "auth_key",
                    Utilities.getPreference(this@MainDashboardActivity, AppConstants.AUTH_KEY)
                )
            }
            captureEvent(
                Events.FAB_CLICKED,
                properties
            )
        }




        binding.ivProductShoot.setOnClickListener {
            viewModel?.CategoryAgnosData?.value.let {
                it

                lifecycleScope.launch {
                    if (it == null) {

                        //get value from db
                        val data = withContext(Dispatchers.IO) {
                            Utilities.getPreference(
                                this@MainDashboardActivity,
                                AppConstants.SELECTED_CATEGORY_ID
                            )
                                ?.let { it1 -> viewModel?.getCategoryAgnosData(it1) }
                        }
                        item = data

                    } else {
                        item = it
                    }

                    if (item?.isActive == true) {
                        val intent =
                            Intent(this@MainDashboardActivity, ShootPortraitActivity::class.java)
                        intent.apply {
                            putExtra(AppConstants.SHOOT_TYPE, "shoot_flow")
                            putExtra(AppConstants.CATEGORY_ID, item!!.categoryId)
                            putExtra(AppConstants.ORIENTATION, item!!.orientation)
                            putExtra(AppConstants.CATEGORY_NAME, item!!.name)
                            putExtra(AppConstants.IMAGE_URL, item!!.displayThumbnail)
                            putExtra(AppConstants.DESCRIPTION, item!!.description)
                            putExtra(AppConstants.COLOR, item!!.colorCode)
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@MainDashboardActivity,
                            "Coming Soon !",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    viewModel?.startHereVisible?.value = false
                    binding.fab.rotation = 0f
                }


            }

        }


    }

    override fun onConnectionChange(isConnected: Boolean) {
        showConnectionChangeView(isConnected, binding.root)
    }

    private fun continueShoot() {

    }

    private fun checkAutoUpdate() {
        if (BuildConfig.VERSION_NAME.contains("debug")) {
            if (allPermissionsGranted()) {
                onPermissionGranted()
            } else {
                permissionRequest.launch(permissions.toTypedArray())
            }
        } else {
            autoUpdatePlayStore()
        }
    }

    private fun autoUpdatePlayStore() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {

                // Request the update.
                appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    MY_REQUEST_CODE
                )
            }
        }

        if (allPermissionsGranted()) {
            onPermissionGranted()
        } else {
            permissionRequest.launch(permissions.toTypedArray())
        }
    }


    protected fun allPermissionsGranted() = permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private val permissions = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    ).apply {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }
    }

    private val permissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            val requiredPermissions = if (getString(R.string.app_name) == AppConstants.OLA_CABS) {
                permissions
            } else {
                permissions.filter {
                    it.key != Manifest.permission.ACCESS_COARSE_LOCATION
                }
            }

            if (requiredPermissions.all {
                    it.value
                }) {
                onPermissionGranted()
            } else {
//                RequiredPermissionDialog().show(supportFragmentManager, "RequiredPermissionDialog")
            }

        }

    private fun capture(eventName: String) {
        val properties = HashMap<String, Any?>()
        properties.apply {
            this["email"] =
                Utilities.getPreference(this@MainDashboardActivity, AppConstants.EMAIL_ID)
                    .toString()
        }

        captureEvent(
            eventName,
            properties
        )
    }


    open fun onPermissionGranted() {
        val properties = HashMap<String, Any?>()
            .apply {
                put("service_state", "Started")
                put(
                    "email",
                    Utilities.getPreference(this@MainDashboardActivity, AppConstants.EMAIL_ID)
                        .toString()
                )
            }

        captureEvent("ALL PERMISSIONS GRANTED", properties)
        checkPendingDataSync(MainDashboardActivity::class.java.simpleName)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != AppCompatActivity.RESULT_OK) {
                moveTaskToBack(true)
                finish()
                Toast.makeText(
                    this,
                    "Update flow failed!$requestCode",
                    Toast.LENGTH_SHORT
                ).show()

                log("MY_APP\", \"Update flow failed! Result code: $resultCode")
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (tab_id == "1") {
            binding.bottomNavigation.selectedItemId = R.id.completedOrdersFragment
        }
        tab_id = ""
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        MY_REQUEST_CODE
                    )
                }
            }
    }


    override fun onBackPressed() {
        if (binding.bottomNavigation.selectedItemId == R.id.completedOrdersFragment) {
            myOrderViewModel.goToHomeFragment.value = true
        } else
            super.onBackPressed()
    }
}

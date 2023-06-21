package com.spyneai.dashboard.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.repository.model.CheckInOutRes
import com.spyneai.dashboard.response.CategoryAgnosticResponse
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Exception
import javax.inject.Inject

class DashboardRemoteRepo @Inject constructor(private val webApi: ClipperApi) : BaseRepository(){

    private var clipperApi = ClipperApiClient().getClient()

    var _checkInOutRes: MutableLiveData<Resource<CheckInOutRes>> = MutableLiveData()
    val checkInOutRes: LiveData<Resource<CheckInOutRes>>
        get() = _checkInOutRes


}
package com.tlrm.mobile.whapp.mvvm.startup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlrm.mobile.whapp.services.UserService
import com.tlrm.mobile.whapp.util.LoadingState
import kotlinx.coroutines.launch

class StartupViewModel(private val userService: UserService): ViewModel() {

    private val _loadingState = MutableLiveData<LoadingState>()

    val loadingState: LiveData<LoadingState>
        get() = _loadingState

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            try {
                _loadingState.value = LoadingState.loading("Syncing user data from server")
                userService.refresh()
                _loadingState.value = LoadingState.LOADED
            } catch (e: Exception) {
                _loadingState.value = LoadingState.error("Unable to sync the user data from server")
            }
        }
    }

}
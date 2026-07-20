package com.example.hytp.feature.address.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hytp.core.data.AddressRepository
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.dto.Address
import com.example.hytp.core.network.dto.AddressRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddressUiState(
    val loading: Boolean = false,
    val list: List<Address> = emptyList(),
    val error: String? = null,
    val saving: Boolean = false,
)

/**
 * 收货地址：列表 + 新建。选择地址用于结算页回传。
 */
@HiltViewModel
class AddressViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressUiState())
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = addressRepository.list()) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(loading = false, list = r.data.list) }
                is ApiResult.Error ->
                    _uiState.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(loading = false, error = "网络异常，请重试") }
            }
        }
    }

    /** 新建地址；成功回调返回新地址。 */
    fun create(req: AddressRequest, onDone: (Address) -> Unit) {
        _uiState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            when (val r = addressRepository.create(req)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(saving = false) }
                    load()
                    onDone(r.data)
                }
                is ApiResult.Error ->
                    _uiState.update { it.copy(saving = false, error = r.message) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(saving = false, error = "网络异常，请重试") }
            }
        }
    }

    fun remove(id: Long) {
        viewModelScope.launch {
            when (addressRepository.remove(id)) {
                is ApiResult.Success ->
                    _uiState.update { s -> s.copy(list = s.list.filterNot { it.id == id }) }
                else -> load()
            }
        }
    }
}

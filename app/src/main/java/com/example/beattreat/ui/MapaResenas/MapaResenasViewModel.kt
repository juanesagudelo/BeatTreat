package com.example.beattreat.ui.MapaResenas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beattreat.data.repository.MapaResenasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapaResenasViewModel @Inject constructor(
    private val repository: MapaResenasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapaResenasUIState())
    val uiState: StateFlow<MapaResenasUIState> = _uiState.asStateFlow()

    init {
        cargarResenas()
    }

    fun cargarResenas() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.getResenasUltimas24Horas()
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        resenas   = result.getOrDefault(emptyList()),
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading    = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
}

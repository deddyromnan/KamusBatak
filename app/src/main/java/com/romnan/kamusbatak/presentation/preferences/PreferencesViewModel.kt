package com.romnan.kamusbatak.presentation.preferences

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romnan.kamusbatak.R
import com.romnan.kamusbatak.domain.model.ThemeMode
import com.romnan.kamusbatak.domain.repository.DictionaryRepository
import com.romnan.kamusbatak.domain.repository.PreferencesRepository
import com.romnan.kamusbatak.domain.util.Resource
import com.romnan.kamusbatak.domain.util.UIText
import com.romnan.kamusbatak.presentation.util.UIEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _state = mutableStateOf(PreferencesScreenState.defaultValue)
    val state: State<PreferencesScreenState> = _state

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        getLastUpdated()
        getCurrentThemeMode()
    }

    private var getCurrentThemeModeJob: Job? = null
    private fun getCurrentThemeMode() {
        getCurrentThemeModeJob?.cancel()
        getCurrentThemeModeJob = viewModelScope.launch {
            preferencesRepository.themeMode.onEach { themeMode ->
                _state.value = state.value.copy(currentThemeMode = themeMode)
            }.launchIn(this)
        }
    }

    private var getLastUpdatedJob: Job? = null
    private fun getLastUpdated() {
        getLastUpdatedJob?.cancel()
        getLastUpdatedJob = viewModelScope.launch {
            dictionaryRepository.localDbLastUpdatedAt.onEach { timeMillis ->
                _state.value = state.value.copy(localDbLastUpdatedAt = timeMillis)
            }.launchIn(this)
        }
    }

    private var onUpdateLocalDbJob: Job? = null
    fun onUpdateLocalDb() {
        onUpdateLocalDbJob?.cancel()
        onUpdateLocalDbJob = viewModelScope.launch {
            dictionaryRepository.updateLocalDb().onEach { result ->
                when (result) {
                    is Resource.Success -> _state.value =
                        state.value.copy(isUpdatingLocalDb = false)
                    is Resource.Loading -> _state.value = state.value.copy(isUpdatingLocalDb = true)
                    is Resource.Error -> {
                        _state.value = state.value.copy(isUpdatingLocalDb = false)
                        _eventFlow.emit(
                            UIEvent.ShowSnackbar(
                                result.uiText ?: UIText.StringResource(R.string.em_unknown)
                            )
                        )
                    }
                }
            }.launchIn(this)
        }
    }

    private var onThemeModeChosenJob: Job? = null
    fun onThemeModeChosen(themeMode: ThemeMode) {
        onThemeModeChosenJob?.cancel()
        onThemeModeDialogVisibilityChange(visible = false)
        onThemeModeChosenJob = viewModelScope.launch {
            preferencesRepository.setThemeMode(themeMode)
        }
    }

    fun onThemeModeDialogVisibilityChange(visible: Boolean) {
        _state.value = state.value.copy(isThemeModeDialogVisible = visible)
    }
}
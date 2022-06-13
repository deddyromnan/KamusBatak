package com.romnan.kamusbatak.featEntriesFinder.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romnan.kamusbatak.R
import com.romnan.kamusbatak.core.domain.model.Entry
import com.romnan.kamusbatak.core.domain.repository.OfflineSupportRepository
import com.romnan.kamusbatak.core.presentation.util.UIEvent
import com.romnan.kamusbatak.core.util.Constants
import com.romnan.kamusbatak.core.util.Resource
import com.romnan.kamusbatak.core.util.UIText
import com.romnan.kamusbatak.featEntriesFinder.domain.repository.EntriesFinderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import logcat.logcat
import javax.inject.Inject

@HiltViewModel
class EntriesFinderViewModel @Inject constructor(
    private val repository: EntriesFinderRepository,
    private val offlineSupportRepository: OfflineSupportRepository
) : ViewModel() {

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _entries = mutableStateOf(emptyList<Entry>())
    val entries: State<List<Entry>> = _entries

    private val _state = mutableStateOf(EntriesFinderState.defaultValue)
    val state: State<EntriesFinderState> = _state

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        downloadUpdate()
    }

    fun onSwapLanguages() {
        _state.value = state.value.copy(
            sourceLanguage = state.value.targetLanguage,
            targetLanguage = state.value.sourceLanguage
        )
        fetchEntries()
    }

    fun onQueryChange(value: String) {
        _searchQuery.value = value
        fetchEntries()
    }

    fun onOpenOptionsMenu() {
        _state.value = state.value.copy(isOptionsMenuVisible = true)
    }

    fun onCloseOptionsMenu() {
        _state.value = state.value.copy(isOptionsMenuVisible = false)
    }

    private var downloadUpdateJob: Job? = null
    private fun downloadUpdate() {
        downloadUpdateJob?.cancel()
        downloadUpdateJob = viewModelScope.launch {
            offlineSupportRepository
                .downloadUpdate()
                .onEach { result ->
                    logcat { "downloadUpdate ${result::class.java.simpleName}" }
                    if (result is Resource.Error) {
                        delay(Constants.DURATION_DOWNLOAD_UPDATE_COOLDOWN)
                        downloadUpdate()
                    }
                }
                .launchIn(this)
        }
    }

    private var fetchEntriesJob: Job? = null
    private fun fetchEntries() {
        fetchEntriesJob?.cancel()
        fetchEntriesJob = viewModelScope.launch {
            delay(Constants.DURATION_SEARCH_DELAY)
            repository.getEntries(
                keyword = searchQuery.value,
                srcLang = state.value.sourceLanguage,
            ).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _entries.value = result.data ?: emptyList()
                        _state.value = state.value.copy(isLoadingEntries = false)
                    }
                    is Resource.Loading -> {
                        _entries.value = result.data ?: emptyList()
                        _state.value = state.value.copy(isLoadingEntries = true)
                    }
                    is Resource.Error -> {
                        _entries.value = result.data ?: emptyList()
                        _state.value = state.value.copy(isLoadingEntries = false)
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
}
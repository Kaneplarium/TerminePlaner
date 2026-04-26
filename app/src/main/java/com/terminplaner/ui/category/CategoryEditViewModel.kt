package com.terminplaner.ui.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.domain.model.Category
import com.terminplaner.domain.repository.CategoryRepository
import com.terminplaner.util.DataExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryEditUiState(
    val id: Long = 0,
    val name: String = "",
    val color: Int = 0xFF2196F3.toInt(),
    val userName: String? = null,
    val isEditMode: Boolean = false,
    val isSaved: Boolean = false,
    val nameError: Boolean = false
)

@HiltViewModel
class CategoryEditViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val themePreferences: ThemePreferences,
    private val dataExportManager: DataExportManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: Long = savedStateHandle.get<Long>("categoryId") ?: 0

    private val _uiState = MutableStateFlow(CategoryEditUiState(isEditMode = categoryId > 0))
    val uiState: StateFlow<CategoryEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (categoryId > 0) {
                categoryRepository.getCategoryById(categoryId)?.let { category ->
                    _uiState.update {
                        it.copy(
                            id = category.id,
                            name = category.name,
                            color = category.color
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            themePreferences.userName.collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = false) }
    }

    fun updateColor(color: Int) {
        _uiState.update { it.copy(color = color) }
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = true) }
            return
        }

        viewModelScope.launch {
            val category = Category(
                id = state.id,
                name = state.name,
                color = state.color
            )

            if (state.isEditMode) {
                categoryRepository.updateCategory(category)
            } else {
                categoryRepository.insertCategory(category)
            }
            dataExportManager.autoExport()
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
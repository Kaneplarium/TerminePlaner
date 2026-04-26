package com.terminplaner.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.domain.model.Category
import com.terminplaner.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesListUiState(
    val categories: List<Category> = emptyList(),
    val userName: String? = null,
    val isProUser: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class CategoriesListViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesListUiState())
    val uiState: StateFlow<CategoriesListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories, isLoading = false) }
            }
        }

        viewModelScope.launch {
            themePreferences.userName.collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }

        viewModelScope.launch {
            themePreferences.isProUser.collect { isPro ->
                _uiState.update { it.copy(isProUser = isPro) }
            }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            categoryRepository.getCategoryById(id)?.let { category ->
                categoryRepository.deleteCategory(category)
            }
        }
    }
}
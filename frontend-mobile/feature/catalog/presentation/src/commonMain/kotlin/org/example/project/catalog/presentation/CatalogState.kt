package org.example.project.catalog.presentation

import org.example.project.catalog.domain.TemplateSummary

sealed interface CatalogState {
    data object Loading : CatalogState
    data object Empty : CatalogState
    data object Error : CatalogState
    data class Loaded(val templates: List<TemplateSummary>) : CatalogState
}

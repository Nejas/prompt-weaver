package org.example.project.catalog.domain

sealed class CatalogException(cause: Throwable? = null) : Exception(cause) {
    class InvalidInput : CatalogException()
    class NotFound : CatalogException()
    class Unavailable(cause: Throwable? = null) : CatalogException(cause)
    class Unexpected(cause: Throwable? = null) : CatalogException(cause)
}

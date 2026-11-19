package org.example.project.di

import org.example.project.network.GatewayConfig
import org.example.project.network.createHttpClient
import org.koin.dsl.module
import org.koin.core.context.startKoin
import org.example.project.catalog.data.HttpCatalogRepository
import org.example.project.catalog.domain.CatalogRepository

fun appModule(gatewayBaseUrl: String) = module {
    single { GatewayConfig(gatewayBaseUrl) }
    single { createHttpClient(get()) }
    single<CatalogRepository> { HttpCatalogRepository(get()) }
}

fun initializeDependencies(gatewayBaseUrl: String) {
    startKoin { modules(appModule(gatewayBaseUrl)) }
}

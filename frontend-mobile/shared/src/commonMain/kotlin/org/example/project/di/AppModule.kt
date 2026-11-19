package org.example.project.di

import org.example.project.network.GatewayConfig
import org.example.project.network.createHttpClient
import org.koin.dsl.module
import org.koin.core.context.startKoin
import com.arkivanov.decompose.ComponentContext
import org.example.project.catalog.presentation.CatalogRoot
import org.example.project.catalog.data.HttpCatalogRepository
import org.example.project.catalog.domain.CatalogRepository

fun appModule(gatewayBaseUrl: String) = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single { GatewayConfig(gatewayBaseUrl) }
    single { createHttpClient(get()) }
    single<CatalogRepository> { HttpCatalogRepository(get(), get<DispatcherProvider>().io) }
    factory { (context: ComponentContext) -> CatalogRoot(context, get(), get<DispatcherProvider>().main) }
}

fun initializeDependencies(gatewayBaseUrl: String) {
    startKoin { modules(appModule(gatewayBaseUrl)) }
}

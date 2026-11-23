package org.example.project.di

import org.example.project.network.GatewayConfig
import org.example.project.network.createHttpClient
import org.koin.dsl.module
import org.koin.core.context.startKoin
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf
import com.arkivanov.decompose.ComponentContext
import org.example.project.catalog.data.HttpCatalogRepository
import org.example.project.catalog.domain.CatalogRepository
import org.example.project.auth.data.HttpAuthRepository
import org.example.project.auth.domain.AuthRepository
import org.example.project.AppRoot

fun appModule(gatewayBaseUrl: String) = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single { GatewayConfig(gatewayBaseUrl) }
    single { createHttpClient(get()) }
    single<CatalogRepository> { HttpCatalogRepository(get(), get<DispatcherProvider>().io) }
    single<AuthRepository> { HttpAuthRepository(get(), get<DispatcherProvider>().io) }
    factory { (context: ComponentContext) -> AppRoot(context, get(), get(), get<DispatcherProvider>().main) }
}

fun initializeDependencies(gatewayBaseUrl: String): Koin =
    startKoin { modules(appModule(gatewayBaseUrl)) }.koin

fun createAppComponent(koin: Koin, context: ComponentContext): AppRoot =
    koin.get { parametersOf(context) }

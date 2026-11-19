package org.example.project.di

import org.example.project.network.GatewayConfig
import org.example.project.network.createHttpClient
import org.koin.dsl.module
import org.koin.core.context.startKoin

fun appModule(gatewayBaseUrl: String) = module {
    single { GatewayConfig(gatewayBaseUrl) }
    single { createHttpClient(get()) }
}

fun initializeDependencies(gatewayBaseUrl: String) {
    startKoin { modules(appModule(gatewayBaseUrl)) }
}

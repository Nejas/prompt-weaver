package org.example.project.di

import com.arkivanov.decompose.ComponentContext
import org.example.project.AppRoot
import org.example.project.auth.data.SessionClient
import org.example.project.auth.domain.AuthRepository
import org.example.project.auth.domain.SessionRepository
import org.example.project.auth.domain.TokenStore
import org.example.project.catalog.data.HttpCatalogRepository
import org.example.project.catalog.domain.CatalogRepository
import org.example.project.chat.data.LocalChatTemplateStore
import org.example.project.chat.data.MergedChatTemplateRepository
import org.example.project.chat.domain.ChatTemplateRepository
import org.example.project.network.GatewayConfig
import org.example.project.network.createHttpClient
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

fun appModule(gatewayBaseUrl: String, tokenStore: TokenStore, localTemplates: LocalChatTemplateStore) = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single { GatewayConfig(gatewayBaseUrl) }
    single { createHttpClient(get()) }
    single<CatalogRepository> { HttpCatalogRepository(get(), get<DispatcherProvider>().io) }
    single<ChatTemplateRepository> { MergedChatTemplateRepository(get(), localTemplates) }
    single { SessionClient(get(), tokenStore, get<DispatcherProvider>().io) }
    single<AuthRepository> { get<SessionClient>() }
    single<SessionRepository> { get<SessionClient>() }
    factory { (context: ComponentContext) -> AppRoot(context, get(), get(), get(), get<DispatcherProvider>().main, get()) }
}

fun initializeDependencies(gatewayBaseUrl: String, tokenStore: TokenStore, localTemplates: LocalChatTemplateStore): Koin =
    startKoin { modules(appModule(gatewayBaseUrl, tokenStore, localTemplates)) }.koin

fun createAppComponent(koin: Koin, context: ComponentContext): AppRoot =
    koin.get { parametersOf(context) }

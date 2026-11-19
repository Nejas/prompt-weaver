package org.example.project.catalog.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.StandardTestDispatcher
import org.example.project.catalog.domain.*
import kotlin.test.Test
import kotlin.test.assertEquals

class CatalogComponentTest {
    @Test fun loadEmptyRetryAndSelect() = runTest {
        val item = TemplateSummary("v1", "Title", "", "", "")
        val repo = FakeRepository()
        var selected = ""
        val component = CatalogComponent(DefaultComponentContext(LifecycleRegistry()), repo, { selected = it }, StandardTestDispatcher(testScheduler))
        testScheduler.runCurrent()
        assertEquals(CatalogState.Empty, component.state.value)
        repo.items = listOf(item)
        component.reload()
        testScheduler.runCurrent()
        assertEquals(CatalogState.Loaded(listOf(item)), component.state.value)
        component.select("v1")
        assertEquals("v1", selected)
    }

    @Test fun errorAndRetry() = runTest {
        val repo = FakeRepository().apply { fail = true }
        val component = CatalogComponent(DefaultComponentContext(LifecycleRegistry()), repo, {}, StandardTestDispatcher(testScheduler))
        testScheduler.runCurrent()
        assertEquals(CatalogState.Error, component.state.value)
        repo.fail = false
        component.reload()
        testScheduler.runCurrent()
        assertEquals(CatalogState.Empty, component.state.value)
    }
}

private class FakeRepository : CatalogRepository {
    var fail = false
    var items = emptyList<TemplateSummary>()
    override suspend fun listTemplates(limit: Int, offset: Int): TemplatePage {
        if (fail) throw CatalogException.Unavailable()
        return TemplatePage(items, limit, offset)
    }
    override suspend fun getTemplate(versionId: String): TemplateDetails = error("Unused")
    override suspend fun composePrompt(versionId: String, parameters: Map<String, String>): ComposedPrompt = error("Unused")
}

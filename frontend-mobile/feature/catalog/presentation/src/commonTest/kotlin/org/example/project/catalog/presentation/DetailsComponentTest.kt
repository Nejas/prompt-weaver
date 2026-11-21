package org.example.project.catalog.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.example.project.catalog.domain.*
import kotlin.test.*

class DetailsComponentTest {
    @Test fun loadValidateAndBack() = runTest {
        val template = TemplateDetails(TemplateSummary("v1", "T", "", "", ""), "", listOf(TemplateParameter("x", "X", "text", true, null, emptyList())))
        val repo = DetailsFake(template)
        var backed = false
        val component = DetailsComponent(DefaultComponentContext(LifecycleRegistry()), "v1", repo, StandardTestDispatcher(testScheduler)) { backed = true }
        assertEquals(DetailsState.Loading, component.state.value)
        testScheduler.runCurrent()
        component.validate()
        assertEquals(listOf(ParameterValidationError.Required("x")), (component.state.value as DetailsState.Loaded).errors)
        component.setValue("x", "hello")
        component.validate()
        assertTrue((component.state.value as DetailsState.Loaded).errors.isEmpty())
        component.back()
        assertTrue(backed)
    }
    @Test fun navigationOpensDetailsAndReturnsToList() = runTest {
        val repo = DetailsFake(TemplateDetails(TemplateSummary("v1", "", "", "", ""), "", emptyList()))
        val root = CatalogRoot(DefaultComponentContext(LifecycleRegistry()), repo, StandardTestDispatcher(testScheduler))
        val catalog = (root.stack.value.active.instance as CatalogRoot.Child.List).component
        catalog.select("v1")
        val details = assertIs<CatalogRoot.Child.Details>(root.stack.value.active.instance)
        details.component.back()
        assertIs<CatalogRoot.Child.List>(root.stack.value.active.instance)
    }
    @Test fun errorAndRetry() = runTest {
        val repo = DetailsFake(TemplateDetails(TemplateSummary("v1", "", "", "", ""), "", emptyList()))
        repo.fail = true
        val component = DetailsComponent(DefaultComponentContext(LifecycleRegistry()), "v1", repo, StandardTestDispatcher(testScheduler)) {}
        testScheduler.runCurrent()
        assertEquals(DetailsState.Error, component.state.value)
        repo.fail = false
        component.reload()
        testScheduler.runCurrent()
        assertIs<DetailsState.Loaded>(component.state.value)
    }
}
private class DetailsFake(val template: TemplateDetails) : CatalogRepository {
    var fail = false
    override suspend fun getTemplate(versionId: String): TemplateDetails {
        if (fail) throw CatalogException.NotFound()
        return template
    }
    override suspend fun listTemplates(limit: Int, offset: Int): TemplatePage = TemplatePage(emptyList(), limit, offset)
    override suspend fun composePrompt(versionId: String, parameters: Map<String, String>): ComposedPrompt = error("Unused")
}

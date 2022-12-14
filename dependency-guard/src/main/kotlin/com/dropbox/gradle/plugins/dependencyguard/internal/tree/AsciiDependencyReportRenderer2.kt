package com.dropbox.gradle.plugins.dependencyguard.internal.tree

import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer
import org.gradle.api.tasks.diagnostics.internal.dependencies.AsciiDependencyReportRenderer
import org.gradle.api.tasks.diagnostics.internal.graph.DependencyGraphsRenderer
import org.gradle.api.tasks.diagnostics.internal.graph.NodeRenderer
import org.gradle.api.tasks.diagnostics.internal.graph.SimpleNodeRenderer
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableModuleResult
import org.gradle.internal.graph.GraphRenderer
import org.gradle.internal.logging.text.StyledTextOutput

/**
 * Simplified version of [AsciiDependencyReportRenderer] that renders [ResolvedComponentResult].
 *
 * `dependencies` task could be used to render dependencies tree, but it does not support Configuration Cache.
 * That's why [AsciiDependencyReportRenderer2] was added to render [ResolvedComponentResult].
 */
internal class AsciiDependencyReportRenderer2 : TextReportRenderer() {

    private var dependencyGraphRenderer: DependencyGraphsRenderer? = null

    fun prepareVisit() {
        val renderer = GraphRenderer(textOutput)
        dependencyGraphRenderer =
            DependencyGraphsRenderer(textOutput, renderer, NodeRenderer.NO_OP, SimpleNodeRenderer())
    }

    fun render(resolvedComponentResult: ResolvedComponentResult) {
        val root = RenderableModuleResult(resolvedComponentResult)

        if (root.children.isEmpty()) {
            textOutput.withStyle(StyledTextOutput.Style.Info).text("No dependencies")
            textOutput.println()
            return
        }
        dependencyGraphRenderer!!.render(listOf(root))
    }

    override fun complete() {
        dependencyGraphRenderer?.complete()
        super.complete()
    }
}
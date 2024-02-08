package org.analogjs.codeInsight

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.find.usages.api.SearchTarget
import com.intellij.find.usages.api.UsageSearchParameters
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.model.Pointer
import com.intellij.model.search.SearchService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.parentOfType
import com.intellij.xml.util.HtmlUtil
import org.analogjs.findTopLevelAnalogTags
import org.analogjs.lang.AnalogFile
import org.angular2.entities.Angular2EntitiesProvider

class AnalogImplicitUsageProvider : ImplicitUsageProvider {

  override fun isImplicitUsage(element: PsiElement): Boolean =
    element.containingFile is AnalogFile
    && (
      (element is JSVariable && element.parentOfType<JSExecutionScope>() is JSEmbeddedContent)
      || (element is ES6ImportedBinding && isUsedDirectiveImport(element)))

  override fun isImplicitRead(element: PsiElement): Boolean =
    false

  override fun isImplicitWrite(element: PsiElement): Boolean =
    false

  private fun isUsedDirectiveImport(element: ES6ImportedBinding): Boolean {
    val selectors = element.multiResolve(false)
                      .asSequence()
                      .firstNotNullOfOrNull { Angular2EntitiesProvider.getDirective(it.element) }
                      ?.selector?.simpleSelectorsWithPsi
                      ?.flatMap { it.allSymbols }
                      ?.toList()
                    ?: return false
    val templateTag = (element.containingFile as AnalogFile).findTopLevelAnalogTags(HtmlUtil.TEMPLATE_TAG_NAME).firstOrNull()
                      ?: return false
    val scope = LocalSearchScope(templateTag)
    val query = selectors
      .map { SearchService.getInstance().searchParameters(ComponentUsageSearchParameters(element.project, it, scope)) }
      .let { SearchService.getInstance().merge(it) }
    return query.findFirst() != null
  }

  private class ComponentUsageSearchParameters(
    private val project: Project,
    target: SearchTarget,
    override val searchScope: SearchScope
  ) : UsageSearchParameters {
    private val pointer: Pointer<out SearchTarget> = target.createPointer()
    override fun areValid(): Boolean = pointer.dereference() != null
    override fun getProject(): Project = project
    override val target: SearchTarget get() = requireNotNull(pointer.dereference())
  }
}
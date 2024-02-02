package org.analogjs.web

import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_JS
import com.intellij.webSymbols.WebSymbolOrigin
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.context.WebSymbolsContext
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.query.WebSymbolsQueryConfigurator
import com.intellij.webSymbols.utils.ReferencingWebSymbol
import org.analogjs.lang.AnalogFile
import org.analogjs.lang.psi.impl.AnalogScriptEmbeddedContentImpl

val ANALOG_SCRIPT_SYMBOLS = WebSymbolQualifiedKind(NAMESPACE_JS, "analog-script-symbols")

class AnalogWebSymbolQueryConfigurator : WebSymbolsQueryConfigurator {

  override fun getScope(project: Project, location: PsiElement?, context: WebSymbolsContext, allowResolve: Boolean): List<WebSymbolsScope> {
    if (location?.containingFile !is AnalogFile) return emptyList()

    if (location.parentOfType<AnalogScriptEmbeddedContentImpl>() != null) {
      return buildTypeScriptScope(location)
    }
    return emptyList()
  }

  private fun buildTypeScriptScope(location: PsiElement): List<WebSymbolsScope> {
    return listOf(analogScriptSymbolsScope)
  }

  private val analogScriptSymbolsScope = object : WebSymbolsScope {

    override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind,
                            params: WebSymbolsListSymbolsQueryParams,
                            scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
      if (qualifiedKind == WebSymbol.JS_SYMBOLS)
        listOf(analogScriptSymbols)
      else
        emptyList()

    override fun createPointer(): Pointer<out WebSymbolsScope> =
      Pointer.hardPointer(this)

    override fun getModificationCount(): Long =
      0
  }

  private val analogScriptSymbols = ReferencingWebSymbol(
    WebSymbol.JS_SYMBOLS, "Analog Script Symbol", WebSymbolOrigin.empty(), ANALOG_SCRIPT_SYMBOLS)

}
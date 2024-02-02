// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.analogjs.codeInsight.template

import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.ResolveResult
import org.analogjs.codeInsight.AnalogStubBasedScopeHandler
import org.analogjs.findModule
import org.analogjs.resolveIfImportSpecifier
import org.angular2.codeInsight.template.Angular2TemplateScope
import org.angular2.codeInsight.template.Angular2TemplateScopesProvider
import java.util.function.Consumer

class AnalogComponentScopeProvider : Angular2TemplateScopesProvider() {

  override fun getScopes(element: PsiElement, hostElement: PsiElement?): List<Angular2TemplateScope> {
    return findModule(element)?.let { scriptSetupModule ->
      listOf(AnalogComponentScope(scriptSetupModule))
    } ?: emptyList()
  }

  private class AnalogComponentScope(private val module: JSExecutionScope) : Angular2TemplateScope(null) {

    override fun resolve(consumer: Consumer<in ResolveResult>) {
      AnalogStubBasedScopeHandler.processDeclarationsInScope(module, { element, _ ->
        val resolved = (element as? JSPsiNamedElementBase)?.resolveIfImportSpecifier()
        val elementToConsume = resolved ?: element
        consumer.accept(PsiElementResolveResult(elementToConsume, true)).let { true }
      }, true)
    }
  }
}

package org.analogjs.codeInsight

import com.intellij.lang.javascript.psi.util.JSStubBasedScopeHandler
import com.intellij.psi.PsiElement
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.contextOfType
import org.analogjs.lang.psi.impl.AnalogScriptEmbeddedContentImpl

object AnalogStubBasedScopeHandler : JSStubBasedScopeHandler() {

  override fun processDeclarationsInScope(context: PsiElement, processor: PsiScopeProcessor, includeParentScopes: Boolean): Boolean =
    when (context) {
      is AnalogScriptEmbeddedContentImpl -> {
        super.processDeclarationsInScope(context, processor, false)
      }

      else -> {
        super.processDeclarationsInScope(context, processor, includeParentScopes)
        && (!includeParentScopes ||
            (context.contextOfType(AnalogScriptEmbeddedContentImpl::class)
               ?.let { processDeclarationsInScope(it, processor, true) }
             ?: true)
           )
      }
    }
}
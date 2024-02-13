package org.analogjs.codeInsight.template

import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.ResolveResult
import com.intellij.util.asSafely
import org.analogjs.PROP_EXPOSES
import org.analogjs.codeInsight.AnalogStubBasedScopeHandler
import org.analogjs.entities.defineMetadataCallInitializer
import org.analogjs.findAnalogScript
import org.analogjs.lang.psi.impl.AnalogScriptEmbeddedContentImpl
import org.analogjs.resolveIfImportSpecifier
import org.angular2.codeInsight.template.Angular2TemplateScope
import org.angular2.codeInsight.template.Angular2TemplateScopesProvider
import java.util.function.Consumer

class AnalogComponentScopeProvider : Angular2TemplateScopesProvider() {

  override fun getScopes(element: PsiElement, hostElement: PsiElement?): List<Angular2TemplateScope> {
    return findAnalogScript(element)?.let { scriptSetupModule ->
      listOf(AnalogComponentScope(scriptSetupModule), AnalogExposeScope(scriptSetupModule))
    } ?: emptyList()
  }

  private class AnalogComponentScope(private val module: AnalogScriptEmbeddedContentImpl) : Angular2TemplateScope(null) {

    override fun resolve(consumer: Consumer<in ResolveResult>) {
      AnalogStubBasedScopeHandler.processDeclarationsInScope(module, { element, _ ->
        val resolved = (element as? JSPsiNamedElementBase)?.resolveIfImportSpecifier()
        val elementToConsume = resolved ?: element
        consumer.accept(PsiElementResolveResult(elementToConsume, true)).let { true }
      }, true)
    }
  }

  private class AnalogExposeScope(private val module: AnalogScriptEmbeddedContentImpl) : Angular2TemplateScope(null) {
    override fun resolve(consumer: Consumer<in ResolveResult>) {
      module.defineMetadataCallInitializer?.findProperty(PROP_EXPOSES)
        ?.value?.asSafely<JSArrayLiteralExpression>()
        ?.expressions?.asSequence()
        ?.filterIsInstance<JSReferenceExpression>()
        ?.filter { it.qualifier == null && it.referenceName != null && it.multiResolve(false).isNotEmpty() }
        ?.forEach { ref ->
          consumer.accept(PsiElementResolveResult(
            JSLocalImplicitElementImpl(ref.referenceName!!,
                                       JSResolveUtil.getElementJSType(ref)?.asRecordType(),
                                       ref, JSImplicitElement.Type.Variable)))
        }
    }

  }
}

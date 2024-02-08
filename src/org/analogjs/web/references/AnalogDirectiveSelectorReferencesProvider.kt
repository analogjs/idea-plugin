package org.analogjs.web.references

import com.intellij.model.psi.PsiExternalReferenceHost
import org.analogjs.isPropertyInDefineMetadata
import org.angular2.Angular2DecoratorUtil.SELECTOR_PROP
import org.angular2.entities.Angular2DirectiveSelector
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.web.references.Angular2SelectorReferencesProvider

class AnalogDirectiveSelectorReferencesProvider : Angular2SelectorReferencesProvider() {
  override fun getDirectiveSelector(element: PsiExternalReferenceHost): Angular2DirectiveSelector? =
    if (isPropertyInDefineMetadata(element, SELECTOR_PROP))
      Angular2EntitiesProvider.getDirective(element.containingFile)?.selector
    else
      null
}
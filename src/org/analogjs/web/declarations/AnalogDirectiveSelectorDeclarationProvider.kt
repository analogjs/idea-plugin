// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.analogjs.web.declarations

import com.intellij.psi.PsiElement
import com.intellij.webSymbols.declarations.WebSymbolDeclaration
import com.intellij.webSymbols.declarations.WebSymbolDeclarationProvider
import org.analogjs.isPropertyInDefineMetadata
import org.angular2.Angular2DecoratorUtil.SELECTOR_PROP
import org.angular2.entities.Angular2DirectiveSelector
import org.angular2.entities.Angular2EntitiesProvider

class AnalogDirectiveSelectorDeclarationProvider : WebSymbolDeclarationProvider {

  override fun getDeclarations(element: PsiElement, offsetInElement: Int): Collection<WebSymbolDeclaration> {
    val directiveSelector: Angular2DirectiveSelector =
      element.takeIf { isPropertyInDefineMetadata(element, SELECTOR_PROP) }
        ?.let { Angular2EntitiesProvider.getDirective(element.containingFile)?.selector }
      ?: return emptyList()
    if (offsetInElement < 0)
      return directiveSelector.simpleSelectorsWithPsi.flatMap { it.allSymbols }.mapNotNull { it.declaration }
    for (selector in directiveSelector.simpleSelectorsWithPsi) {
      val selectorPart = selector.getElementAt(offsetInElement)
      if (selectorPart != null) {
        selectorPart.declaration
          ?.let { return setOf(it) }
        break
      }
    }
    return emptyList()
  }
}

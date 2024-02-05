package org.analogjs.codeInsight

import com.intellij.lang.javascript.modules.imports.JSAddImportExecutor
import com.intellij.lang.javascript.modules.imports.JSImportExecutorFactory
import com.intellij.psi.PsiElement
import org.analogjs.codeInsight.imports.AnalogAddImportExecutor
import org.analogjs.lang.AnalogFile
import org.angular2.codeInsight.Angular2JSHandlersFactory

class AnalogJSHandlersFactory : Angular2JSHandlersFactory() {
  override fun accept(place: PsiElement): Boolean =
    place.containingFile is AnalogFile

  override fun createImportFactories(place: PsiElement): List<JSImportExecutorFactory> {
    return listOf(object : JSImportExecutorFactory {
      override fun createExecutor(place: PsiElement): JSAddImportExecutor =
        AnalogAddImportExecutor(place)
    })
  }
}
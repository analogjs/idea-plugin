package org.analogjs.codeInsight

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import org.analogjs.lang.AnalogFile

class AnalogImplicitUsageProvider : ImplicitUsageProvider {

  override fun isImplicitUsage(element: PsiElement): Boolean =
    element is JSVariable
    && element.containingFile is AnalogFile
    && element.parentOfType<JSExecutionScope>() is JSEmbeddedContent

  override fun isImplicitRead(element: PsiElement): Boolean =
    false

  override fun isImplicitWrite(element: PsiElement): Boolean =
    false

}
// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.analogjs.lang.formatter

import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.lang.xml.XmlFormattingModel
import com.intellij.psi.formatter.FormattingDocumentModelImpl
import com.intellij.psi.formatter.xml.HtmlPolicy
import com.intellij.psi.xml.XmlTag
import org.angular2.lang.html.psi.formatter.Angular2HtmlFormattingBlock
import org.angular2.lang.html.psi.formatter.Angular2HtmlTagBlock

class AnalogFormattingModelBuilder : FormattingModelBuilder {
  override fun createModel(formattingContext: FormattingContext): FormattingModel {
    val psiFile = formattingContext.containingFile
    val documentModel = FormattingDocumentModelImpl.createOn(psiFile)
    val element = formattingContext.psiElement
    val settings = formattingContext.codeStyleSettings
    return if (element is XmlTag) {
      XmlFormattingModel(
        psiFile,
        Angular2HtmlTagBlock(element.node, null, null, HtmlPolicy(settings, documentModel),
                             null, false),
        documentModel)
    }
    else {
      XmlFormattingModel(
        psiFile,
        Angular2HtmlFormattingBlock(psiFile.node, null, null, AnalogRootFormattingPolicy(settings, documentModel),
                                    null, null, false),
        documentModel)
    }
  }
}
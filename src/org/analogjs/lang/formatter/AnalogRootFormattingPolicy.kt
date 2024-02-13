package org.analogjs.lang.formatter

import com.intellij.formatting.FormattingDocumentModel
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlPolicy
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType

class AnalogRootFormattingPolicy(settings: CodeStyleSettings, documentModel: FormattingDocumentModel) :
  HtmlPolicy(settings, documentModel) {

  override fun indentChildrenOf(parentTag: XmlTag?): Boolean {
    if (parentTag == null) {
      return true
    }
    val firstChild = findFirstNonEmptyChild(parentTag) ?: return false
    // Indent children of all top level tags
    return firstChild.node.elementType === XmlTokenType.XML_START_TAG_START
  }
}
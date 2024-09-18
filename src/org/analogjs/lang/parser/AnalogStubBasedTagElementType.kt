package org.analogjs.lang.parser

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.html.HtmlStubBasedTagElementType
import com.intellij.psi.xml.XmlTag
import org.analogjs.lang.AnalogLanguage
import org.analogjs.lang.parser.AnalogStubElementTypes.EXTERNAL_ID_PREFIX

open class AnalogStubBasedTagElementType(debugName: String) : HtmlStubBasedTagElementType(debugName, AnalogLanguage.INSTANCE) {
  override fun shouldCreateStub(node: ASTNode?): Boolean =
    (node?.psi as? XmlTag)
      // top-level style/script/template tag
      ?.let {
        it.parentTag == null
      } ?: false

  override fun getExternalId(): String {
    return EXTERNAL_ID_PREFIX + super.getExternalId()
  }
}
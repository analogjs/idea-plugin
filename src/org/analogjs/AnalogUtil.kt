package org.analogjs

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.StubSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.impl.source.xml.stub.XmlTagStub
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import com.intellij.xml.util.HtmlUtil
import org.analogjs.lang.AnalogFile

@StubSafe
fun findModule(element: PsiElement?): JSExecutionScope? =
  element
    ?.let { InjectedLanguageManager.getInstance(element.project) }
    ?.getTopLevelFile(element)
    ?.asSafely<XmlFile>()
    ?.findScriptTag()
    ?.let { tag ->
      PsiTreeUtil.getStubChildOfType(tag, JSEmbeddedContent::class.java)
    }

@StubSafe
fun XmlFile.findScriptTag(): XmlTag? =
  findTopLevelAnalogTags(HtmlUtil.SCRIPT_TAG_NAME)
    .firstOrNull()

@StubSafe
fun XmlFile.findTopLevelAnalogTags(tagName: String): List<XmlTag> {
  if (this !is AnalogFile) return emptyList()
  stub?.let { stub ->
    return stub.childrenStubs
      .asSequence()
      .mapNotNull { (it as? XmlTagStub<*>)?.psi }
      .filter { it.localName.equals(tagName, ignoreCase = true) }
      .toList()
  }
  val result = SmartList<XmlTag>()
  accept(object : TopLevelElementsVisitor() {
    override fun visitXmlTag(tag: XmlTag) {
      if (tag.localName.equals(tagName, ignoreCase = true)) {
        result.add(tag)
      }
    }
  })
  return result
}

fun JSPsiNamedElementBase.resolveIfImportSpecifier(): JSPsiNamedElementBase =
  (this as? ES6ImportSpecifier)
    ?.multiResolve(false)
    ?.asSequence()
    ?.mapNotNull { it.takeIf { it.isValidResult }?.element as? JSPsiNamedElementBase }
    ?.firstOrNull()
  ?: this

private open class TopLevelElementsVisitor : XmlElementVisitor() {
  override fun visitXmlDocument(document: XmlDocument): Unit = recursion(document)

  override fun visitXmlFile(file: XmlFile): Unit = recursion(file)

  protected fun recursion(element: PsiElement) {
    element.children.forEach { it.accept(this) }
  }
}
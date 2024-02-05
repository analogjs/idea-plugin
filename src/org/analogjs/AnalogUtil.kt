package org.analogjs

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.injection.InjectedLanguageManager
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
import com.intellij.xml.util.HtmlUtil.TEMPLATE_TAG_NAME
import org.analogjs.lang.AnalogFile
import org.analogjs.lang.psi.impl.AnalogScriptEmbeddedContentImpl

const val FUN_DEFINE_METADATA = "defineMetadata"

const val PROP_EXPOSES = "exposes"

const val ANALOG_EXTENSION = ".analog"

val AnalogFile.analogScript: AnalogScriptEmbeddedContentImpl?
  get() = findAnalogScript(this)

val AnalogFile.templateTag: XmlTag?
  get() = findTopLevelAnalogTags(TEMPLATE_TAG_NAME).firstOrNull()

@StubSafe
fun findAnalogScript(element: PsiElement?): AnalogScriptEmbeddedContentImpl? =
  element
    ?.let { InjectedLanguageManager.getInstance(element.project) }
    ?.getTopLevelFile(element)
    ?.asSafely<XmlFile>()
    ?.findScriptTag()
    ?.let { tag ->
      PsiTreeUtil.getStubChildOfType(tag, AnalogScriptEmbeddedContentImpl::class.java)
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
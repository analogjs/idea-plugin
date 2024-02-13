package org.analogjs.lang.parser

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.xml.util.HtmlUtil.*
import org.angular2.lang.html.Angular2TemplateSyntax
import org.angular2.lang.html.parser.Angular2HtmlParsing
import java.util.*

class AnalogParsing(templateSyntax: Angular2TemplateSyntax, builder: PsiBuilder)
  : Angular2HtmlParsing(templateSyntax, builder) {

  override fun getHtmlTagElementType(info: HtmlTagInfo, tagLevel: Int): IElementType {
    val tagName = info.normalizedName.lowercase(Locale.US)
    if (tagName in ALWAYS_STUBBED_TAGS
        || (tagLevel == 1 && tagName in TOP_LEVEL_TAGS)) {
      return if (tagName == TEMPLATE_TAG_NAME) AnalogStubElementTypes.TEMPLATE_TAG else AnalogStubElementTypes.STUBBED_TAG
    }
    return super.getHtmlTagElementType(info, tagLevel)
  }

  companion object {
    val ALWAYS_STUBBED_TAGS: List<String> = listOf(SCRIPT_TAG_NAME)
    val TOP_LEVEL_TAGS: List<String> = listOf(TEMPLATE_TAG_NAME, STYLE_TAG_NAME)
  }
}

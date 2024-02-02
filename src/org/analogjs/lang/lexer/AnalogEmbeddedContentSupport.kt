package org.analogjs.lang.lexer

import com.intellij.html.embedding.*
import com.intellij.lang.Language
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JavaScriptHighlightingLexer
import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.tree.IElementType
import com.intellij.xml.util.HtmlUtil
import com.intellij.xml.util.HtmlUtil.LANG_ATTRIBUTE_NAME
import org.analogjs.lang.parser.AnalogStubElementTypes

class AnalogEmbeddedContentSupport : HtmlEmbeddedContentSupport {
  override fun isEnabled(lexer: BaseHtmlLexer): Boolean = lexer is AnalogLexer

  override fun createEmbeddedContentProviders(lexer: BaseHtmlLexer): List<HtmlEmbeddedContentProvider> =
    listOf(
      AnalogTagEmbeddedContentProvider(lexer),
    )
}

class AnalogTagEmbeddedContentProvider(lexer: BaseHtmlLexer) : HtmlTagEmbeddedContentProvider(lexer) {

  private val interestingTags: List<String> = listOf(HtmlUtil.TEMPLATE_TAG_NAME, HtmlUtil.SCRIPT_TAG_NAME, HtmlUtil.STYLE_TAG_NAME)

  override fun isInterestedInTag(tagName: CharSequence): Boolean =
    interestingTags.any { namesEqual(tagName, it) }

  override fun isInterestedInAttribute(attributeName: CharSequence): Boolean =
    namesEqual(attributeName, LANG_ATTRIBUTE_NAME)

  override fun createEmbedmentInfo(): HtmlEmbedmentInfo? {
    val tagName = tagName ?: return null
    val attributeValue = attributeValue?.trim()?.toString()
    return when {
      namesEqual(tagName, HtmlUtil.STYLE_TAG_NAME) ->
        styleLanguage(attributeValue)
          ?.let { HtmlEmbeddedContentSupport.getStyleTagEmbedmentInfo(it) }
        ?: HtmlEmbeddedContentProvider.RAW_TEXT_EMBEDMENT

      namesEqual(tagName, HtmlUtil.SCRIPT_TAG_NAME) -> AnalogScriptEmbedmentInfo(AnalogStubElementTypes.SCRIPT_EMBEDDED_CONTENT)
      namesEqual(tagName, HtmlUtil.TEMPLATE_TAG_NAME) -> getTemplateTagInfo(tagName, attributeValue)
      else -> null
    }
  }

  private fun getTemplateTagInfo(tagName: CharSequence, lang: String?): HtmlEmbedmentInfo? {
    return if (lang == null || lang.equals("html", ignoreCase = true)) null else findEmbedmentInfo(lang)
  }

  class AnalogScriptEmbedmentInfo(private val elementType: IElementType) : HtmlEmbedmentInfo {
    override fun getElementType(): IElementType = elementType
    override fun createHighlightingLexer(): Lexer = JavaScriptHighlightingLexer(DialectOptionHolder.TS)
  }

  private fun findEmbedmentInfo(language: String): HtmlEmbedmentInfo =
    Language.findInstancesByMimeType(language)
      .asSequence()
      .plus(Language.findInstancesByMimeType("text/$language"))
      .plus(
        Language.getRegisteredLanguages()
          .asSequence()
          .filter { languageMatches(language, it) }
      )
      .plus(if (StringUtil.containsIgnoreCase(language, "template")) listOf(HTMLLanguage.INSTANCE) else emptyList())
      .map {
        HtmlEmbeddedContentSupport.getScriptTagEmbedmentInfo(it)
      }.firstOrNull().let(::wrapEmbedmentInfo)

  private fun languageMatches(scriptType: String, language: Language): Boolean =
    scriptType.equals(language.id, ignoreCase = true)
    || FileTypeManager.getInstance().getFileTypeByExtension(scriptType) === language.associatedFileType


  private fun wrapEmbedmentInfo(embedmentInfo: HtmlEmbedmentInfo?): HtmlEmbedmentInfo {
    return when (val elementType = embedmentInfo?.getElementType()) {
      null -> HtmlEmbeddedContentProvider.RAW_TEXT_EMBEDMENT
      else -> object : HtmlEmbedmentInfo {
        // JSElementTypes.toModuleContentType is significant for JSX/TSX
        override fun getElementType(): IElementType? = JSElementTypes.toModuleContentType(elementType)
        override fun createHighlightingLexer(): Lexer? = embedmentInfo.createHighlightingLexer()
      }
    }
  }

  companion object {
    fun styleLanguage(styleLang: String?): Language? {
      val cssLanguage = CSSLanguage.INSTANCE
      if (styleLang != null) {
        if (styleLang.equals("text/css", ignoreCase = true)) return cssLanguage
        cssLanguage
          .dialects
          .firstOrNull { dialect ->
            dialect.id.equals(styleLang, ignoreCase = true)
            || dialect.mimeTypes.any { it.equals(styleLang, ignoreCase = true) }
          }
          ?.let { return it }
      }
      return cssLanguage
    }
  }

}
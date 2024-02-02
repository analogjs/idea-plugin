package org.analogjs.lang.lexer

import com.intellij.html.embedding.HtmlEmbeddedContentProvider
import com.intellij.lexer.HtmlScriptStyleEmbeddedContentProvider
import org.angular2.lang.html.Angular2TemplateSyntax
import org.angular2.lang.html.lexer.Angular2HtmlLexer

class AnalogLexer(highlightMode: Boolean, templateSyntax: Angular2TemplateSyntax) :
  Angular2HtmlLexer(highlightMode, templateSyntax, null) {

  override fun acceptEmbeddedContentProvider(provider: HtmlEmbeddedContentProvider): Boolean {
    return provider !is HtmlScriptStyleEmbeddedContentProvider
  }
}
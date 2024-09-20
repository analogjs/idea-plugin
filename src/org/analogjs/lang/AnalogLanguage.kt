package org.analogjs.lang

import com.intellij.javascript.web.WebFramework
import com.intellij.javascript.web.html.WebFrameworkHtmlDialect
import com.intellij.lang.html.HTMLLanguage
import org.analogjs.AnalogBundle
import org.angular2.angular2Framework
import org.angular2.lang.html.Angular2HtmlDialect
import org.angular2.lang.html.Angular2TemplateSyntax
import org.jetbrains.annotations.Nls

class AnalogLanguage private constructor()
  : WebFrameworkHtmlDialect(HTMLLanguage.INSTANCE, "Analog"), Angular2HtmlDialect {

  override fun getDisplayName(): @Nls String {
    return AnalogBundle.message("analog.template")
  }

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_18_1

  override val svgDialect: Boolean
    get() = false

  override val framework: WebFramework
    get() = angular2Framework

  companion object {
    @JvmField
    val INSTANCE = AnalogLanguage()
  }
}
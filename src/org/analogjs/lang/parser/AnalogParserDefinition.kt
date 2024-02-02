package org.analogjs.lang.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HTMLParser
import com.intellij.lang.html.HTMLParserDefinition
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import org.analogjs.lang.AnalogFile
import org.analogjs.lang.AnalogFileElementType
import org.analogjs.lang.lexer.AnalogLexer
import org.angular2.lang.html.Angular2TemplateSyntax

class AnalogParserDefinition : HTMLParserDefinition() {

  override fun createLexer(project: Project): Lexer {
    return AnalogLexer(false, Angular2TemplateSyntax.V_17)
  }

  override fun getFileNodeType(): IFileElementType {
    return AnalogFileElementType.INSTANCE
  }

  override fun createFile(viewProvider: FileViewProvider): PsiFile {
    return AnalogFile(viewProvider, AnalogFileElementType.INSTANCE)
  }

  override fun createParser(project: Project?): HTMLParser = AnalogParser()
}

class AnalogParser : HTMLParser() {
  override fun createHtmlParsing(builder: PsiBuilder): AnalogParsing =
    AnalogParsing(Angular2TemplateSyntax.V_17, builder)
}
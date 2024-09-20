package org.analogjs.lang.highlighting

import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.angular2.lang.html.Angular2TemplateSyntax
import org.angular2.lang.html.highlighting.Angular2HtmlFileHighlighter

class AnalogHighlighterFactory : SyntaxHighlighterFactory() {
  override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
    return Angular2HtmlFileHighlighter(Angular2TemplateSyntax.V_18_1, null)
  }
}
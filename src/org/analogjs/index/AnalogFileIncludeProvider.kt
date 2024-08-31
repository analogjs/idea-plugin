package org.analogjs.index

import com.intellij.lang.ecmascript6.index.ES6FileIncludeProvider
import com.intellij.lang.ecmascript6.index.JSFrameworkFileIncludeProvider
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.psi.impl.include.FileIncludeInfo
import com.intellij.util.indexing.FileContent
import org.analogjs.findAnalogScript
import org.analogjs.lang.AgFileType
import org.analogjs.lang.AnalogFileType

/**
 * ES6FileIncludeProvider doesn't work for vue files but we need these files in index for building ts imports graph
 * @see ES6FileIncludeProvider
 */
class AnalogFileIncludeProvider : JSFrameworkFileIncludeProvider(AnalogFileType) {
  override fun getIncludeInfos(content: FileContent): Array<FileIncludeInfo> {
    if (!ES6FileIncludeProvider.checkTextHasFromKeyword(content)) return emptyArray()

    val psiFile = content.psiFile
    val importDeclarations = findAnalogScript(psiFile)?.let { ES6ImportPsiUtil.getImportDeclarations(it) } ?: emptyList()
    return createFileIncludeInfos(importDeclarations)
  }
}

/**
 * ES6FileIncludeProvider doesn't work for vue files but we need these files in index for building ts imports graph
 * @see ES6FileIncludeProvider
 */
class AgFileIncludeProvider : JSFrameworkFileIncludeProvider(AgFileType) {
  override fun getIncludeInfos(content: FileContent): Array<FileIncludeInfo> {
    if (!ES6FileIncludeProvider.checkTextHasFromKeyword(content)) return emptyArray()

    val psiFile = content.psiFile
    val importDeclarations = findAnalogScript(psiFile)?.let { ES6ImportPsiUtil.getImportDeclarations(it) } ?: emptyList()
    return createFileIncludeInfos(importDeclarations)
  }
}
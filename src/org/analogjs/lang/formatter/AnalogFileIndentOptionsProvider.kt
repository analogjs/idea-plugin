// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.analogjs.lang.formatter

import com.intellij.lang.html.HTMLLanguage
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.IndentOptions
import com.intellij.psi.codeStyle.PsiBasedFileIndentOptionsProvider
import com.intellij.util.asSafely
import org.analogjs.lang.AnalogFile
import org.analogjs.lang.AnalogLanguage

class AnalogFileIndentOptionsProvider : PsiBasedFileIndentOptionsProvider() {

  override fun getIndentOptionsByPsiFile(settings: CodeStyleSettings, file: PsiFile): IndentOptions? {
    if (file is AnalogFile) {
      // Always use uniform indentation
      //if (settings.getCustomSettings(AnalogCodeStyleSettings::class.java).UNIFORM_INDENT)
      return settings.getLanguageIndentOptions(AnalogLanguage.INSTANCE)
          ?.clone()
          ?.asSafely<IndentOptions>()
          ?.also { it.isOverrideLanguageOptions = true }
      //else
      //  settings.getLanguageIndentOptions(HTMLLanguage.INSTANCE)
    }
    return null
  }
}
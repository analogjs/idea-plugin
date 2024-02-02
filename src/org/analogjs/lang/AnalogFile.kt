// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.analogjs.lang

import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.psi.JSExternalModule
import com.intellij.lang.javascript.psi.JSType
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker

class AnalogFile(viewProvider: FileViewProvider, fileElementType: IFileElementType)
  : HtmlFileImpl(viewProvider, fileElementType), JSExternalModule {

  override fun getDefaultExportedName(): String =
    FileUtil.getNameWithoutExtension(name)
      .replace(".", "-")
      .let { JSStringUtil.toCamelCase(it) }

  override fun buildModuleType(module: PsiElement): JSType? {
    return CachedValuesManager.getCachedValue(module) {
      CachedValueProvider.Result.createSingleDependency(null, PsiModificationTracker.MODIFICATION_COUNT)
    }
  }
}

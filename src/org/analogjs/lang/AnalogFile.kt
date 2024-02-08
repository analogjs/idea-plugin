// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.analogjs.lang

import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.psi.JSExternalModule
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.resolve.ResultSink
import com.intellij.lang.javascript.psi.types.JSRecordTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.lang.javascript.psi.types.recordImpl.PropertySignatureImpl
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.analogjs.analogScript
import org.analogjs.codeInsight.AnalogStubBasedScopeHandler

class AnalogFile(viewProvider: FileViewProvider, fileElementType: IFileElementType)
  : HtmlFileImpl(viewProvider, fileElementType), JSExternalModule {

  override fun getDefaultExportedName(): String =
    FileUtil.getNameWithoutExtension(name)
      .replace(".", "-")
      .let { JSStringUtil.toCamelCase(it) }

  override fun buildModuleType(module: PsiElement): JSType? {
    return CachedValuesManager.getCachedValue(module) {
      val script = module.asSafely<AnalogFile>()?.analogScript
                   ?: return@getCachedValue CachedValueProvider.Result.createSingleDependency(null, PsiModificationTracker.MODIFICATION_COUNT)
      val exports = mutableListOf<PsiElement>()
      AnalogStubBasedScopeHandler.processDeclarationsInScope(script, { element, _ ->
        if (element is JSAttributeListOwner && element.hasModifier(JSAttributeList.ModifierType.EXPORT)) {
          exports.add(element)
        }
        true
      }, false)

      val properties = exports
        .mapNotNull {
          val name = it.asSafely<PsiNamedElement>()?.name
          if (name != null)
            PropertySignatureImpl(name, JSResolveUtil.getElementJSType(it), true, true, it)
          else
            null
        }
        .plus(PropertySignatureImpl("default", null, true, true, module))

      CachedValueProvider.Result.createSingleDependency(
        JSRecordTypeImpl(JSTypeSourceFactory.createTypeSource(module, true), properties),
        PsiModificationTracker.MODIFICATION_COUNT)
    }
  }
}

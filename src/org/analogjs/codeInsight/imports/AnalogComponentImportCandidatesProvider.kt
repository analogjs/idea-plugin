// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.analogjs.codeInsight.imports

import com.intellij.lang.javascript.modules.JSImportPlaceInfo
import com.intellij.lang.javascript.modules.imports.ES6ImportCandidate
import com.intellij.lang.javascript.modules.imports.JSImportCandidatesBase
import com.intellij.lang.javascript.modules.imports.providers.JSCandidatesProcessor
import com.intellij.lang.javascript.modules.imports.providers.JSImportCandidatesProvider
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import org.analogjs.lang.AnalogFile
import org.angular2.lang.Angular2LangUtil
import java.util.function.Predicate

class AnalogComponentImportCandidatesProvider(private val placeInfo: JSImportPlaceInfo) : JSImportCandidatesBase(placeInfo) {

  override fun getNames(keyFilter: Predicate<in String>): Set<String> {
    val psiManager = PsiManager.getInstance(placeInfo.project)
    return FilenameIndex.getAllFilesByExt(placeInfo.project, "analog")
      .asSequence()
      .mapNotNull { psiManager.findFile(it) }
      .filterIsInstance<AnalogFile>()
      .map { it.defaultExportedName }
      .toSet()
  }

  override fun processCandidates(name: String, processor: JSCandidatesProcessor) {
    val psiManager = PsiManager.getInstance(placeInfo.project)
    FilenameIndex.getAllFilesByExt(placeInfo.project, "analog")
      .asSequence()
      .mapNotNull { psiManager.findFile(it) }
      .filterIsInstance<AnalogFile>()
      .filter { it.defaultExportedName == name }
      .map { ES6ImportCandidate(it.defaultExportedName, it, placeInfo.place) }
      .forEach { processor.processCandidate(it) }
  }

}

class AnalogComponentImportCandidatesProviderFactory : JSImportCandidatesProvider.CandidatesFactory {
  override fun createProvider(placeInfo: JSImportPlaceInfo): JSImportCandidatesProvider? =
    if (Angular2LangUtil.isAngular2Context(placeInfo.place)) AnalogComponentImportCandidatesProvider(placeInfo) else null

}
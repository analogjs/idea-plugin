// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.analogjs.index

import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.openapi.fileTypes.FileType
import com.intellij.util.ThreeState
import com.intellij.util.indexing.*
import com.intellij.util.indexing.FileBasedIndex.InputFilter
import com.intellij.util.indexing.hints.BaseFileTypeInputFilter
import com.intellij.util.indexing.hints.FileTypeSubstitutionStrategy.AFTER_SUBSTITUTION
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.analogjs.analogScript
import org.analogjs.entities.defineMetadataCallInitializer
import org.analogjs.entities.getDefaultSelector
import org.analogjs.lang.AnalogFile
import org.analogjs.lang.AnalogFileType
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.SELECTOR_PROP
import org.angular2.entities.Angular2EntityUtils

val ANALOG_DIRECTIVE_SELECTORS_INDEX_KEY = ID.create<String, Void>("AnalogDirectiveSelectorsIndex")

/**
 * Indexes style languages used in *.vue files.
 */
class AnalogDirectiveSelectorsIndex : ScalarIndexExtension<String>() {

  override fun getName(): ID<String, Void> = ANALOG_DIRECTIVE_SELECTORS_INDEX_KEY

  override fun getIndexer(): DataIndexer<String, Void, FileContent> = DataIndexer { fileContent ->
    val file = fileContent.psiFile as? AnalogFile
               ?: return@DataIndexer emptyMap()
    val selector = file.analogScript?.defineMetadataCallInitializer
                     ?.findProperty(SELECTOR_PROP)
                     ?.value
                     ?.let { Angular2DecoratorUtil.getExpressionStringValue(it) }
                   ?: getDefaultSelector(file)
    val indexNames = Angular2EntityUtils.getDirectiveIndexNames(selector.trim { it <= ' ' })
    indexNames.associateWith { null }
  }

  override fun getKeyDescriptor(): KeyDescriptor<String> =
    EnumeratorStringDescriptor.INSTANCE

  override fun getVersion(): Int = 1

  override fun getInputFilter(): InputFilter = object : BaseFileTypeInputFilter(AFTER_SUBSTITUTION) {
    override fun acceptFileType(fileType: FileType): ThreeState {
      return if (fileType == AnalogFileType) {
        ThreeState.UNSURE // check hasNodeModulesDirInPath
      }
      else {
        ThreeState.NO
      }
    }

    override fun slowPathIfFileTypeHintUnsure(file: IndexedFile): Boolean {
      return !NodeModuleUtil.hasNodeModulesDirInPath(file.file, null)
    }
  }

  override fun dependsOnFileContent(): Boolean = true

}
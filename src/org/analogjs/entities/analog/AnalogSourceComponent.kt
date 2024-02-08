package org.analogjs.entities.analog

import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.javascript.psi.util.stubSafeChildren
import com.intellij.model.Pointer
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.refactoring.suggested.createSmartPointer
import org.analogjs.analogScript
import org.analogjs.lang.AnalogFile
import org.angular2.Angular2DecoratorUtil
import org.angular2.entities.*
import org.angular2.entities.source.Angular2SourceUtil

class AnalogSourceComponent(file: AnalogFile) : AnalogSourceDirective(file), Angular2Component {

  override val templateFile: PsiFile
    get() = file

  override val imports: Set<Angular2Entity>
    get() = getCachedValue {
      Result.create(resolveImports(), PsiModificationTracker.MODIFICATION_COUNT)
    }

  override val jsResolveScope: PsiElement?
    get() = file.analogScript

  override val jsExportScope: PsiElement?
    get() = jsResolveScope

  override val isScopeFullyResolved: Boolean
    get() = true

  override val ngContentSelectors: List<Angular2DirectiveSelector>
    get() = getCachedValue {
      Result.create(Angular2SourceUtil.getNgContentSelectors(file), file)
    }

  override val cssFiles: List<PsiFile>
    get() = getCachedValue {
      Result.create(findCssFiles(), VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS, file)
    }

  override val attributes: Collection<Angular2DirectiveAttribute>
    get() = emptyList()

  override val directiveKind: Angular2DirectiveKind
    get() = Angular2DirectiveKind.REGULAR

  override fun createPointer(): Pointer<AnalogSourceComponent> {
    val file = file.createSmartPointer()
    return Pointer {
      file.dereference()?.let { AnalogSourceComponent(it) }
    }
  }

  override val isStandalone: Boolean
    get() = true

  private fun resolveImports() =
    file.analogScript
      ?.stubSafeChildren
      ?.asSequence()
      ?.filterIsInstance<ES6ImportDeclaration>()
      ?.flatMap { import ->
        import.importedBindings
          .asSequence()
          .flatMap { it.multiResolve(false).asSequence() }
          .filter { it.isValidResult }
          .mapNotNull { Angular2EntitiesProvider.getEntity(it.element) } +
        import.importSpecifiers
          .asSequence()
          .flatMap { it.multiResolve(false).asSequence() }
          .filter { it.isValidResult }
          .mapNotNull { Angular2EntitiesProvider.getEntity(it.element) }
      }
      ?.toSet()
    ?: emptySet()

  private fun findCssFiles() =
    Angular2SourceUtil.findCssFiles(getDefineMetadataProperty(Angular2DecoratorUtil.STYLE_URLS_PROP), true)
      .plus(listOfNotNull(Angular2SourceUtil.getReferencedFile(getDefineMetadataProperty(Angular2DecoratorUtil.STYLE_URL_PROP), true)))
      .toList()
}
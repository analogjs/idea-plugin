package org.analogjs.entities.analog

import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.util.stubSafeChildren
import com.intellij.model.Pointer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.refactoring.suggested.createSmartPointer
import org.analogjs.ANALOG_EXTENSION
import org.analogjs.analogScript
import org.analogjs.entities.defineMetadataCallInitializer
import org.analogjs.entities.getDefaultSelector
import org.analogjs.lang.AnalogFile
import org.angular2.Angular2DecoratorUtil.SELECTOR_PROP
import org.angular2.entities.*
import org.angular2.entities.source.Angular2SourceUtil
import java.util.*

class AnalogSourceComponent(val file: AnalogFile) : UserDataHolderBase(), Angular2Component {

  override fun getName(): String =
    file.name.removeSuffix(ANALOG_EXTENSION).let {
      JSStringUtil.toPascalCase(it)
    }

  override val selector: Angular2DirectiveSelector
    get() = CachedValuesManager.getCachedValue(file) {
      val selectorProp = file.analogScript?.defineMetadataCallInitializer?.findProperty(SELECTOR_PROP)
      val selector = if (selectorProp != null) {
        Angular2SourceUtil.getComponentSelector(selectorProp, selectorProp)
      }
      else {
        Angular2DirectiveSelectorImpl(file, getDefaultSelector(file), null)
      }
      CachedValueProvider.Result.create(selector, file)
    }

  override val sourceElement: PsiElement
    get() = file

  override val templateFile: PsiFile
    get() = file

  override val imports: Set<Angular2Entity>
    get() = file.analogScript
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

  override val isScopeFullyResolved: Boolean
    get() = true

  override val ngContentSelectors: List<Angular2DirectiveSelector>
    get() = CachedValuesManager.getCachedValue(file) {
      CachedValueProvider.Result.create(Angular2SourceUtil.getNgContentSelectors(file), file)
    }

  override val cssFiles: List<PsiFile>
    get() = emptyList() // TODO

  override val exportAs: Map<String, Angular2DirectiveExportAs>
    get() = emptyMap() // TODO

  override val bindings: Angular2DirectiveProperties
    get() = Angular2DirectiveProperties(emptyList(), emptyList()) // TODO

  override val hostDirectives: Collection<Angular2HostDirective>
    get() = emptyList() // TODO

  override fun areHostDirectivesFullyResolved(): Boolean =
    true

  override val attributes: Collection<Angular2DirectiveAttribute>
    get() = emptyList()

  override val directiveKind: Angular2DirectiveKind
    get() = Angular2DirectiveKind.REGULAR

  override fun createPointer(): Pointer<out Angular2Component> {
    val file = file.createSmartPointer()
    return Pointer {
      file.dereference()?.let { AnalogSourceComponent(it) }
    }
  }

  override val isStandalone: Boolean
    get() = true

  override val decorator: ES6Decorator?
    get() = null

  override val typeScriptClass: TypeScriptClass?
    get() = null
}
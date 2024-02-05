package org.analogjs.entities.analog

import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.model.Pointer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.refactoring.suggested.createSmartPointer
import org.analogjs.analogScript
import org.analogjs.entities.defineMetadataCallInitializer
import org.analogjs.entities.getDefaultSelector
import org.analogjs.lang.AnalogFile
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.SELECTOR_PROP
import org.angular2.entities.*
import org.angular2.entities.source.Angular2SourceHostDirectiveWithMappings
import org.angular2.entities.source.Angular2SourceHostDirectiveWithoutMappings
import org.angular2.entities.source.Angular2SourceSymbolCollectorBase
import org.angular2.entities.source.Angular2SourceUtil
import org.angular2.entities.source.Angular2SourceUtil.getExportAs

open class AnalogSourceDirective(val file: AnalogFile)
  : UserDataHolderBase(), Angular2Directive,
    Angular2HostDirectivesResolver.Angular2DirectiveWithHostDirectives {

  @Suppress("LeakingThis")
  private val hostDirectivesResolver = Angular2HostDirectivesResolver(this)

  override fun getName(): String =
    file.name.takeWhile { it != '.' }
      .let { JSStringUtil.toPascalCase(it) }
      .let { "${it}Analog${if (this.isComponent) "Component" else "Directive"}" }

  override val selector: Angular2DirectiveSelector
    get() = getCachedValue {
      val selectorProp = getDefineMetadataProperty(SELECTOR_PROP)
      val selector = if (selectorProp != null) {
        Angular2SourceUtil.getComponentSelector(selectorProp, selectorProp)
      }
      else {
        Angular2DirectiveSelectorImpl(file, getDefaultSelector(file), null)
      }
      Result.create(selector, file)
    }

  override val sourceElement: PsiElement
    get() = file

  override val exportAs: Map<String, Angular2DirectiveExportAs>
    get() = hostDirectivesResolver.exportAs

  override val bindings: Angular2DirectiveProperties
    get() = Angular2DirectiveProperties(emptyList(), emptyList()) // TODO

  override val directExportAs: Map<String, Angular2DirectiveExportAs>
    get() = getCachedValue {
      Result.create(getExportAs(this, getDefineMetadataProperty(Angular2DecoratorUtil.EXPORT_AS_PROP)), file)
    }

  override val directHostDirectivesSet: Angular2ResolvedSymbolsSet<Angular2HostDirective>
    get() = getCachedValue {
      Angular2HostDirectivesResolver.HostDirectivesCollector(file)
        .collect(getDefineMetadataProperty(Angular2DecoratorUtil.EXPORT_AS_PROP))
    }
  override val hostDirectives: Collection<Angular2HostDirective>
    get() = hostDirectivesResolver.hostDirectives

  override fun areHostDirectivesFullyResolved(): Boolean =
    hostDirectivesResolver.hostDirectivesFullyResolved

  override val attributes: Collection<Angular2DirectiveAttribute>
    get() = emptyList()

  override val directiveKind: Angular2DirectiveKind
    get() = Angular2DirectiveKind.REGULAR

  override fun createPointer(): Pointer<out AnalogSourceDirective> {
    val file = file.createSmartPointer()
    return Pointer {
      file.dereference()?.let { AnalogSourceDirective(it) }
    }
  }

  override val isStandalone: Boolean
    get() = true

  override val decorator: ES6Decorator?
    get() = null

  override val typeScriptClass: TypeScriptClass?
    get() = null

  protected fun getDefineMetadataProperty(name: String): JSProperty? {
    return file.analogScript?.defineMetadataCallInitializer?.findProperty(name)
  }

  protected fun <T> getCachedValue(provider: CachedValueProvider<T>): T {
    return CachedValuesManager.getManager(file.project).getCachedValue(this, provider)
  }

}
package org.analogjs.entities.analog

import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.types.JSNamedTypeFactory
import com.intellij.lang.javascript.psi.types.JSTypeContext
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.model.Pointer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbolApiStatus
import com.intellij.webSymbols.WebSymbolQualifiedKind
import org.analogjs.analogScript
import org.analogjs.codeInsight.AnalogStubBasedScopeHandler
import org.analogjs.entities.defineMetadataCallInitializer
import org.analogjs.entities.getDefaultSelector
import org.analogjs.index.getFunctionNameFromAnalogIndex
import org.analogjs.lang.AnalogFile
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.OUTPUT_DEC
import org.angular2.Angular2DecoratorUtil.SELECTOR_PROP
import org.angular2.entities.*
import org.angular2.entities.source.Angular2PropertyInfo
import org.angular2.entities.source.Angular2SourceUtil
import org.angular2.entities.source.Angular2SourceUtil.getExportAs
import org.angular2.lang.Angular2LangUtil
import org.angular2.web.NG_DIRECTIVE_INPUTS
import org.angular2.web.NG_DIRECTIVE_OUTPUTS

open class AnalogSourceDirective(val file: AnalogFile)
  : UserDataHolderBase(), Angular2Directive,
    Angular2HostDirectivesResolver.Angular2DirectiveWithHostDirectives {

  @Suppress("LeakingThis")
  private val hostDirectivesResolver = Angular2HostDirectivesResolver(this)

  override val isModifiable: Boolean
    get() = true

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
    get() = getCachedValue {
      Result.create(getPropertiesNoCache(), file)
    }

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

  override val entitySource: PsiElement?
    get() = file

  override val entitySourceName: String
    get() = file.defaultExportedName

  override val entityJsType: JSType?
    get() = JSNamedTypeFactory.createType(getName(), JSTypeSourceFactory.createTypeSource(file), JSTypeContext.INSTANCE)

  override val apiStatus: WebSymbolApiStatus
    get() = WebSymbolApiStatus.Stable

  override val templateGuards: List<JSElement>
    get() = emptyList()

  protected fun getDefineMetadataProperty(name: String): JSProperty? {
    return file.analogScript?.defineMetadataCallInitializer?.findProperty(name)
  }

  protected fun <T> getCachedValue(provider: CachedValueProvider<T>): T {
    return CachedValuesManager.getManager(file.project).getCachedValue(this, provider)
  }

  private fun getPropertiesNoCache(): Angular2DirectiveProperties {

    val inputs = LinkedHashMap<String, Angular2DirectiveProperty>()
    val outputs = LinkedHashMap<String, Angular2DirectiveProperty>()

    val inputMap = readPropertyMappings(Angular2DecoratorUtil.INPUTS_PROP)
    val outputMap = readPropertyMappings(Angular2DecoratorUtil.OUTPUTS_PROP)

    val script = file.analogScript
    if (script != null) {
      AnalogStubBasedScopeHandler.processDeclarationsInScope(script, { element, _ ->
        val variable = element as? JSVariable
                       ?: return@processDeclarationsInScope true
        processVariable(variable, inputMap, Angular2DecoratorUtil.INPUT_DEC, Angular2DecoratorUtil.INPUT_FUN, NG_DIRECTIVE_INPUTS, inputs)
        processVariable(variable, outputMap, Angular2DecoratorUtil.OUTPUT_DEC, null, NG_DIRECTIVE_OUTPUTS, outputs)
        true
      }, false)
    }
    return Angular2DirectiveProperties(inputs.values, outputs.values)
  }

  private fun readPropertyMappings(source: String): MutableMap<String, Angular2PropertyInfo> =
    Angular2SourceUtil.readDirectivePropertyMappings(getDefineMetadataProperty(source))

  private fun processVariable(variable: JSVariable,
                              mappings: MutableMap<String, Angular2PropertyInfo>,
                              decorator: String,
                              functionName: String?,
                              qualifiedKind: WebSymbolQualifiedKind,
                              result: MutableMap<String, Angular2DirectiveProperty>) {
    val variableName = variable.name ?: return
    val info: Angular2PropertyInfo? =
      mappings.remove(variable.name)
      ?: variable
        .initializerOrStub
        ?.asSafely<JSCallExpression>()
        ?.let {
          Angular2SourceUtil.createPropertyInfo(it, functionName, variableName, ::getFunctionNameFromAnalogIndex)
          ?: createPropertyInfoForEventEmitter(it, decorator, variableName)
        }
    if (info != null) {
      result.putIfAbsent(info.name, AnalogSourceDirectiveProperty.create(variable, qualifiedKind, info))
    }
  }

  private fun createPropertyInfoForEventEmitter(call: JSCallExpression, decorator: String, defaultName: String): Angular2PropertyInfo? =
    if (decorator == OUTPUT_DEC && call.isNewExpression && getFunctionNameFromAnalogIndex(call) == Angular2LangUtil.EVENT_EMITTER)
      Angular2PropertyInfo(defaultName, false, call, declaringElement = null)
    else
      null

}
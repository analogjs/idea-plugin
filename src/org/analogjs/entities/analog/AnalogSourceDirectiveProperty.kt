// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.analogjs.entities.analog

import com.intellij.javascript.webSymbols.apiStatus
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil.isStubBased
import com.intellij.model.Pointer
import com.intellij.navigation.SymbolNavigationService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.asSafely
import com.intellij.webSymbols.PsiSourcedWebSymbol
import com.intellij.webSymbols.WebSymbolApiStatus
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.utils.WebSymbolDeclaredInPsi
import com.intellij.webSymbols.utils.coalesceApiStatus
import org.angular2.Angular2DecoratorUtil
import org.angular2.entities.Angular2DirectiveProperty
import org.angular2.entities.Angular2EntityUtils
import org.angular2.entities.source.Angular2PropertyInfo
import java.util.*

abstract class AnalogSourceDirectiveProperty(
  protected val variable: JSVariable,
  override val qualifiedKind: WebSymbolQualifiedKind,
  override val name: String,
  override val required: Boolean,
  val declarationSource: PsiElement?,
) : Angular2DirectiveProperty {

  companion object {
    fun create(variable: JSVariable,
               qualifiedKind: WebSymbolQualifiedKind,
               info: Angular2PropertyInfo): AnalogSourceDirectiveProperty =
      if (info.declarationRange == null || info.declaringElement == null)
        AnalogSourceVariableDirectiveProperty(
          variable, qualifiedKind, info.name, info.required, info.declarationSource?.takeIf { isStubBased(it) }
        )
      else
        AnalogSourceMappedDirectiveProperty(
          variable, qualifiedKind, info.name, info.required, info.declarationSource?.takeIf { isStubBased(it) },
          info.declaringElement!!, info.declarationRange!!
        )
  }

  override val fieldName: String
    get() = variable.name ?: name

  private val transformParameterType: JSType?
    get() = objectInitializer?.findProperty(Angular2DecoratorUtil.TRANSFORM_PROP)?.jsType?.asRecordType()?.callSignatures
      ?.firstNotNullOfOrNull { signature -> signature.functionType.parameters.takeIf { it.size > 0 }?.get(0) }
      ?.inferredType

  @Suppress("NonAsciiCharacters")
  override val rawJsType: JSType?
    get() = variable.jsType
              ?.asRecordType()
              ?.findPropertySignature("ɵINPUT_SIGNAL_BRAND_WRITE_TYPE")
              ?.jsTypeWithOptionality
            ?: transformParameterType

  override val virtualProperty: Boolean
    get() = false

  override val apiStatus: WebSymbolApiStatus
    get() = coalesceApiStatus(sources) { (it as? JSElementBase)?.apiStatus }

  val sources: List<PsiElement>
    get() = listOf(variable)

  override fun toString(): String {
    return Angular2EntityUtils.toString(this)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val property = other as AnalogSourceDirectiveProperty
    return (variable == property.variable
            && name == property.name
            && kind == property.kind
            && required == property.required)
  }

  override fun hashCode(): Int {
    return Objects.hash(variable, name, kind, required)
  }

  private val objectInitializer: JSObjectLiteralExpression?
    get() = declarationSource as? JSObjectLiteralExpression
            ?: (declarationSource as? JSLiteralExpression)
              ?.context?.asSafely<JSProperty>()
              ?.context?.asSafely<JSObjectLiteralExpression>()

  private class AnalogSourceVariableDirectiveProperty(
    variable: JSVariable,
    qualifiedKind: WebSymbolQualifiedKind,
    name: String,
    required: Boolean,
    declarationSource: PsiElement?,
  ) : AnalogSourceDirectiveProperty(variable, qualifiedKind, name, required, declarationSource), PsiSourcedWebSymbol {
    override val sourceElement: PsiElement
      get() = sources[0]

    override val source: PsiElement
      get() = sourceElement

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> {
      val sns = SymbolNavigationService.getInstance()
      return sources.map { s -> sns.psiElementNavigationTarget(s) }
    }

    override fun createPointer(): Pointer<AnalogSourceVariableDirectiveProperty> {
      val variablePtr = variable.createSmartPointer()
      val name = this.name
      val qualifiedKind = this.qualifiedKind
      val required = this.required
      val declarationSourcePtr = declarationSource?.createSmartPointer()
      return Pointer {
        val variable = variablePtr.dereference()
                       ?: return@Pointer null
        val declarationSource = declarationSourcePtr?.let { it.dereference() ?: return@Pointer null }
        AnalogSourceVariableDirectiveProperty(variable, qualifiedKind, name, required, declarationSource)
      }
    }

    override fun equals(other: Any?): Boolean =
      other === this ||
      other is AnalogSourceVariableDirectiveProperty
      && super.equals(other)

    override fun hashCode(): Int =
      super.hashCode()

  }

  private class AnalogSourceMappedDirectiveProperty(
    variable: JSVariable,
    qualifiedKind: WebSymbolQualifiedKind,
    name: String,
    required: Boolean,
    declarationSource: PsiElement?,
    override val sourceElement: PsiElement,
    override val textRangeInSourceElement: TextRange,
  ) : AnalogSourceDirectiveProperty(variable, qualifiedKind, name, required, declarationSource), WebSymbolDeclaredInPsi {

    override fun createPointer(): Pointer<AnalogSourceMappedDirectiveProperty> {
      val variablePtr = variable.createSmartPointer()
      val sourceElementPtr = sourceElement.createSmartPointer()
      val name = name
      val qualifiedKind = qualifiedKind
      val required = required
      val declarationSourcePtr = declarationSource?.createSmartPointer()
      val textRangeInSourceElement = textRangeInSourceElement
      return Pointer {
        val variable = variablePtr.dereference()
                       ?: return@Pointer null
        val sourceElement = sourceElementPtr.dereference()
                            ?: return@Pointer null
        val declarationSource = declarationSourcePtr?.let { it.dereference() ?: return@Pointer null }
        AnalogSourceMappedDirectiveProperty(variable, qualifiedKind, name, required, declarationSource,
                                            sourceElement, textRangeInSourceElement)
      }
    }

    override fun equals(other: Any?): Boolean =
      other === this ||
      other is AnalogSourceMappedDirectiveProperty
      && super.equals(other)
      && other.sourceElement == sourceElement
      && other.textRangeInSourceElement == textRangeInSourceElement

    override fun hashCode(): Int =
      Objects.hash(super.hashCode(), sourceElement, textRangeInSourceElement)

  }

}

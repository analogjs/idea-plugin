package org.analogjs.index

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.index.FrameworkIndexingHandler
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData
import com.intellij.lang.javascript.psi.stubs.JSImplicitElementStructure
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubIndexKey
import org.analogjs.FUN_DEFINE_METADATA
import org.analogjs.lang.parser.AnalogStubElementTypes
import org.angular2.Angular2DecoratorUtil.INPUT_FUN
import org.angular2.lang.Angular2LangUtil.EVENT_EMITTER

private val STUBBED_CALL_EXPRESSIONS = listOf(FUN_DEFINE_METADATA, INPUT_FUN)
private val STUBBED_NEW_EXPRESSIONS = listOf(EVENT_EMITTER)

private const val ANALOG_FUNCTION_NAME_USER_STRING = "analogfn"

private val INDEX_MAP = mapOf<String, StubIndexKey<String, JSImplicitElementProvider>?>(
  ANALOG_FUNCTION_NAME_USER_STRING to null,
)

fun getFunctionNameFromAnalogIndex(call: JSCallExpression): String? =
  call.indexingData
    ?.implicitElements
    ?.find { it.userString == ANALOG_FUNCTION_NAME_USER_STRING }
    ?.name

class AnalogIndexingHandler : FrameworkIndexingHandler() {

  override fun shouldCreateStubForCallExpression(node: ASTNode): Boolean {
    return isAnalogScriptContext(node)
           && isAnalogStubbedFunctionCall(node)
  }

  override fun processCallExpression(callExpression: JSCallExpression?, outData: JSElementIndexingData) {
    val node = callExpression?.node ?: return
    if (isAnalogScriptContext(node) && isAnalogStubbedFunctionCall(node)) {
      createJSImplicitElementForFunctionCall(callExpression, outData)
    }
  }

  override fun computeJSImplicitElementUserStringKeys(): Set<String> {
    return INDEX_MAP.keys
  }

  override fun indexImplicitElement(element: JSImplicitElementStructure, sink: IndexSink?): Boolean {
    if (sink == null) {
      return false
    }
    val index = INDEX_MAP[element.userString]
    if (index != null) {
      sink.occurrence(index, element.name)
      return true
    }
    return false
  }

  private fun isAnalogScriptContext(node: ASTNode): Boolean =
    generateSequence(node) { it.treeParent }
      .any { it.elementType == AnalogStubElementTypes.SCRIPT_EMBEDDED_CONTENT }

  private fun isAnalogStubbedFunctionCall(callNode: ASTNode): Boolean {
    if (callNode.elementType !== JSStubElementTypes.CALL_EXPRESSION
        && callNode.elementType !== JSStubElementTypes.TYPESCRIPT_NEW_EXPRESSION) return false

    val methodExpression = callNode.firstChildNode
                             .let { if (it.elementType == JSTokenTypes.NEW_KEYWORD) it.treeNext?.treeNext else it }
                           ?: return false
    if (methodExpression.elementType !== JSElementTypes.REFERENCE_EXPRESSION) return false

    val referencedNameElement =
      methodExpression.firstChildNode.takeIf { it.elementType == JSTokenTypes.IDENTIFIER }
      ?: return false
    return (callNode.elementType == JSStubElementTypes.CALL_EXPRESSION && STUBBED_CALL_EXPRESSIONS.contains(referencedNameElement.text))
           || (callNode.elementType == JSStubElementTypes.TYPESCRIPT_NEW_EXPRESSION && STUBBED_NEW_EXPRESSIONS.contains(referencedNameElement.text))
  }

  private fun createJSImplicitElementForFunctionCall(callExpression: JSCallExpression, outData: JSElementIndexingData) {
    val reference = (callExpression.methodExpression as? JSReferenceExpression)
                      ?.takeIf { it.qualifier == null }
                    ?: return
    val referenceName = reference.referenceName
    recordFunctionName(callExpression, outData, referenceName ?: return)
  }

  private fun recordFunctionName(callExpression: JSCallExpression,
                                 outData: JSElementIndexingData,
                                 referenceName: String) {
    outData.addImplicitElement(
      JSImplicitElementImpl.Builder(referenceName, callExpression)
        .setUserStringWithData(
          this,
          ANALOG_FUNCTION_NAME_USER_STRING,
          null
        )
        .toImplicitElement()
    )
  }

}
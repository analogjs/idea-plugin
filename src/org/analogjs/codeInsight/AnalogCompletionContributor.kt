package org.analogjs.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.lang.javascript.completion.JSImportCompletionUtil
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference
import com.intellij.util.ProcessingContext
import org.analogjs.lang.AnalogFile
import org.angular2.codeInsight.language
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression

class AnalogCompletionContributor : CompletionContributor() {
  init {
    extend(CompletionType.BASIC,
           PlatformPatterns.psiElement().with(language(Angular2Language)),
           TemplateExpressionCompletionProvider())
  }

  private class TemplateExpressionCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters,
                                context: ProcessingContext,
                                result: CompletionResultSet) {
      if (parameters.position.containingFile !is AnalogFile) return
      var ref = parameters.position.containingFile.findReferenceAt(parameters.offset)
      if (ref is PsiMultiReference) {
        ref = ref.references.find { it is Angular2PipeReferenceExpression || it is JSReferenceExpressionImpl }
      }
      if (ref is JSReferenceExpressionImpl && (ref.qualifier == null || ref.qualifier is JSThisExpression)) {
        result.runRemainingContributors(parameters) { completionResult ->
          val lookupElement = completionResult.lookupElement
          // Filter out imports - Analog needs to support it differently
          if (lookupElement !is PrioritizedLookupElement<*>
              || lookupElement.priority != JSImportCompletionUtil.IMPORT_PRIORITY.priorityValue.toDouble()) {
            result.withRelevanceSorter(completionResult.sorter)
              .withPrefixMatcher(completionResult.prefixMatcher)
              .addElement(lookupElement)
          }
        }
      }
    }
  }
}
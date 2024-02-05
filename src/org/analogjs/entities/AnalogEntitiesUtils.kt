package org.analogjs.entities

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSExpressionStatement
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.StubSafe
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.stubSafeCallArguments
import org.analogjs.FUN_DEFINE_METADATA
import org.analogjs.index.getFunctionNameFromIndex
import org.analogjs.lang.psi.impl.AnalogScriptEmbeddedContentImpl

@get:StubSafe
val AnalogScriptEmbeddedContentImpl.defineMetadataCallInitializer: JSObjectLiteralExpression?
  get() = (stub
             ?.childrenStubs
             ?.asSequence()
             ?.filterIsInstance<JSCallExpression>()
           ?: children.asSequence()
             .filterIsInstance<JSExpressionStatement>()
             .flatMap { it.children.asSequence() }
             .filterIsInstance<JSCallExpression>()
             .filter { JSStubBasedPsiTreeUtil.isStubBased(it) }
          )
    .firstOrNull { getFunctionNameFromIndex(it) == FUN_DEFINE_METADATA }
    ?.stubSafeCallArguments
    ?.firstNotNullOfOrNull { it as? JSObjectLiteralExpression }

package org.analogjs.entities

import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSExpressionStatement
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.StubSafe
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.stubSafeCallArguments
import org.analogjs.AG_EXTENSION
import org.analogjs.ANALOG_EXTENSION
import org.analogjs.FUN_DEFINE_METADATA
import org.analogjs.index.getFunctionNameFromAnalogIndex
import org.analogjs.lang.AnalogFile
import org.analogjs.lang.psi.impl.AnalogScriptEmbeddedContentImpl
import java.util.*

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
        .firstOrNull { getFunctionNameFromAnalogIndex(it) == FUN_DEFINE_METADATA }
        ?.stubSafeCallArguments
        ?.firstNotNullOfOrNull { it as? JSObjectLiteralExpression }

fun getDefaultSelector(file: AnalogFile): String =
    when {
        file.name.endsWith(ANALOG_EXTENSION) -> file.name.removeSuffix(ANALOG_EXTENSION)
        file.name.endsWith(AG_EXTENSION) -> file.name.removeSuffix(AG_EXTENSION)
        else -> file.name // No matching extension found, keep the original name
    }.let { baseName ->
        sequenceOf(
            JSStringUtil.toPascalCase(baseName), JSStringUtil.toKebabCase(baseName),
            JSStringUtil.toSnakeCase(baseName).uppercase(Locale.US)
        )
            .joinToString(",")
    }
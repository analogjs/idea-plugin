package org.analogjs.codeInsight

import com.intellij.lang.javascript.psi.util.JSStubBasedScopeHandler
import com.intellij.lang.typescript.TypeScriptSpecificHandlersFactory

class AnalogSpecificHandlersFactory : TypeScriptSpecificHandlersFactory() {

  override fun getStubBasedScopeHandler(): JSStubBasedScopeHandler =
    AnalogStubBasedScopeHandler

}
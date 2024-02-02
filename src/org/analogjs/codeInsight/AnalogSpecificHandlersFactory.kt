// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.analogjs.codeInsight

import com.intellij.lang.javascript.psi.util.JSStubBasedScopeHandler
import com.intellij.lang.typescript.TypeScriptSpecificHandlersFactory

class AnalogSpecificHandlersFactory : TypeScriptSpecificHandlersFactory() {

  override fun getStubBasedScopeHandler(): JSStubBasedScopeHandler =
    AnalogStubBasedScopeHandler

}
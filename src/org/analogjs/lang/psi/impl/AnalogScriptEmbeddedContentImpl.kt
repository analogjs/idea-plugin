package org.analogjs.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.lang.javascript.psi.stubs.JSEmbeddedContentStub
import com.intellij.psi.stubs.IStubElementType

class AnalogScriptEmbeddedContentImpl : JSEmbeddedContentImpl {

  constructor(node: ASTNode?) : super(node)

  constructor(stub: JSEmbeddedContentStub, type: IStubElementType<*, *>) : super(stub, type)

}
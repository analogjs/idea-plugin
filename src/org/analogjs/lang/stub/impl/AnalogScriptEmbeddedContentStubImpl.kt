package org.analogjs.lang.stub.impl

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.stubs.impl.JSEmbeddedContentStubImpl
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.analogjs.lang.psi.impl.AnalogScriptEmbeddedContentImpl

class AnalogScriptEmbeddedContentStubImpl : JSEmbeddedContentStubImpl {
  constructor(psi: JSEmbeddedContent, parent: StubElement<*>?, elementType: IStubElementType<out StubElement<*>, *>)
    : super(psi, parent, elementType)

  constructor(dataStream: StubInputStream, parent: StubElement<*>?, elementType: IStubElementType<out StubElement<*>, *>)
    : super(dataStream, parent, elementType)

  override fun createPsi(): JSEmbeddedContent =
    AnalogScriptEmbeddedContentImpl(this, stubType)

}
package org.analogjs.lang.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.stubs.JSEmbeddedContentStub
import com.intellij.lang.javascript.psi.stubs.TypeScriptScriptContentIndex
import com.intellij.lang.javascript.types.JSEmbeddedContentElementType
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.analogjs.lang.parser.AnalogStubElementTypes.EXTERNAL_ID_PREFIX
import org.analogjs.lang.psi.impl.AnalogScriptEmbeddedContentImpl
import org.analogjs.lang.stub.impl.AnalogScriptEmbeddedContentStubImpl
import java.io.IOException

class AnalogScriptEmbeddedContentElementType(debugName: String)
  : JSEmbeddedContentElementType(JavaScriptSupportLoader.TYPESCRIPT, debugName) {

  override fun getExternalId(): String =
    "$EXTERNAL_ID_PREFIX$debugName"

  override fun indexStub(stub: JSEmbeddedContentStub, sink: IndexSink) {
    super.indexStub(stub, sink)
    sink.occurrence(TypeScriptScriptContentIndex.KEY, TypeScriptScriptContentIndex.DEFAULT_INDEX_KEY)
  }

  override fun construct(node: ASTNode): PsiElement {
    return AnalogScriptEmbeddedContentImpl(node)
  }

  @Throws(IOException::class)
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSEmbeddedContentStub {
    return AnalogScriptEmbeddedContentStubImpl(dataStream, parentStub, this)
  }

  override fun createStub(psi: JSEmbeddedContent, parentStub: StubElement<*>?): JSEmbeddedContentStub {
    return AnalogScriptEmbeddedContentStubImpl(psi, parentStub, this)
  }

  override fun isModule(): Boolean =
    true

}
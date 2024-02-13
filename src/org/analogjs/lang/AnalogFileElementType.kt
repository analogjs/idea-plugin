package org.analogjs.lang

import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import org.analogjs.lang.parser.AnalogStubElementTypes
import org.angular2.lang.html.Angular2HtmlFileElementType

class AnalogFileElementType private constructor()
  : IStubFileElementType<PsiFileStub<HtmlFileImpl>>("html.analog", AnalogLanguage.INSTANCE) {

  override fun getStubVersion(): Int {
    return analogStubVersion
  }

  companion object {
    @JvmField
    val INSTANCE: IStubFileElementType<PsiFileStub<HtmlFileImpl>> = AnalogFileElementType()

    val analogStubVersion: Int
      get() = Angular2HtmlFileElementType.angular2HtmlStubVersion + AnalogStubElementTypes.STUB_VERSION
  }
}
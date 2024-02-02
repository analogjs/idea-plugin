// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.analogjs.lang.parser

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.xml.stub.XmlTagStubImpl
import org.analogjs.lang.psi.impl.AnalogTemplateTagImpl

class AnalogTemplateTagElementType : AnalogStubBasedTagElementType("TEMPLATE_TAG") {
  override fun createPsi(node: ASTNode) = AnalogTemplateTagImpl(node)
  override fun createPsi(stub: XmlTagStubImpl) = AnalogTemplateTagImpl(stub, this)
}
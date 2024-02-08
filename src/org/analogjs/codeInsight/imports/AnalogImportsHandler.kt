package org.analogjs.codeInsight.imports

import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.modules.imports.JSImportCandidateWithExecutor
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Editor
import org.analogjs.analogScript
import org.analogjs.entities.analog.AnalogSourceDirective
import org.angular2.codeInsight.imports.Angular2ImportsHandler
import org.angular2.entities.Angular2Entity
import org.angular2.entities.Angular2ImportsOwner

class AnalogImportsHandler : Angular2ImportsHandler {

  override fun accepts(entity: Angular2Entity): Boolean =
    entity is AnalogSourceDirective

  override fun insertImport(editor: Editor?, candidate: JSImportCandidateWithExecutor, importsOwner: Angular2ImportsOwner) {
    val element = candidate.element ?: return
    val destinationModule = (importsOwner as AnalogSourceDirective).file.analogScript
    if (destinationModule == null) {
      return
    }
    val name = candidate.name
    WriteAction.run<RuntimeException> {
      ES6ImportPsiUtil.insertJSImport(destinationModule, name, element, editor)
    }
  }
}
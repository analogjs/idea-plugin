package org.analogjs.codeInsight.imports

import com.intellij.lang.ecmascript6.actions.ES6AddImportExecutor
import com.intellij.lang.javascript.modules.JSImportCandidateDescriptor
import com.intellij.lang.javascript.modules.imports.JSImportDescriptor
import com.intellij.lang.javascript.modules.imports.JSSimpleImportDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import org.analogjs.findAnalogScript
import org.angular2.codeInsight.imports.Angular2GlobalImportCandidateDescriptor
import org.angular2.codeInsight.imports.Angular2ImportCandidateDescriptor

class AnalogAddImportExecutor(place: PsiElement) : ES6AddImportExecutor(place) {
  override fun prepareScopeToAdd(place: PsiElement, fromExternalModule: Boolean): PsiElement? {
    ApplicationManager.getApplication().assertReadAccessAllowed()
    return findAnalogScript(place)
  }

  override fun postProcessScope(place: PsiElement, info: JSImportDescriptor, scope: PsiElement) {
    ApplicationManager.getApplication().assertReadAccessAllowed()
    // TODO
    //val componentClass = Angular2ComponentLocator.findComponentClass(place)
    //if (componentClass == null
    //    || info !is Angular2FieldImportCandidateDescriptor
    //    || (info.importedName == null && info.exportedName == null)
    //    // Don't add import, if a particular field is already present
    //    || componentClass.findMembersByName(info.fieldName)
    //      .any { (it as? JSAttributeListOwner)?.hasModifier(JSAttributeList.ModifierType.STATIC) != true }
    //) return
    //val anchor = componentClass.lastChild
    //runUndoTransparentWriteAction {
    //  PsiDocumentManager.getInstance(place.project).commitAllDocuments()
    //  val semicolon = JSCodeStyleSettings.getSemicolon(componentClass)
    //  JSChangeUtil.createClassMemberPsiFromTextWithContext(
    //    "protected readonly ${info.fieldName} = ${info.importedName ?: info.exportedName}$semicolon",
    //    componentClass, JSElement::class.java)?.let { member ->
    //    val inserted = componentClass.addBefore(member, anchor)
    //    CodeStyleManager.getInstance(place.project).reformatNewlyAddedElement(componentClass.node, inserted.node)
    //  }
    //}
  }

  override fun createImportOrUpdateExistingInner(descriptor: JSImportDescriptor) {
    val type = descriptor.importType
    when {
      descriptor.importType.isComposite ->
        super.createImportOrUpdateExistingInner(descriptor)

      descriptor is Angular2GlobalImportCandidateDescriptor -> {
        val scope = prepareScopeToAdd(place, !type.isNamespace) ?: return
        postProcessScope(place, descriptor, scope)
      }

      descriptor is JSImportCandidateDescriptor || descriptor is JSSimpleImportDescriptor ->
        super.createImportOrUpdateExistingInner(Angular2ImportCandidateDescriptor(descriptor))

      else ->
        super.createImportOrUpdateExistingInner(descriptor)

    }
  }

  override fun getImportStatementText(descriptor: JSImportDescriptor): String {
    if (descriptor is Angular2GlobalImportCandidateDescriptor) {
      return "expose ${descriptor.name}"
    }
    return super.getImportStatementText(descriptor)
  }

}
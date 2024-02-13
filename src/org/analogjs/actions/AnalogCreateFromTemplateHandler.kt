package org.analogjs.actions

import com.intellij.ide.fileTemplates.DefaultCreateFromTemplateHandler
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import org.analogjs.lang.AnalogFileType

class AnalogCreateFromTemplateHandler : DefaultCreateFromTemplateHandler() {

  companion object {
    const val ANALOG_COMPONENT_TEMPLATE_NAME: String = "Analog Component"
    const val ANALOG_DIRECTIVE_TEMPLATE_NAME: String = "Analog Directive"

    const val ANALOG_RECENT_TEMPLATES: String = "analog.recent.templates"
  }

  override fun handlesTemplate(template: FileTemplate): Boolean {
    val fileType = FileTypeManagerEx.getInstanceEx().getFileTypeByExtension(template.extension)
    return AnalogFileType == fileType && template.name
      .let { it == ANALOG_COMPONENT_TEMPLATE_NAME || it == ANALOG_DIRECTIVE_TEMPLATE_NAME }
  }

  override fun isNameRequired(): Boolean = true

  override fun createFromTemplate(project: Project,
                                  directory: PsiDirectory,
                                  fileName: String?,
                                  template: FileTemplate,
                                  templateText: String,
                                  props: MutableMap<String, Any>): PsiElement {
    val propertiesComponent = PropertiesComponent.getInstance(project)
    (propertiesComponent.getList(ANALOG_RECENT_TEMPLATES) ?: emptyList())
      .toMutableList()
      .let {
        it.remove(template.name)
        it.add(template.name)
        propertiesComponent.setList(ANALOG_RECENT_TEMPLATES, it.toList())
      }
    return super.createFromTemplate(project, directory, fileName, template, templateText, props)
  }
}

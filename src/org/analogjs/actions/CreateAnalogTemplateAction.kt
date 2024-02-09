// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.analogjs.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import org.analogjs.AnalogBundle
import org.analogjs.AnalogIcons
import org.analogjs.actions.AnalogCreateFromTemplateHandler.Companion.ANALOG_COMPONENT_TEMPLATE_NAME
import org.analogjs.actions.AnalogCreateFromTemplateHandler.Companion.ANALOG_DIRECTIVE_TEMPLATE_NAME
import org.analogjs.actions.AnalogCreateFromTemplateHandler.Companion.ANALOG_RECENT_TEMPLATES
import org.analogjs.context.hasAnalog
import org.analogjs.context.hasAnalogFiles
import org.jetbrains.annotations.Nls

class CreateAnalogTemplateAction : CreateFileFromTemplateAction(), DumbAware {
  companion object {

    @Nls
    private val name = AnalogBundle.message("action.CreateAnalogTemplateAction.text")
  }

  override fun isAvailable(dataContext: DataContext): Boolean =
    super.isAvailable(dataContext)
    && (PROJECT.getData(dataContext)?.let { hasAnalogFiles(it) } == true
        || (PSI_ELEMENT.getData(dataContext) ?: PSI_FILE.getData(dataContext))?.let { hasAnalog(it) } == true)

  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder
      .setTitle(AnalogBundle.message("analog.create.directive.action.dialog.title", name))

    val recentNames = PropertiesComponent.getInstance(project).getList(ANALOG_RECENT_TEMPLATES) ?: emptyList()

    listOfNotNull(ANALOG_COMPONENT_TEMPLATE_NAME, ANALOG_DIRECTIVE_TEMPLATE_NAME)
      .sortedByDescending { recentNames.indexOf(it) }
      .forEach { name ->
        builder.addKind(
          when (name) {
            ANALOG_COMPONENT_TEMPLATE_NAME -> AnalogBundle.message("analog.create.directive.template.component")
            ANALOG_DIRECTIVE_TEMPLATE_NAME -> AnalogBundle.message("analog.create.directive.template.directive")
            else -> throw IllegalStateException(name)
          },
          AnalogIcons.Analog,
          name
        )
      }
  }

  override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
    AnalogBundle.message("analog.create.directive.action.name", newName)

}

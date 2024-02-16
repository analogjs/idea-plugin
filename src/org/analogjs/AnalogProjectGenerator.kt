package org.analogjs

import com.intellij.execution.filters.Filter
import com.intellij.ide.util.projectWizard.WebTemplateNewProjectWizard
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.GeneratorNewProjectWizardBuilderAdapter
import com.intellij.javascript.CreateRunConfigurationUtil
import com.intellij.javascript.nodejs.packages.NodePackageUtil
import com.intellij.lang.javascript.boilerplate.JavaScriptNewTemplatesFactoryBase
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.lang.javascript.boilerplate.NpxPackageDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ProjectTemplate
import com.intellij.util.PathUtil
import javax.swing.Icon

private const val PACKAGE_NAME = "create-analog"


class AnalogProjectGenerator : NpmPackageProjectGenerator() {

  override fun getId(): String = AnalogBundle.BUNDLE
  override fun getName(): String = AnalogBundle.message("analog.project.create.name");
  override fun packageName(): String = PACKAGE_NAME
  override fun presentablePackageName(): String =  AnalogBundle.message("analog.project.create.name");
  override fun getIcon(): Icon = AnalogIcons.Analog
  override fun getDescription(): String = AnalogBundle.message("analog.project.create.description");
  override fun filters(project: Project, baseDir: VirtualFile): Array<Filter> = emptyArray()
  override fun customizeModule(baseDir: VirtualFile, entry: ContentEntry?) {}

  override fun getNpxCommands(): List<NpxPackageDescriptor.NpxCommand> =
    listOf(NpxPackageDescriptor.NpxCommand(PACKAGE_NAME, PACKAGE_NAME))

  override fun generatorArgs(project: Project, dir: VirtualFile, settings: Settings): Array<String> {
    val workingDir = if (generateInTemp()) dir.name else "."
    return arrayOf(workingDir)
  }

  override fun validateProjectPath(path: String): String? {
    val error = NodePackageUtil.validateNpmPackageName(PathUtil.getFileName(path))
    return error ?: super.validateProjectPath(path)
  }

  override fun onGettingSmartAfterProjectGeneration(project: Project, baseDir: VirtualFile) {
    super.onGettingSmartAfterProjectGeneration(project, baseDir)
    CreateRunConfigurationUtil.npmConfiguration(project, "dev")
  }
}

class AnalogProjectTemplateFactory : JavaScriptNewTemplatesFactoryBase() {
  override fun createTemplates(context: WizardContext?): Array<ProjectTemplate> = arrayOf(AnalogProjectGenerator())
}
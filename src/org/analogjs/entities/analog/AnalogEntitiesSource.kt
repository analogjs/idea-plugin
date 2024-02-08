package org.analogjs.entities.analog

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.asSafely
import com.intellij.util.indexing.FileBasedIndex
import org.analogjs.index.ANALOG_DIRECTIVE_SELECTORS_INDEX_KEY
import org.analogjs.lang.AnalogFile
import org.analogjs.templateTag
import org.angular2.entities.*

class AnalogEntitiesSource : Angular2EntitiesSource {

  override fun getSupportedEntityPsiElements(): List<Class<out PsiElement>> =
    listOf(AnalogFile::class.java)

  override fun getEntity(element: PsiElement): Angular2Entity? =
    element.asSafely<AnalogFile>()?.let { file ->
      CachedValuesManager.getCachedValue(file) {
        val hasTemplate = file.templateTag != null
        CachedValueProvider.Result.create(
          if (hasTemplate) AnalogSourceComponent(file) else AnalogSourceDirective(file),
          file
        )
      }
    }

  override fun findDirectiveCandidates(project: Project, indexLookupName: String): List<Angular2Directive> {
    val psiManager = PsiManager.getInstance(project)
    return FileBasedIndex.getInstance()
      .getContainingFilesIterator(ANALOG_DIRECTIVE_SELECTORS_INDEX_KEY, indexLookupName, GlobalSearchScope.projectScope(project))
      .asSequence()
      .filter { it.isValid }
      .mapNotNull { psiManager.findFile(it) }
      .mapNotNull { Angular2EntitiesProvider.getDirective(it) }
      .toList()
  }
  override fun findTemplateComponent(templateContext: PsiElement): Angular2Component? =
    InjectedLanguageManager.getInstance(templateContext.project)
      .getTopLevelFile(templateContext)
      .asSafely<AnalogFile>()
      ?.let { Angular2EntitiesProvider.getComponent(it) }

  override fun getAllModules(project: Project): Collection<Angular2Module> =
    emptyList()

  override fun getAllPipeNames(project: Project): Collection<String> =
    emptyList()

  override fun findPipes(project: Project, name: String): Collection<Angular2Pipe> =
    emptyList()
}
package org.analogjs.entities.analog

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
import org.angular2.entities.*

class AnalogEntitiesSource : Angular2EntitiesSource {

  override fun getEntity(element: PsiElement): Angular2Entity? =
    element.asSafely<AnalogFile>()?.let { file ->
      CachedValuesManager.getCachedValue(file) {
        CachedValueProvider.Result.create(AnalogSourceComponent(file), file)
      }
    }

  override fun findDirectivesCandidates(project: Project, indexLookupName: String): List<Angular2Directive> {
    val psiManager = PsiManager.getInstance(project)
    return FileBasedIndex.getInstance()
      .getContainingFilesIterator(ANALOG_DIRECTIVE_SELECTORS_INDEX_KEY, indexLookupName, GlobalSearchScope.projectScope(project))
      .asSequence()
      .filter { it.isValid }
      .mapNotNull { psiManager.findFile(it) }
      .mapNotNull { Angular2EntitiesProvider.getDirective(it) }
      .toList()
  }

  override fun getAllModules(project: Project): Collection<Angular2Module> =
    emptyList()

  override fun getAllPipeNames(project: Project): Collection<String> =
    emptyList()

  override fun findPipes(project: Project, name: String): Collection<Angular2Pipe> =
    emptyList()
}
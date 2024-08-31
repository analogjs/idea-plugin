package org.analogjs.context

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.indexing.FileBasedIndexImpl
import com.intellij.webSymbols.context.WebSymbolsContext
import org.analogjs.lang.AgFileType
import org.analogjs.lang.AnalogFileType

const val KIND_ANALOG = "analog"

fun hasAnalogFiles(project: Project): Boolean =
    CachedValuesManager.getManager(project).getCachedValue(project) {
        CachedValueProvider.Result.create(
            FileBasedIndexImpl.disableUpToDateCheckIn<Boolean, Exception> {
                FileTypeIndex.containsFileOfType(AnalogFileType, GlobalSearchScope.projectScope(project))
                FileTypeIndex.containsFileOfType(AgFileType, GlobalSearchScope.projectScope(project))
            },
            VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
            DumbService.getInstance(project)
        )
    }

fun hasAnalog(context: PsiElement) =
    WebSymbolsContext.get(KIND_ANALOG, context) == KIND_ANALOG
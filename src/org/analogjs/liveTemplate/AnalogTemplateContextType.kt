package org.analogjs.liveTemplate

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import org.analogjs.AnalogBundle
import org.analogjs.lang.AnalogFile

class AnalogTemplateContextType : TemplateContextType(AnalogBundle.message("analog.live.template.context")) {
    override fun isInContext(context: TemplateActionContext) = context.file is AnalogFile
}
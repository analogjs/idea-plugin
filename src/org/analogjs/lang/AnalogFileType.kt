package org.analogjs.lang

import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import org.analogjs.AnalogBundle
import org.analogjs.AnalogIcons
import javax.swing.Icon

object AnalogFileType : WebFrameworkHtmlFileType(AnalogLanguage.INSTANCE, "Analog", "analog") {

  override fun getDescription(): String {
    return AnalogBundle.message("filetype.analog.description")
  }

  override fun getIcon(): Icon =
    AnalogIcons.Analog
}
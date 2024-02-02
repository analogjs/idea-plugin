// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.analogjs

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

class AnalogBundle : DynamicBundle(BUNDLE) {

  companion object {
    const val BUNDLE: @NonNls String = "messages.AnalogBundle"
    private val INSTANCE: AnalogBundle = AnalogBundle()

    @JvmStatic
    fun message(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any): @Nls String {
      return INSTANCE.getMessage(key, *params)
    }

    @JvmStatic
    fun htmlMessage(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any): @Nls String {
      return "<html>" + INSTANCE.getMessage(key, *params) + "</html>"
    }

    @JvmStatic
    fun messagePointer(key: @PropertyKey(resourceBundle = BUNDLE) String,
                       vararg params: Any): Supplier<String> {
      return INSTANCE.getLazyMessage(key, *params)
    }
  }
}
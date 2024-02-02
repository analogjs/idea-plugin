package org.analogjs;

import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class AnalogIcons {
  private static @NotNull Icon load(@NotNull String path, int cacheKey, int flags) {
    return IconManager.getInstance().loadRasterizedIcon(path, AnalogIcons.class.getClassLoader(), cacheKey, flags);
  }

  /**
   * 16x11
   */
  public static final @NotNull Icon Analog = load("icons/analog.svg", 1894196026, 0);
}

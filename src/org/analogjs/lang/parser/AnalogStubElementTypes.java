// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.analogjs.lang.parser;

public interface AnalogStubElementTypes {

  int STUB_VERSION = 0;

  String EXTERNAL_ID_PREFIX = "ANALOG:";

  AnalogStubBasedTagElementType STUBBED_TAG = new AnalogStubBasedTagElementType("STUBBED_TAG");

  AnalogTemplateTagElementType TEMPLATE_TAG = new AnalogTemplateTagElementType();

  AnalogScriptEmbeddedContentElementType SCRIPT_EMBEDDED_CONTENT =
    new AnalogScriptEmbeddedContentElementType("SCRIPT_");
}


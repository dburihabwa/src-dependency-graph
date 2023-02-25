/*
 * Copyright (C) 2023 Dorian Burihabwa
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package com.burihabwa.source.checks;

import org.sonar.api.Plugin;

/**
 * Entry point of your plugin containing your custom rules
 */
public class MyJavaRulesPlugin implements Plugin {

  @Override
  public void define(Context context) {
    // server extensions -> objects are instantiated during server startup
    context.addExtension(MyJavaRulesDefinition.class);
  }

}

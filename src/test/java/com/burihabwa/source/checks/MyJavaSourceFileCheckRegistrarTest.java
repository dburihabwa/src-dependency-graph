/*
 * Copyright (C) 2022 Dorian Burihabwa
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package com.burihabwa.source.checks;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.java.api.CheckRegistrar;
import static org.assertj.core.api.Assertions.assertThat;

class MyJavaSourceFileCheckRegistrarTest {

  @Test
  void checkNumberRules() {
    CheckRegistrar.RegistrarContext context = new CheckRegistrar.RegistrarContext();

    MyJavaFileCheckRegistrar registrar = new MyJavaFileCheckRegistrar();
    registrar.register(context);

    assertThat(context.checkClasses()).hasSize(1);
    assertThat(context.testCheckClasses()).hasSize(1);
  }

}

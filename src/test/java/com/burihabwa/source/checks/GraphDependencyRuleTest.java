/*
 * Copyright (C) 2022 Dorian Burihabwa
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
/*
 *
 */
package com.burihabwa.source.checks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.java.checks.verifier.internal.InternalCheckVerifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GraphDependencyRuleTest {
    @TempDir
    Path tempDir;

    @Test
    void test() throws IOException {
        GraphDependencyRule check = new GraphDependencyRule(tempDir);
        InternalCheckVerifier.newInstance()
                .onFile("src/test/resources/simple/Simple.java")
                .withCheck(check)
                .verifyNoIssues();
        String actual, expected;
        try (InputStream actualIn = new FileInputStream(check.computePathToModuleGraph().toFile());
             InputStream expectedIn = new FileInputStream("src/test/resources/simple/module-graph.json")) {
            actual = new String(actualIn.readAllBytes(), Charset.defaultCharset());
            expected = new String(expectedIn.readAllBytes(), Charset.defaultCharset());
        }
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void static_functions_are_saved() throws IOException {
        GraphDependencyRule check = new GraphDependencyRule(Path.of(tempDir.toString()));
        InternalCheckVerifier.newInstance()
                .onFiles(
                        "src/test/resources/static-imports/Consumer.java",
                        "src/test/resources/static-imports/Producer.java"
                ).withCheck(check)
                .verifyNoIssues();

        String actual, expected;
        try (InputStream actualIn = new FileInputStream(check.computePathToModuleGraph().toFile());
             InputStream expectedIn = new FileInputStream("src/test/resources/static-imports/module-graph.json")) {
            actual = new String(actualIn.readAllBytes(), Charset.defaultCharset());
            expected = new String(expectedIn.readAllBytes(), Charset.defaultCharset());
        }
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void explicitly_extended_types_are_listed_as_imports() throws IOException {
        GraphDependencyRule check = new GraphDependencyRule(Path.of(tempDir.toString()));
        InternalCheckVerifier.newInstance()
                .onFiles(
                        "src/test/resources/inheritance/Child.java",
                        "src/test/resources/inheritance/Base.java"
                ).withCheck(check)
                .verifyNoIssues();
        String actual, expected;
        try (InputStream actualIn = new FileInputStream(check.computePathToModuleGraph().toFile());
             InputStream expectedIn = new FileInputStream("src/test/resources/inheritance/module-graph.json")) {
            actual = new String(actualIn.readAllBytes(), Charset.defaultCharset());
            expected = new String(expectedIn.readAllBytes(), Charset.defaultCharset());
        }
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void explicitly_implemented_types_are_listed_as_imports() throws IOException {
        GraphDependencyRule check = new GraphDependencyRule(Path.of(tempDir.toString()));
        InternalCheckVerifier.newInstance()
                .onFiles(
                        "src/test/resources/implementation/Implementable.java",
                        "src/test/resources/implementation/Implementor.java"
                ).withCheck(check)
                .verifyNoIssues();
        String actual, expected;
        try (InputStream actualIn = new FileInputStream(check.computePathToModuleGraph().toFile());
             InputStream expectedIn = new FileInputStream("src/test/resources/implementation/module-graph.json")) {
            actual = new String(actualIn.readAllBytes(), Charset.defaultCharset());
            expected = new String(expectedIn.readAllBytes(), Charset.defaultCharset());
        }
        assertThat(actual).isEqualTo(expected);
    }
}
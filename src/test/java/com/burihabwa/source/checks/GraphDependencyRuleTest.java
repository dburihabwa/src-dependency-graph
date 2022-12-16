/*
 * Copyright (C) 2022 Dorian Burihabwa
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
/*
 *
 */
package com.burihabwa.source.checks;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.sonar.java.checks.verifier.internal.InternalCheckVerifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

class GraphDependencyRuleTest {

    @Test
    void test() {
        GraphDependencyRule check = new GraphDependencyRule();
        InternalCheckVerifier.newInstance()
                .onFiles(
                        "src/main/java/com/burihabwa/source/graph/File.java",
                        "src/main/java/com/burihabwa/source/checks/GraphDependencyRule.java",
                        "src/test/resources/simple/Simple.java"
                ).withCheck(check)
                .verifyNoIssues();
        //check.writeFilesToDisk(Paths.get("boom.json"), GraphDependencyRule.getFiles());
    }

    @Test
    void static_functions_are_saved() throws IOException {
        GraphDependencyRule check = new GraphDependencyRule();
        InternalCheckVerifier.newInstance()
                .onFiles(
                        "src/test/resources/static-imports/Consumer.java",
                        "src/test/resources/static-imports/Producer.java"
                ).withCheck(check)
                .verifyNoIssues();
        Gson gson = new Gson();

        String actual, expected;
        try (InputStream actualIn = new FileInputStream("module-graph.json");
             InputStream expectedIn = new FileInputStream("src/test/resources/static-imports/module-graph.json")) {
            actual = new String(actualIn.readAllBytes(), Charset.defaultCharset());
            expected = new String(expectedIn.readAllBytes(), Charset.defaultCharset());
        }
        assertThat(actual).isEqualTo(expected);
    }
}
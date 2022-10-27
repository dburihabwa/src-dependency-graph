/*
 * Copyright (C) 2022 Dorian Burihabwa
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
/*
 *
 */
package com.burihabwa.source.checks;

import org.junit.jupiter.api.Test;
import org.sonar.java.checks.verifier.internal.InternalCheckVerifier;

class GraphDependencyRuleTest {

    @Test
    void test() {
        GraphDependencyRule check = new GraphDependencyRule();
        InternalCheckVerifier.newInstance()
                //.onFile()
                .onFiles(
                        "src/main/java/com/burihabwa/source/graph/File.java",
                        "src/main/java/com/burihabwa/source/checks/GraphDependencyRule.java",
                        "src/test/resources/simple/Simple.java"
                ).withCheck(check)
                .verifyNoIssues();
        //check.writeFilesToDisk(Paths.get("boom.json"), GraphDependencyRule.getFiles());
    }

}
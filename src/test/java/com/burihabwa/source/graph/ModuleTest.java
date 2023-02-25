/*
 * Copyright (C) 2023 Dorian Burihabwa
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package com.burihabwa.source.graph;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModuleTest {
    @Test
    void loads_module_from_file_and_dumps_expected() throws IOException {
        Path path = Path.of("src", "test", "resources", "simple", "module-graph.json");
        Module module = Module.of(path);
        String moduleToString = module.toString();
        String expected;
        try (FileInputStream in = new FileInputStream(path.toFile())) {
            expected = new String(in.readAllBytes(), Charset.defaultCharset());
        }
        assertThat(moduleToString).isEqualTo(expected);
    }

    @Test
    void loads_module_from_string() throws IOException {
        Path path = Path.of("src", "test", "resources", "simple", "module-graph.json");
        String input;
        try (FileInputStream in = new FileInputStream(path.toFile())) {
            input = new String(in.readAllBytes(), Charset.defaultCharset());
        }
        Module module = Module.of(input);

        assertThat(module.toString()).isEqualTo(input);
    }

    @Test
    void throws_an_IllegalArgumentException_when_source_file_cannot_be_found() {
        Module module = new Module(Collections.emptyList());
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> module.getSourceFilesImpactedByChangeOf(Path.of("src/main/java/org/example/NonExistent.java")),
                "Path to source file cannot be found in source set."
        );
        assertThat(exception)
                .hasMessage("Path to source file cannot be found in source set (src/main/java/org/example/NonExistent.java).");
    }

    @Test
    void returns_an_empty_list_when_module_has_a_single_source_file() {
        Path pathToFile = Path.of("src/main/java/org/example/SingleFile.java");
        SourceFile sourceFile = new SourceFile(
                pathToFile,
                Collections.emptyList(),
                Collections.emptyList()
        );
        Module singleFileModule = new Module(List.of(sourceFile));
        List<SourceFile> impactedSourceFiles = singleFileModule.getSourceFilesImpactedByChangeOf(pathToFile);
        assertThat(impactedSourceFiles).isEmpty();
    }

    @Test
    void returns_an_empty_list_when_module_source_file_is_not_referenced_by_other() {
        Path pathToFile = Path.of("src/main/java/org/example/SingleFile.java");
        List<SourceFile> sourceFiles = List.of(
                new SourceFile(pathToFile, Collections.emptyList(), Collections.emptyList()),
                new SourceFile(Path.of("src/main/java/org/example/OtherFile"), Collections.emptyList(), Collections.emptyList())
        );
        Module singleFileModule = new Module(sourceFiles);
        List<SourceFile> impactedSourceFiles = singleFileModule.getSourceFilesImpactedByChangeOf(pathToFile);
        assertThat(impactedSourceFiles).isEmpty();
    }

    @Test
    void returns_a_list_of_source_files_impacted_by_change() {
        Path pathToFile = Path.of("src/main/java/org/example/SingleFile.java");
        Path pathToSecondFile = Path.of("src/main/java/org/example/OtherFile");
        SourceFile secondFile = new SourceFile(pathToSecondFile, List.of("org.example.Child"), List.of("org.example.Base"));
        List<SourceFile> sourceFiles = List.of(
                new SourceFile(pathToFile, List.of("org.example.Base"), List.of("java.util.List")),
                secondFile
        );
        Module singleFileModule = new Module(sourceFiles);
        List<SourceFile> impactedSourceFiles = singleFileModule.getSourceFilesImpactedByChangeOf(pathToFile);
        assertThat(impactedSourceFiles)
                .hasSize(1)
                .containsExactly(secondFile);
    }

    @Test
    void returns_a_dot_representation_with_a_single_file() throws IOException {
        Path directory = Path.of("src", "test", "resources", "dot");
        Path expected = directory.resolve("single-file.dot");
        SourceFile sourceFile = new SourceFile(Path.of("A.java"), List.of("A"), Collections.emptyList());

        Module module = new Module(List.of(sourceFile));
        String graph = module.toDot();

        try (FileInputStream in = new FileInputStream(expected.toFile())) {
            assertThat(graph).isEqualToIgnoringNewLines(new String(in.readAllBytes(), Charset.defaultCharset()));
        }
    }

    @Test
    void returns_a_dot_representation_with_multiple_files() throws IOException {
        Path directory = Path.of("src", "test", "resources", "dot");
        Path expected = directory.resolve("inheritance-file.dot");
        SourceFile firstFile = new SourceFile(Path.of("A.java"), List.of("org.example.A"), Collections.emptyList());
        SourceFile secondFile = new SourceFile(Path.of("B.java"), List.of("org.example.B"), List.of("org.example.A"));

        Module module = new Module(List.of(firstFile, secondFile));
        String graph = module.toDot();

        try (FileInputStream in = new FileInputStream(expected.toFile())) {
            assertThat(graph).isEqualToIgnoringNewLines(new String(in.readAllBytes(), Charset.defaultCharset()));
        }
    }
}
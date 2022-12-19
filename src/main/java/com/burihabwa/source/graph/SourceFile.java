/*
 * Copyright (C) 2022 Dorian Burihabwa
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package com.burihabwa.source.graph;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SourceFile {
    public final Path path;
    public final List<String> classes;
    public final List<String> imports;

    public SourceFile(Path path, List<String> classes, List<String> imports) {
        this.path = path;
        this.classes = Collections.unmodifiableList(classes);
        this.imports = Collections.unmodifiableList(imports);
    }

    @Override
    public String toString() {
        return String.format(
                "{ \"path\": \"%s\", \"classes\": [%s], \"imports\": [%s] }",
                path,
                join(classes),
                join(imports)
        );
    }

    public static String join(Collection<String> collection) {
        return collection.stream()
                .map(cls -> String.format("\"%s\"", cls))
                .collect(Collectors.joining(", "));
    }
}

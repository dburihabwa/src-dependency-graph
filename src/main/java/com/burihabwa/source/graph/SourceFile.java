/*
 * Copyright (C) 2023 Dorian Burihabwa
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package com.burihabwa.source.graph;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class SourceFile {
    public final Path path;
    public final List<String> classes;
    public final List<String> imports;

    public SourceFile(Path path, List<String> classes, List<String> imports) {
        this.path = path;
        this.classes = Collections.unmodifiableList(classes);
        this.imports = Collections.unmodifiableList(imports);
    }
}

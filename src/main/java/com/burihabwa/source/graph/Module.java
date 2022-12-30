/*
 * Copyright (C) 2022 Dorian Burihabwa
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package com.burihabwa.source.graph;

import com.google.gson.*;

import javax.annotation.CheckForNull;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Module {
    private List<SourceFile> sourceFiles;
    private Map<String, String> classToPath;

    public Module(List<SourceFile> sourceFiles) {
        this.sourceFiles = Collections.unmodifiableList(sourceFiles);
        classToPath = new HashMap<>();
        sourceFiles.stream().forEach(sourceFile -> {
            String sourcePath = sourceFile.path.toString();
            sourceFile.classes.forEach(clazz -> classToPath.put(clazz, sourcePath));
        });
    }

    public static Module of(Path graph) throws IOException {
        Gson gson = new Gson();
        JsonObject object;
        List<SourceFile> convertedSourceFiles = new ArrayList<>();
        try (FileReader reader = new FileReader(graph.toFile())) {
            object = gson.fromJson(reader, JsonObject.class);
        }
        JsonArray fileElements = object.getAsJsonArray("files");
        fileElements.forEach(element -> {
            Path path = Path.of(element.getAsJsonObject().get("path").getAsString());
            List<String> classes = parseStringArray(element.getAsJsonObject().get("classes").getAsJsonArray());
            List<String> imports = parseStringArray(element.getAsJsonObject().get("imports").getAsJsonArray());
            SourceFile file = new SourceFile(path, classes, imports);
            convertedSourceFiles.add(file);
        });
        return new Module(convertedSourceFiles);
    }

    public List<SourceFile> getSourceFilesImpactedByChangeOf(Path changed) {
        SourceFile changedFile = getSourceFile(changed);
        if (changedFile == null) {
            throw new IllegalArgumentException(String.format("Path to source file cannot be found in source set (%s).", changed));
        }
        Set<String> impactedTypes = changedFile.classes.stream().collect(Collectors.toUnmodifiableSet());
        return sourceFiles.parallelStream()
                .filter(sourceFile -> importsImpactedType(sourceFile, impactedTypes))
                .collect(Collectors.toList());
    }

    private static boolean importsImpactedType(SourceFile sourceFile, Set<String> impactedTypes) {
        Set<String> imports = sourceFile.imports.stream().collect(Collectors.toSet());
        imports.retainAll(impactedTypes);
        return !imports.isEmpty();
    }

    @CheckForNull
    private SourceFile getSourceFile(Path path) {
        for (SourceFile sourceFile : sourceFiles) {
            if (sourceFile.path.equals(path)) {
                return sourceFile;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Module.class, new ModuleSerializer());
        return builder.create().toJson(this);
    }

    private static List<String> parseStringArray(JsonArray array) {
        List<String> tokens = new ArrayList<>(array.size());
        for (JsonElement jsonElement : array) {
            tokens.add(jsonElement.getAsString());
        }
        return tokens;
    }

    public String toDot() {
        StringBuilder builder = new StringBuilder();
        builder.append("digraph module {" + System.lineSeparator());
        sourceFiles.forEach(sourceFile -> fileToGraphLine(sourceFile, classToPath, builder));
        builder.append("}" + System.lineSeparator());
        return builder.toString();
    }

    private static void fileToGraphLine(SourceFile sourceFile, Map<String, String> classToPath, StringBuilder builder) {
        String path = sourceFile.path.toString();
        if (sourceFile.imports.isEmpty()) {
            builder.append("  \"" + path + "\";" + System.lineSeparator());
        } else {
            for (String anImport : sourceFile.imports) {
                String pathToImport = classToPath.get(anImport);
                if (pathToImport != null) {
                    builder.append("  \"" + path + "\" -> \"" + pathToImport + "\";" + System.lineSeparator());
                }
            }
        }
    }

    private static class ModuleSerializer implements JsonSerializer<Module> {
        @Override
        public JsonElement serialize(Module module, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject object = new JsonObject();
            JsonArray array = new JsonArray(module.sourceFiles.size());
            for (SourceFile file : module.sourceFiles) {
                JsonObject objectFile = new JsonObject();
                objectFile.addProperty("path", file.path.toString());
                JsonArray classArray = new JsonArray();
                file.classes.forEach(classArray::add);
                objectFile.add("classes", classArray);
                JsonArray importArray = new JsonArray();
                file.imports.forEach(importArray::add);
                objectFile.add("imports", importArray);
                array.add(objectFile);
            }
            object.add("files", array);
            return object;
        }
    }
}

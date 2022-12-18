/*
 * Copyright (C) 2022 Dorian Burihabwa
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package com.burihabwa.source.graph;

import com.google.gson.*;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Files {
    private List<File> sourceFiles;

    public Files(List<File> sourceFiles) {
        this.sourceFiles = Collections.unmodifiableList(sourceFiles);
    }

    public static Files of(Path graph) throws IOException {
        Gson gson = new Gson();
        JsonObject object;
        List<File> convertedFiles = new ArrayList<>();
        try (FileReader reader = new FileReader(graph.toFile())) {
            object = gson.fromJson(reader, JsonObject.class);
        }
        JsonArray fileElements = object.getAsJsonArray("files");
        fileElements.forEach(element -> {
            Path path = Path.of(element.getAsJsonObject().get("path").getAsString());
            List<String> classes = parseStringArray(element.getAsJsonObject().get("classes").getAsJsonArray());
            List<String> imports = parseStringArray(element.getAsJsonObject().get("imports").getAsJsonArray());
            File file = new File(path, classes, imports);
            convertedFiles.add(file);
        });
        return new Files(convertedFiles);
    }

    @Override
    public String toString() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Files.class, new FilesSerializer());
        return builder.create().toJson(this);
    }

    private static List<String> parseStringArray(JsonArray array) {
        List<String> tokens = new ArrayList<>(array.size());
        for (JsonElement jsonElement : array) {
            tokens.add(jsonElement.getAsString());
        }
        return tokens;
    }

    private static class FilesSerializer implements JsonSerializer<Files> {
        @Override
        public JsonElement serialize(Files files, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject object = new JsonObject();
            JsonArray array = new JsonArray(files.sourceFiles.size());
            for (File file : files.sourceFiles) {
                JsonObject objectFile = new JsonObject();
                objectFile.addProperty("path", file.path.toString());
                JsonArray classArray = new JsonArray();
                file.classes.forEach(classArray::add);
                objectFile.add("classes", classArray);
                JsonArray importArray = new JsonArray();
                file.imports.forEach(classArray::add);
                objectFile.add("imports", importArray);
                array.add(objectFile);
            }
            object.add("files", array);
            return object;
        }
    }
}

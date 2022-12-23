/*
 * Copyright (C) 2022 Dorian Burihabwa
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
/*
 *
 */
package com.burihabwa.source.checks;

import com.burihabwa.source.graph.Module;
import com.burihabwa.source.graph.SourceFile;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.InputFileScannerContext;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.ModuleScannerContext;
import org.sonar.plugins.java.api.internal.EndOfAnalysis;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Rule(key = "file-dependency-graph")
public class GraphDependencyRule extends IssuableSubscriptionVisitor implements EndOfAnalysis {
    private static final String FILE_FORMAT = "%s.json";
    private static final String GRAPH_FORMAT = "%s-graph.json";

    private final Path outputFolder;
    private final List<SourceFile> files = new ArrayList<>();

    GraphDependencyRule() {
        this.outputFolder = Path.of(".");
    }

    GraphDependencyRule(Path outputFolder) {
        this.outputFolder = outputFolder;
    }

    @Override
    public boolean scanWithoutParsing(InputFileScannerContext inputFileScannerContext) {
        return Files.exists(computePathToFileGraph(inputFileScannerContext));
    }

    public Path getOutputFolder() {
        return outputFolder;
    }

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return List.of(Tree.Kind.COMPILATION_UNIT);
    }

    @Override
    public void visitNode(Tree tree) {
        CompilationUnitTree cut = (CompilationUnitTree) tree;
        CutVisitor visitor = new CutVisitor();
        tree.accept(visitor);
        Path path = Paths.get(context.getInputFile().path().toString());
        List<String> imports = cut.imports().stream()
                .filter(ImportTree.class::isInstance)
                .map(clause -> ((ImportTree) clause).qualifiedIdentifier())
                .map(GraphDependencyRule::concatenate)
                .collect(Collectors.toList());
        imports.addAll(visitor.imports);
        files.add(new SourceFile(path, visitor.classes, imports));
    }


    @Override
    public void endOfAnalysis(ModuleScannerContext context) {
        Path path = computePathToModuleGraph();
        writeFilesToDisk(path, files);
    }

    public static Path writeFilesToDisk(Path path, List<SourceFile> files) {
        try (
                OutputStream out = new FileOutputStream(path.toFile());
        ) {
            var moduleGraph = new Module(files).toString();
            out.write(moduleGraph.getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return path;
    }

    private static String concatenate(Tree tree) {
        Deque<String> pieces = new LinkedList<>();
        Tree expr = tree;
        while (expr.is(Tree.Kind.MEMBER_SELECT)) {
            MemberSelectExpressionTree mse = (MemberSelectExpressionTree) expr;
            pieces.push(mse.identifier().name());
            pieces.push(".");
            expr = mse.expression();
        }
        if (expr.is(Tree.Kind.IDENTIFIER)) {
            IdentifierTree idt = (IdentifierTree) expr;
            pieces.push(idt.name());
        }

        StringBuilder sb = new StringBuilder();
        for (String piece : pieces) {
            sb.append(piece);
        }
        return sb.toString().replace('$', '.');
    }

    private static class CutVisitor extends BaseTreeVisitor {
        private final List<String> classes = new ArrayList<>();
        private final List<String> imports = new ArrayList<>();

        @Override
        public void visitClass(ClassTree tree) {
            Type type = tree.symbol().type();
            String fqdn = type.fullyQualifiedName();
            classes.add(fqdn);
            Type superClass = tree.symbol().superClass();
            if (superClass != null) {
                convertSuperType(type, superClass).ifPresent(parentTypeAsString -> imports.add(parentTypeAsString));
            }
            tree.superInterfaces().stream()
                    .map(TypeTree::symbolType)
                    .map(implementedType -> convertSuperType(type, implementedType))
                    .map(Optional::get)
                    .forEach(interfaceTypeAsString -> imports.add(interfaceTypeAsString));

            super.visitClass(tree);
        }
    }

    private static Optional<String> convertSuperType(Type type, Type superClass) {
        String fqdn = type.fullyQualifiedName();
        if (superClass.isUnknown()) {
            String packageName = fqdn.substring(0, fqdn.length() - type.name().length());
            return Optional.of(packageName + superClass.name());
        } else {
            String qualifiedName = superClass.fullyQualifiedName();
            if (!qualifiedName.startsWith("java.") && !qualifiedName.startsWith("javax.")) {
                return Optional.of(qualifiedName);
            }
        }
        return Optional.empty();
    }

    private static Path computePathToFileGraph(InputFileScannerContext inputFileScannerContext) {
        String key = FILE_FORMAT.format(inputFileScannerContext.getInputFile().key());
        return Path.of(key);
    }

    public Path computePathToModuleGraph() {
        String moduleKey = context.getModuleKey();
        if (moduleKey.isEmpty()) {
            moduleKey = "module";
        }
        return outputFolder.resolve(String.format(GRAPH_FORMAT, moduleKey));
    }
}

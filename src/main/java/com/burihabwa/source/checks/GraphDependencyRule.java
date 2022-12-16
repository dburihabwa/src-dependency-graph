/*
 * Copyright (C) 2022 Dorian Burihabwa
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
/*
 *
 */
package com.burihabwa.source.checks;

import com.burihabwa.source.graph.File;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.InputFileScannerContext;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.ModuleScannerContext;
import org.sonar.plugins.java.api.internal.EndOfAnalysis;
import org.sonar.plugins.java.api.tree.*;

import java.io.*;
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
    private final List<File> files = new ArrayList<>();

    GraphDependencyRule() {
        this.outputFolder = Path.of(".");
    }

    GraphDependencyRule(Path outputFolder) {
        this.outputFolder = outputFolder;
    }

    @Override
    public boolean scanWithoutParsing(InputFileScannerContext inputFileScannerContext) {
        Path path = computePathToFileGraph(inputFileScannerContext);
        if (Files.exists(path)) {
            return true;
        }
        return false;
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
        Path path = Paths.get(context.getInputFile().key());
        List<String> imports = cut.imports().stream()
                .filter(ImportTree.class::isInstance)
                .map(clause -> ((ImportTree) clause).qualifiedIdentifier())
                .map(GraphDependencyRule::concatenate)
                .collect(Collectors.toList());
        files.add(new File(path, visitor.classes, imports));
    }


    @Override
    public void endOfAnalysis(ModuleScannerContext context) {
        Path path = computePathToModuleGraph();
        writeFilesToDisk(path, files);
    }

    public static Path writeFilesToDisk(Path path, List<File> files) {
        try (
                OutputStream out = new FileOutputStream(path.toFile());
                PrintWriter writer = new PrintWriter(out)
        ) {
            writer.write("{\n\t\"files\": [\n");
            int indexOfLastElement = files.size() - 1;
            if (indexOfLastElement >= 0) {
                for (int i = 0; i < indexOfLastElement; i++) {
                    writer.write("\t\t" + files.get(i) + ",\n");
                }
                writer.write("\t\t" + files.get(indexOfLastElement) + "\n");
            }
            writer.write("\t]\n}\n");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
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

        @Override
        public void visitClass(ClassTree tree) {
            String fqdn = tree.symbol().type().fullyQualifiedName();
            classes.add(fqdn);
            super.visitClass(tree);
        }
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

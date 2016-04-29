package cs.technion.ac.il.sd.app;

import com.google.inject.Inject;
import cs.technion.ac.il.sd.ExternalCompiler;
import cs.technion.ac.il.sd.library.GraphUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Assigment 1 (Makefile) Implementation
 */
public class MakefileImpl implements Makefile {


    private final ExternalCompiler external;
    private HashMap<String, Compilable> nameToComp;
    private HashMap<String, List<String>> nameToDepNames;
    private DirectedGraph<Compilable, DefaultEdge> depGraph;

    @Inject
    public MakefileImpl(ExternalCompiler external) {
        this.external = external;
        this.nameToComp = new HashMap<>();
        this.nameToDepNames = new HashMap<>();
    }

    @Override
    public void processFile(File file) {
        //TODO - remove compilable interface?
        depGraph = createDependenciesGraph(file);
        Optional<Iterator<Compilable>> toposort = GraphUtils.toposort(depGraph);
        if (!toposort.isPresent()) {
            external.fail();
            return;
        }
        toposort.get().forEachRemaining(this::compileAndUpdateDependants);
    }

    private void compileAndUpdateDependants(Compilable compilable) {
        if (compilable.wasModified()) {
            external.compile(compilable.getName());
            GraphUtils.DFSTraverseSingleComponent(depGraph, compilable, null).forEachRemaining(c -> c.wasModified(true));
        }
    }

    private DirectedGraph<Compilable, DefaultEdge> createDependenciesGraph(File file) {
        return parseFile(file).updateModifiedFiles().buildGraph();
    }

    private MakefileImpl updateModifiedFiles() {
        nameToComp.values().stream()
                .filter(c -> c.getType() == Compilable.Type.FILE)
                .forEach(c -> c.wasModified(external.wasModified(c.getName())));
        return this;
    }

    private DirectedGraph<Compilable, DefaultEdge> buildGraph() {

        DirectedGraph<Compilable, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        nameToComp.values().
                forEach(graph::addVertex);
        nameToDepNames.forEach((name, depNames) -> depNames
                .forEach(dep -> graph.addEdge(nameToComp.get(dep), nameToComp.get(name))));
        return graph;
    }

    private MakefileImpl parseFile(File file) {

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.lines().forEach(this::parseLine);
        } catch (IOException e) {
            throw new AssertionError();
        }
        return this;
    }

    private String[] splitInputLine(String line) {
        return line.split("\\W+");
    }

    private void parseLine(String line) {

        Compilable.Type type = line.contains(":") ? Compilable.Type.FILE : Compilable.Type.TASK;
        String[] tasksOrFiles = splitInputLine(line);

        String taskOrFile = tasksOrFiles[0];
        nameToComp.put(taskOrFile, new CompilableImpl(taskOrFile, type));
        ArrayList<String> dependencies = new ArrayList<>();
        for (int i = 1; i < tasksOrFiles.length; i++) {
            String dependency = tasksOrFiles[i];
            dependencies.add(dependency);
            nameToComp.putIfAbsent(dependency, new CompilableImpl(dependency, Compilable.Type.FILE));
        }
        nameToDepNames.put(taskOrFile, dependencies);
    }

}

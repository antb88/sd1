package cs.technion.ac.il.sd.app;

import com.google.inject.Inject;
import cs.technion.ac.il.sd.ExternalCompiler;
import cs.technion.ac.il.sd.library.GraphUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

/**
 * Assigment 1 (Makefile) Implementation
 */
public class MakefileImpl implements Makefile {


    private final ExternalCompiler external;

    @Inject
    public MakefileImpl(ExternalCompiler external) {
        this.external = external;
    }

    @Override
    public void processFile(File file) {

        MakefileParser p = MakefileParser.parse(file).updateModified(external);
        DirectedGraph<Compilable, DefaultEdge> depGraph = createDependenciesGraph(p);
        Optional<Iterator<Compilable>> toposort = GraphUtils.toposort(depGraph);
        if (!toposort.isPresent()) {
            external.fail();
            return;
        }
        toposort.get().forEachRemaining(c -> compileAndUpdateDependants(c, depGraph));
    }


    private void compileAndUpdateDependants(Compilable compilable, DirectedGraph<Compilable, DefaultEdge> depGraph) {
        if (compilable.wasModified()) {
            external.compile(compilable.getName());
            GraphUtils.getAllReachableVerticesFromSource(depGraph, compilable).forEach(c -> c.wasModified(true));
        }
    }

    private DirectedGraph<Compilable, DefaultEdge> createDependenciesGraph(MakefileParser p) {
        DirectedGraph<Compilable, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        Collection<Compilable> compilables = p.getCompilables();
        compilables.forEach(graph::addVertex);
        compilables.forEach(t -> p.getDependantsOf(t)
                .forEach(d -> graph.addEdge(d, t)));
        return graph;
    }
}

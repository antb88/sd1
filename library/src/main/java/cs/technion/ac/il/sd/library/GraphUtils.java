package cs.technion.ac.il.sd.library;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.Iterator;
import java.util.Optional;

/**
 * GraphUtils library
 */
public class GraphUtils {

    /**
     * Performs topological sort on DirectedGraph
     *
     * @param graph graph on which topological sort will be applied
     * @return Optional Iterator whose order of iteration is graph's topological sort
     *  OR Optional.empty if there is no valid sort
     */

    public static <V,E> Optional<Iterator<V>> toposort(DirectedGraph<V, E> graph)  {
        if (hasCycle(graph))
            return Optional.empty();
        return Optional.of(new TopologicalOrderIterator<>(graph));
    }

    /**
     * Checks whether a given {@link DirectedGraph} has a cycle
     *
     * @param graph graph
     * @return true iff graph has cycle
     */
    public static <V,E> boolean hasCycle(DirectedGraph<V,E> graph) {
        return new CycleDetector<>(graph).detectCycles();
    }


}

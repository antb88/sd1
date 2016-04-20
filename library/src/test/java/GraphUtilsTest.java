import cs.technion.ac.il.sd.library.GraphUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.DirectedGraphBuilder;
import org.jgrapht.graph.builder.DirectedGraphBuilderBase;
import org.jgrapht.graph.builder.DirectedWeightedGraphBuilderBase;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Test file for {@link GraphUtils#toposort(DirectedGraph)}
 */
public class GraphUtilsTest {

    private static final boolean PRINT_ON_FAIL = true;

    private static final DirectedAcyclicGraph<Integer, DefaultEdge> smallGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
    private static final DirectedAcyclicGraph<Integer, DefaultEdge> emptyGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
    private static final DirectedAcyclicGraph<Integer, DefaultEdge> complexGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);

    private <V, E> Optional<Iterator<V>> toposort(DirectedGraph<V, E> graph) {
        return GraphUtils.toposort(graph);
    }

    public <V, E> boolean toposortInvariant(DirectedGraph<V, E> graph, Iterator<V> toposort) {

        Map<V, Integer> topoOrder = new HashMap<>();
        int order = 0;
        while (toposort.hasNext()) {
            topoOrder.put(toposort.next(), ++order);
        }
        if (topoOrder.keySet().size() != graph.vertexSet().size()) {
            return false;
        }

        for (E edge : graph.edgeSet()) {

            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);

            if (topoOrder.get(source) > topoOrder.get(target)) {
                if (PRINT_ON_FAIL) {
                    System.out.println("toposort indexOf " + source + " = " + topoOrder.get(source));
                    System.out.println("toposort indexOf " + target + " = " + topoOrder.get(target));
                    System.out.println("graph = " + graph);
                    System.out.println("toposort = " + toposort);
                }
                return false;
            }
        }
        return true;
    }


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setupSmall() {
        smallGraph.addVertex(1);
        smallGraph.addVertex(2);
        smallGraph.addVertex(3);
        smallGraph.addVertex(4);
        smallGraph.addEdge(1, 2);
        smallGraph.addEdge(1, 3);
        smallGraph.addEdge(3, 4);
        System.out.println("@setupSmall");
    }


    @BeforeClass
    public static void setupComplex() {
        complexGraph.addVertex(5);
        complexGraph.addVertex(7);
        complexGraph.addVertex(3);
        complexGraph.addVertex(11);
        complexGraph.addVertex(8);
        complexGraph.addVertex(2);
        complexGraph.addVertex(9);
        complexGraph.addVertex(10);
        complexGraph.addEdge(5, 11);
        complexGraph.addEdge(11, 2);
        complexGraph.addEdge(11, 9);
        complexGraph.addEdge(11, 10);
        complexGraph.addEdge(7, 11);
        complexGraph.addEdge(7, 8);
        complexGraph.addEdge(8, 9);
        complexGraph.addEdge(3, 8);
        complexGraph.addEdge(3, 10);
        System.out.println("@setupComplex");
    }

    @Test
    public void nullGraphThrows() {
        thrown.expect(IllegalArgumentException.class);
        toposort(null);
    }

    @Test
    public void cyclicToposortFails() {
        DirectedGraph<Integer, DefaultEdge> cyclicGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        cyclicGraph.addVertex(1);
        cyclicGraph.addVertex(2);
        cyclicGraph.addVertex(3);
        cyclicGraph.addVertex(4);
        cyclicGraph.addEdge(1, 2);
        cyclicGraph.addEdge(1, 3);
        cyclicGraph.addEdge(3, 4);
        cyclicGraph.addEdge(4, 1);
        Assert.assertEquals(true, GraphUtils.hasCycle(cyclicGraph));
        Assert.assertEquals(Optional.empty(), toposort(cyclicGraph));
    }

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10);


    @Test
    public void smallGraphReturnsValidSort() {

        List<Integer> topoSort = new LinkedList<>();
        toposort(smallGraph).get().forEachRemaining(topoSort::add);
        boolean sortValid = topoSort.equals(Arrays.asList(1, 2, 3, 4))
                || topoSort.equals(Arrays.asList(1, 3, 2, 4));

        Assert.assertTrue(toposortInvariant(smallGraph, toposort(smallGraph).get()));
        Assert.assertTrue(sortValid);
    }


    @Test
    public void emptyGraphReturnsEmptySort() {
        Iterator<Integer> topoSort = toposort(emptyGraph).get();
        Assert.assertEquals(false, topoSort.hasNext());
    }


    @Test
    public void complexGraphPreservesInvariant() {
        Iterator<Integer> topoSort = toposort(complexGraph).get();
        Assert.assertTrue(toposortInvariant(complexGraph, topoSort));
    }
}
